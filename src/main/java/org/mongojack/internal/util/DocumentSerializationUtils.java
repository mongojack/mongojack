/*
 * Copyright 2011 VZ Netzwerke Ltd
 * Copyright 2014 devbliss GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mongojack.internal.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.std.AsArraySerializerBase;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import org.bson.BsonBinary;
import org.bson.BsonBoolean;
import org.bson.BsonDateTime;
import org.bson.BsonDecimal128;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonDocumentWriter;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonMaxKey;
import org.bson.BsonMinKey;
import org.bson.BsonObjectId;
import org.bson.BsonRegularExpression;
import org.bson.BsonString;
import org.bson.BsonSymbol;
import org.bson.BsonTimestamp;
import org.bson.BsonUndefined;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.mongojack.Aggregation;
import org.mongojack.Aggregation.Expression;
import org.mongojack.Aggregation.Group.Accumulator;
import org.mongojack.DBProjection.ProjectionBuilder;
import org.mongojack.DBQuery;
import org.mongojack.DBRef;
import org.mongojack.MongoJsonMappingException;
import org.mongojack.QueryCondition;
import org.mongojack.UpdateOperationValue;
import org.mongojack.internal.ObjectIdSerializer;
import org.mongojack.internal.query.CollectionQueryCondition;
import org.mongojack.internal.query.CompoundQueryCondition;
import org.mongojack.internal.query.SimpleQueryCondition;
import org.mongojack.internal.stream.DBEncoderBsonGenerator;
import org.mongojack.internal.update.MultiUpdateOperationValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utilities for helping with serialisation
 */
public class DocumentSerializationUtils {

    private static final Set<Class<?>> BASIC_TYPES;

    static {
        Set<Class<?>> types = new HashSet<>();
        types.add(String.class);
        types.add(Integer.class);
        types.add(Boolean.class);
        types.add(Short.class);
        types.add(Long.class);
        types.add(BigInteger.class);
        types.add(Float.class);
        types.add(Double.class);
        types.add(Byte.class);
        types.add(Character.class);
        types.add(BigDecimal.class);
        types.add(int[].class);
        types.add(boolean[].class);
        types.add(short[].class);
        types.add(long[].class);
        types.add(float[].class);
        types.add(double[].class);
        types.add(byte[].class);
        types.add(char[].class);
        types.add(Date.class);
        // Patterns are used by the regex method of the query builder
        types.add(Pattern.class);
        // Native types that we support
        types.add(ObjectId.class);
        types.add(DBRef.class);
        BASIC_TYPES = types;
    }

    /**
     * Serialize the fields of the given object using the given object mapper.
     * This will convert POJOs to Documents where necessary.
     *
     * @param object       The object to serialize the fields of
     * @param registry     Codec registry
     * @return The Document, safe for serialization to MongoDB
     */
    public static Bson serializeFields(
        Bson object,
        CodecRegistry registry
    ) {
        return object.toBsonDocument(Document.class, registry);
    }

    public static Bson serializeQuery(
        ObjectMapper objectMapper,
        JavaType type,
        @SuppressWarnings("deprecation") DBQuery.Query query
    ) {
        SerializerProvider serializerProvider = JacksonAccessor.getSerializerProvider(objectMapper);
        JsonSerializer<?> serializer = JacksonAccessor.findValueSerializer(serializerProvider, type);
        final BsonDocument document = new BsonDocument();
        try (
            BsonDocumentWriter writer = new BsonDocumentWriter(document);
            DBEncoderBsonGenerator generator = new DBEncoderBsonGenerator(writer)
        ) {
            serializeQuery(serializerProvider, serializer, query, writer, generator);
            return document;
        } catch (IOException e) {
            throw new MongoJsonMappingException(e.getMessage(), e);
        }
    }

    private static void serializeQuery(
        SerializerProvider serializerProvider,
        JsonSerializer<?> serializer,
        @SuppressWarnings("deprecation") DBQuery.Query query,
        BsonDocumentWriter writer,
        DBEncoderBsonGenerator generator
    ) throws IOException {
        writer.writeStartDocument();
        for (Map.Entry<String, QueryCondition> field : query.conditions()) {
            String key = field.getKey();
            QueryCondition condition = field.getValue();
            writer.writeName(key);
            serializeQueryCondition(serializerProvider, serializer, key, condition, writer, generator);
        }
        writer.writeEndDocument();
    }

    private static void serializeQueryCondition(
        SerializerProvider serializerProvider,
        JsonSerializer<?> serializer,
        String key,
        QueryCondition condition,
        BsonDocumentWriter writer,
        DBEncoderBsonGenerator generator
    ) throws IOException {
        if (condition instanceof SimpleQueryCondition) {
            SimpleQueryCondition simple = (SimpleQueryCondition) condition;
            if (simple.requiresSerialization() && simple.getValue() != null) {
                if (keyIsNotOperator(key)) {
                    serializer = findQuerySerializer(false, key, serializerProvider, serializer);
                }
            }
            serializeQueryField(simple.getValue(), serializer, serializerProvider, writer, generator);
        } else if (condition instanceof CollectionQueryCondition) {
            CollectionQueryCondition coll = (CollectionQueryCondition) condition;
            if (keyIsNotOperator(key)) {
                serializer = findQuerySerializer(coll.targetIsCollection(), key, serializerProvider, serializer);
            }
            writer.writeStartArray();
            for (QueryCondition item : coll.getValues()) {
                serializeQueryCondition(serializerProvider, serializer, "$", item, writer, generator);
            }
            writer.writeEndArray();
        } else {
            CompoundQueryCondition compound = (CompoundQueryCondition) condition;
            if (keyIsNotOperator(key)) {
                serializer = findQuerySerializer(compound.targetIsCollection(), key, serializerProvider, serializer);
            }
            serializeQuery(serializerProvider, serializer, compound.getQuery(), writer, generator);
        }
    }

    private static boolean keyIsNotOperator(String key) {
        return !key.startsWith("$");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void serializeQueryField(
        Object value,
        JsonSerializer serializer,
        SerializerProvider serializerProvider,
        BsonDocumentWriter writer,
        DBEncoderBsonGenerator generator
    ) throws IOException {
        if (value == null) {
            writer.writeNull();
            return;
        }
        if (serializer == null) {
            if (value instanceof Collection) {
                writer.writeStartArray();
                Collection<?> coll = (Collection<?>) value;
                for (Object item : coll) {
                    serializeQueryField(item, null, serializerProvider, writer, generator);
                }
                writer.writeEndArray();
                return;
            } else if (value.getClass().isArray()) {
                writer.writeStartArray();
                Object[] array = (Object[]) value;
                for (Object o : array) {
                    serializeQueryField(o, null, serializerProvider, writer, generator);
                }
                writer.writeEndArray();
                return;
            } else {
                // We don't know what it is, just find a serializer for it
                serializer = JacksonAccessor.findValueSerializer(serializerProvider, value.getClass());
            }
        }

        if (serializer.handledType() != null &&
            !serializer.handledType().isAssignableFrom(value.getClass()) &&
            (BASIC_TYPES.contains(value.getClass()) || value instanceof BsonValue)) {
            if (writeKnownType(value, writer)) {
                return;
            }
            serializer = JacksonAccessor.findValueSerializer(serializerProvider, value.getClass());
        } else if (serializer instanceof AsArraySerializerBase && !(value instanceof Iterable)) {
            serializer = JacksonAccessor.findValueSerializer(serializerProvider, value.getClass());
        }

        serializer.serialize(value, generator, serializerProvider);
    }

    public static boolean writeKnownType(
        Object value,
        BsonWriter writer
    ) {
        if (value instanceof String) {
            writer.writeString((String)value);
        } else if (value instanceof Integer) {
            writer.writeInt32((Integer) value);
        } else if (value instanceof Boolean) {
            writer.writeBoolean((Boolean) value);
        } else if (value instanceof Short) {
            writer.writeInt32(((Short) value).intValue());
        } else if (value instanceof Long) {
            writer.writeInt64((Long) value);
        } else if (value instanceof BigInteger) {
            writer.writeString(value.toString());
        } else if (value instanceof Float) {
            writer.writeDouble(((Float) value).doubleValue());
        } else if (value instanceof Double) {
            writer.writeDouble((Double) value);
        } else if (value instanceof Byte) {
            writer.writeInt32(((Byte) value).intValue());
        } else if (value instanceof BigDecimal) {
            writer.writeDecimal128(new Decimal128((BigDecimal) value));
        } else if (value instanceof byte[]) {
            writer.writeBinaryData(new BsonBinary((byte[]) value));
        } else if (value instanceof Date) {
            writer.writeDateTime(((Date) value).getTime());
        } else if (value instanceof Pattern) {
            writer.writeRegularExpression(new BsonRegularExpression(((Pattern) value).pattern()));
        } else if (value instanceof ObjectId) {
            writer.writeObjectId((ObjectId) value);
        } else if (value instanceof BsonSymbol) {
            writer.writeSymbol(((BsonSymbol) value).getSymbol());
        } else if (value instanceof BsonObjectId) {
            writer.writeObjectId(((BsonObjectId) value).getValue());
        } else if (value instanceof BsonBoolean) {
            writer.writeBoolean(((BsonBoolean) value).getValue());
        } else if (value instanceof BsonString) {
            writer.writeString(((BsonString) value).getValue());
        } else if (value instanceof BsonMaxKey) {
            writer.writeMaxKey();
        } else if (value instanceof BsonMinKey) {
            writer.writeMinKey();
        } else if (value instanceof BsonInt64) {
            writer.writeInt64(((BsonInt64) value).getValue());
        } else if (value instanceof BsonInt32) {
            writer.writeInt32(((BsonInt32) value).getValue());
        } else if (value instanceof BsonDouble) {
            writer.writeDouble(((BsonDouble) value).getValue());
        } else if (value instanceof BsonDecimal128) {
            writer.writeDecimal128(((BsonDecimal128) value).getValue());
        } else if (value instanceof BsonDateTime) {
            writer.writeDateTime(((BsonDateTime) value).getValue());
        } else if (value instanceof BsonTimestamp) {
            writer.writeTimestamp((BsonTimestamp) value);
        } else if (value instanceof BsonUndefined) {
            writer.writeUndefined();
        } else if (value instanceof BsonRegularExpression) {
            writer.writeRegularExpression((BsonRegularExpression) value);
        } else if (value instanceof BsonBinary) {
            writer.writeBinaryData((BsonBinary) value);
        } else {
            return false;
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    public static Bson serializeFilter(
        ObjectMapper objectMapper,
        JavaType type,
        Bson query,
        CodecRegistry registry
    ) {
        SerializerProvider serializerProvider = JacksonAccessor.getSerializerProvider(objectMapper);
        JsonSerializer serializer = JacksonAccessor.findValueSerializer(
            serializerProvider, type);
        final BsonDocument document = new BsonDocument();
        try (
            BsonDocumentWriter writer = new BsonDocumentWriter(document);
            DBEncoderBsonGenerator generator = new DBEncoderBsonGenerator(writer)
        ) {
            serializeFilter(serializerProvider, serializer, query, registry, writer, generator);
            return document;
        } catch (Exception e) {
            return query;
        }
    }

    @SuppressWarnings("unchecked")
    private static void serializeFilter(
        SerializerProvider serializerProvider,
        JsonSerializer<?> serializer,
        Bson query,
        CodecRegistry registry,
        BsonDocumentWriter writer,
        DBEncoderBsonGenerator generator
    ) throws IOException {
        // not sure this is the best way to do it.  But you can't get anything out of a Bson but a BsonDocument...
        final Map<String, Object> decoded = registry.get(Map.class).decode(new BsonDocumentReader(query.toBsonDocument(Document.class, registry)), DecoderContext.builder().build());
        serializeFilter(serializerProvider, serializer, decoded, writer, generator);
    }

    private static void serializeFilter(
        final SerializerProvider serializerProvider,
        final JsonSerializer<?> serializer,
        final Map<String, Object> decoded,
        BsonDocumentWriter writer,
        DBEncoderBsonGenerator generator
    ) throws IOException {
        writer.writeStartDocument();
        for (Entry<String, Object> field : decoded.entrySet()) {
            String key = field.getKey();
            Object condition = field.getValue();
            writer.writeName(key);
            serializeFilterCondition(serializerProvider, serializer, key, condition, key.matches("^\\$(?:in|nin|all)$"), writer, generator);
        }
        writer.writeEndDocument();
    }

    @SuppressWarnings("unchecked")
    private static void serializeFilterCondition(
        SerializerProvider serializerProvider,
        JsonSerializer<?> serializer,
        String key,
        Object condition,
        boolean targetIsCollection,
        BsonDocumentWriter writer,
        DBEncoderBsonGenerator generator
    ) throws IOException {
        if (condition instanceof Collection) {
            if (keyIsNotOperator(key)) {
                serializer = findQuerySerializer(targetIsCollection, key, serializerProvider, serializer);
            }
            writer.writeStartArray();
            for (Object item : (Collection<Object>) condition) {
                serializeFilterCondition(serializerProvider, serializer, "$", item, targetIsCollection, writer, generator);
            }
            writer.writeEndArray();
        } else if (condition instanceof Map) {
            if (keyIsNotOperator(key)) {
                serializer = findQuerySerializer(targetIsCollection, key, serializerProvider, serializer);
            }
            serializeFilter(serializerProvider, serializer, (Map<String, Object>) condition, writer, generator);
        } else {
            if (keyIsNotOperator(key)) {
                serializer = findQuerySerializer(false, key, serializerProvider, serializer);
            }
            serializeQueryField(condition, serializer, serializerProvider, writer, generator);
        }
    }

    /**
     * Serialize the given field
     * @param value        The value to serialize
     * @param serializerProvider A SerializerProvider
     * @param registry     The codec registry to be used for serialization
     */
    @SuppressWarnings("unchecked")
    private static void serializeUpdateField(
        Object value,
        SerializerProvider serializerProvider,
        BsonDocumentWriter writer,
        DBEncoderBsonGenerator generator,
        CodecRegistry registry
    ) throws IOException {
        @SuppressWarnings("rawtypes") JsonSerializer serializer;
        if (value == null) {
            writer.writeNull();
            return;
        } else if (value instanceof Bson) {
            BsonDocument document = ((Bson) value).toBsonDocument(Document.class, registry);
            BsonDocumentReader reader = new BsonDocumentReader(document);
            writer.pipe(reader);
            return;
        } else if (value instanceof Collection) {
            writer.writeStartArray();
            Collection<?> coll = (Collection<?>) value;
            for (Object item : coll) {
                serializeUpdateField(item, serializerProvider, writer, generator, registry);
            }
            writer.writeEndArray();
            return;
        } else if (value.getClass().isArray()) {
            writer.writeStartArray();
            Object[] array = (Object[]) value;
            for (Object o : array) {
                serializeUpdateField(o, serializerProvider, writer, generator, registry);
            }
            writer.writeEndArray();
            return;
        } else {
            serializer = JacksonAccessor.findValueSerializer(serializerProvider, value.getClass());
        }

        if (serializer.handledType() != null &&
            !serializer.handledType().isAssignableFrom(value.getClass()) &&
            (BASIC_TYPES.contains(value.getClass()) || value instanceof BsonValue)) {
            if (writeKnownType(value, writer)) {
                return;
            }
        }

        serializer.serialize(value, generator, serializerProvider);
    }

    public static Bson serializeDBUpdate(
        Map<String, Map<String,UpdateOperationValue>> update,
        ObjectMapper objectMapper,
        JavaType javaType,
        CodecRegistry registry
    ) {
        SerializerProvider serializerProvider = JacksonAccessor.getSerializerProvider(objectMapper);

        JsonSerializer<?> serializer = JacksonAccessor.findValueSerializer(serializerProvider, javaType);

        final BsonDocument document = new BsonDocument();
        try (
            BsonDocumentWriter writer = new BsonDocumentWriter(document);
            DBEncoderBsonGenerator generator = new DBEncoderBsonGenerator(writer)
        ) {
            writer.writeStartDocument();
            for (Map.Entry<String, Map<String, UpdateOperationValue>> op : update.entrySet()) {
                writer.writeName(op.getKey());
                writer.writeStartDocument();
                for (Map.Entry<String, UpdateOperationValue> field : op.getValue().entrySet()) {
                    writer.writeName(field.getKey());
                    boolean subDocument = false;
                    if ((op.getKey().equals("$addToSet") || op.getKey().equals("$push"))
                        && field.getValue() instanceof MultiUpdateOperationValue) {
                        subDocument = true;
                        writer.writeStartDocument();
                        writer.writeName("$each");
                    }
                    if (field.getValue().requiresSerialization()) {
                        JsonSerializer<?> fieldSerializer = findUpdateSerializer(
                            field.getValue().isTargetCollection(),
                            field.getKey(),
                            serializerProvider,
                            serializer
                        );
                        if (fieldSerializer != null) {
                            serializeUpdateField(
                                field.getValue(),
                                fieldSerializer,
                                serializerProvider,
                                writer,
                                generator
                            );
                        } else {
                            // Try default serializers
                            serializeUpdateField(
                                field.getValue().getValue(),
                                serializerProvider, writer, generator, registry
                            );
                        }
                    } else {
                        serializeUpdateField(
                            field.getValue().getValue(),
                            serializerProvider, writer, generator, registry
                        );
                    }
                    if (subDocument) {
                        writer.writeEndDocument();
                    }
                }
                writer.writeEndDocument();
            }
            writer.writeEndDocument();
            return document;
        } catch (IOException e) {
            throw new MongoJsonMappingException(e.getMessage(), e);
        }
    }

    private static void serializeUpdateField(
        UpdateOperationValue value,
        JsonSerializer<?> serializer,
        SerializerProvider serializerProvider,
        BsonDocumentWriter writer,
        DBEncoderBsonGenerator generator
    ) throws IOException {
        if (value instanceof MultiUpdateOperationValue) {
            writer.writeStartArray();
            for (Object item : ((MultiUpdateOperationValue) value).getValues()) {
                serializeUpdateField(
                    item,
                    serializer,
                    serializerProvider,
                    generator
                );
            }
            writer.writeEndArray();
        } else {
            serializeUpdateField(
                value.getValue(),
                serializer,
                serializerProvider,
                generator
            );
        }
    }

    @SuppressWarnings("unchecked")
    private static void serializeUpdateField(
        Object value,
        @SuppressWarnings("rawtypes") JsonSerializer serializer,
        SerializerProvider serializerProvider,
        DBEncoderBsonGenerator generator
    ) throws IOException {
        serializer.serialize(value, generator, serializerProvider);
    }

    @SuppressWarnings({"rawtypes", "StatementWithEmptyBody"})
    private static JsonSerializer<?> findUpdateSerializer(
        boolean targetIsCollection, String fieldPath,
        SerializerProvider serializerProvider, JsonSerializer<?> serializer
    ) {
        if (serializer instanceof BeanSerializerBase) {
            JsonSerializer<?> fieldSerializer = serializer;
            // Iterate through the components of the field name
            String[] fields = fieldPath.split("\\.");
            for (String field : fields) {
                if (fieldSerializer == null) {
                    // We don't have a field serializer to look up the field on,
                    // so give up
                    return null;
                }
                if (field.equals("$") || field.matches("\\d+")) {
                    // The current serializer must be a collection
                    if (fieldSerializer instanceof ContainerSerializer) {
                        fieldSerializer = getJsonSerializerForContainer(serializerProvider, fieldSerializer);
                    } else {
                        // Give up, don't attempt to serialise it
                        return null;
                    }
                } else if (fieldSerializer instanceof BeanSerializerBase) {
                    BeanPropertyWriter writer = JacksonAccessor
                        .findPropertyWriter(
                            (BeanSerializerBase) fieldSerializer, field);
                    if (writer != null) {
                        fieldSerializer = getJsonSerializerForBean(serializerProvider, writer);
                    } else {
                        // Give up
                        return null;
                    }
                } else if (fieldSerializer instanceof MapSerializer) {
                    fieldSerializer = ((MapSerializer) fieldSerializer)
                        .getContentSerializer();
                } else {
                    // Don't know how to find what the serialiser for this field
                    // is
                    return null;
                }
            }
            // Now we have a serializer for the field, see if we're supposed to
            // be serialising for a collection
            if (targetIsCollection) {
                if (fieldSerializer instanceof ContainerSerializer) {
                    fieldSerializer = ((ContainerSerializer) fieldSerializer)
                        .getContentSerializer();
                } else if (fieldSerializer instanceof ObjectIdSerializer) {
                    // Special case for ObjectIdSerializer, leave as is, the
                    // ObjectIdSerializer handles both single
                    // values as well as collections with no problems.
                } else {
                    // Give up
                    return null;
                }
            }
            return fieldSerializer;
        } else {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    private static JsonSerializer<?> getJsonSerializerForContainer(final SerializerProvider serializerProvider, JsonSerializer<?> fieldSerializer) {
        JsonSerializer<?> contentSerializer = ((ContainerSerializer) fieldSerializer)
            .getContentSerializer();
        if (contentSerializer == null) {
            // Work it out
            JavaType contentType = ((ContainerSerializer) fieldSerializer)
                .getContentType();
            if (contentType != null) {
                contentSerializer = JacksonAccessor
                    .findValueSerializer(
                        serializerProvider, contentType);
            }
        }
        fieldSerializer = contentSerializer;
        return fieldSerializer;
    }

    @SuppressWarnings({"StatementWithEmptyBody", "rawtypes"})
    private static JsonSerializer<?> findQuerySerializer(
        boolean targetIsCollection, String fieldPath,
        SerializerProvider serializerProvider, JsonSerializer<?> serializer
    ) {
        if (serializer instanceof BeanSerializerBase
            || serializer instanceof MapSerializer) {
            JsonSerializer<?> fieldSerializer = serializer;
            // Iterate through the components of the field name
            String[] fields = fieldPath.split("\\.");
            for (String field : fields) {
                if (fieldSerializer == null) {
                    // We don't have a field serializer to look up the field on,
                    // so give up
                    return null;
                }

                boolean isIndex = field.matches("\\d+");

                // First step into the collection if there is one
                if (!isIndex) {
                    while (fieldSerializer instanceof ContainerSerializer) {
                        fieldSerializer = getJsonSerializerForContainer(serializerProvider, fieldSerializer);
                    }
                }

                if (isIndex) {
                    if (fieldSerializer instanceof ContainerSerializer) {
                        fieldSerializer = getJsonSerializerForContainer(serializerProvider, fieldSerializer);
                    } else {
                        // Give up, don't attempt to serialise it
                        return null;
                    }
                } else if (fieldSerializer instanceof BeanSerializerBase) {
                    BeanPropertyWriter writer = JacksonAccessor
                        .findPropertyWriter(
                            (BeanSerializerBase) fieldSerializer, field);
                    if (writer != null) {
                        fieldSerializer = getJsonSerializerForBean(serializerProvider, writer);
                    } else {
                        // Give up
                        return null;
                    }
                } else {
                    // Don't know how to find what the serialiser for this field
                    // is
                    return null;
                }
            }
            // Now we have a serializer for the field, see if we're supposed to
            // be serialising for a collection
            if (targetIsCollection) {
                if (fieldSerializer instanceof ContainerSerializer) {
                    fieldSerializer = ((ContainerSerializer) fieldSerializer)
                        .getContentSerializer();
                } else if (fieldSerializer instanceof ObjectIdSerializer) {
                    // Special case for ObjectIdSerializer, leave as is, the
                    // ObjectIdSerializer handles both single
                    // values as well as collections with no problems.
                } else {
                    // Give up
                    return null;
                }
            }
            return fieldSerializer;
        } else {
            return null;
        }
    }

    private static JsonSerializer<?> getJsonSerializerForBean(final SerializerProvider serializerProvider, final BeanPropertyWriter writer) {
        JsonSerializer<?> fieldSerializer;
        fieldSerializer = writer.getSerializer();
        if (fieldSerializer == null) {
            // Do a generic lookup
            fieldSerializer = JacksonAccessor
                .findValueSerializer(
                    serializerProvider,
                    writer.getType()
                );
        }
        return fieldSerializer;
    }

    public static Bson serializePipelineStage(ObjectMapper objectMapper, JavaType type, @SuppressWarnings("deprecation") Aggregation.Stage<?> stage) {
        SerializerProvider serializerProvider = JacksonAccessor
            .getSerializerProvider(objectMapper);
        JsonSerializer<?> serializer = JacksonAccessor.findValueSerializer(
            serializerProvider, type);
        return serializePipelineStage(serializerProvider, serializer, stage);
    }

    @SuppressWarnings("deprecation")
    private static Bson serializePipelineStage(
        SerializerProvider serializerProvider,
        JsonSerializer<?> serializer,
        Aggregation.Stage<?> stage
    ) {
        if (stage instanceof Aggregation.Limit) {
            return new Document("$limit", ((Aggregation.Limit) stage).limit());
        }
        if (stage instanceof Aggregation.Skip) {
            return new Document("$skip", ((Aggregation.Skip) stage).skip());
        }
        if (stage instanceof Aggregation.Sort) {
            return new Document("$sort", ((Aggregation.Sort) stage).builder());
        }
        if (stage instanceof Aggregation.Unwind) {
            return new Document("$unwind", ((Object) ((Aggregation.Unwind) stage).path()).toString());
        }
        if (stage instanceof Aggregation.Match) {
            final BsonDocument document = new BsonDocument();
            try (
                BsonDocumentWriter writer = new BsonDocumentWriter(document);
                DBEncoderBsonGenerator generator = new DBEncoderBsonGenerator(writer)
            ) {
                serializeQuery(serializerProvider, serializer, ((Aggregation.Match) stage).query(), writer, generator);
                return new Document("$match", document);
            } catch (IOException e) {
                throw new MongoJsonMappingException(e.getMessage(), e);
            }
        }
        if (stage instanceof Aggregation.Project) {
            ProjectionBuilder builder = ((Aggregation.Project) stage).builder();
            Document object = new Document();
            for (Entry<String, Object> entry : builder.entrySet()) {
                if (entry.getValue() instanceof Expression<?>) {
                    object.append(
                        entry.getKey(),
                        serializeExpression((Expression<?>) entry.getValue())
                    );
                } else {
                    object.append(entry.getKey(), entry.getValue());
                }
            }
            return new Document("$project", object);
        }
        if (stage instanceof Aggregation.Group) {
            Aggregation.Group group = (Aggregation.Group) stage;
            Document object = new Document("_id", serializeExpression(group.key()));
            for (Map.Entry<String, Aggregation.Group.Accumulator> field : group.calculatedFields()) {
                object.append(field.getKey(), serializeAccumulator(field.getValue()));
            }
            return new Document("$group", object);
        }
        if (stage instanceof Aggregation.Out) {
            return new Document("$out", ((Aggregation.Out) stage).collectionName());
        }
        throw new IllegalArgumentException(stage.getClass().getName());
    }

    private static Bson serializeAccumulator(Accumulator accumulator) {
        return new Document(
            accumulator.operator.name(),
            serializeExpression(accumulator.expression)
        );
    }

    @SuppressWarnings({"deprecation", "rawtypes"})
    private static Object serializeExpression(Expression<?> expression) {
        if (expression instanceof Aggregation.FieldPath) {
            return ((Aggregation.FieldPath) expression).toString();
        }
        if (expression instanceof Aggregation.Literal<?>) {
            return new Document("$literal", ((Aggregation.Literal<?>) expression).value());
        }
        if (expression instanceof Aggregation.ExpressionObject) {
            Document object = new Document();
            for (Entry<String, Expression<?>> property : ((Aggregation.ExpressionObject) expression).properties()) {
                object.append(
                    property.getKey(),
                    serializeExpression(property.getValue())
                );
            }
            return object;
        }
        if (expression instanceof Aggregation.OperatorExpression) {
            Aggregation.OperatorExpression<?> oe = (Aggregation.OperatorExpression<?>) expression;
            List<Object> operands = new ArrayList<>();
            for (Expression<?> e : oe.operands()) {
                operands.add(serializeExpression(e));
            }
            return new Document(oe.operator(), operands);
        }
        throw new IllegalArgumentException(expression.getClass().getName());
    }
}
