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
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.std.AsArraySerializerBase;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.UuidCodec;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.mongojack.DBRef;
import org.mongojack.JacksonCodecRegistry;
import org.mongojack.MongoJsonMappingException;
import org.mongojack.UpdateOperationValue;
import org.mongojack.internal.ObjectIdSerializer;
import org.mongojack.internal.stream.DBEncoderBsonGenerator;
import org.mongojack.internal.update.MultiUpdateOperationValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Utilities for helping with serialisation
 */
public class DocumentSerializationUtilsImpl implements DocumentSerializationUtilsApi {

    protected final Set<Class<?>> BASIC_TYPES;

    protected DocumentSerializationUtilsImpl() {
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
     * @param object   The object to serialize the fields of
     * @param registry Codec registry
     * @return The Document, safe for serialization to MongoDB
     */
    @Override
    public Bson serializeFields(
        Bson object,
        CodecRegistry registry
    ) {
        return object.toBsonDocument(Document.class, registry);
    }

    protected boolean keyIsNotOperator(String key) {
        return !key.startsWith("$");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void serializeQueryField(
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

    @Override
    public boolean writeKnownType(
        Object value,
        BsonWriter writer
    ) {
        if (value instanceof String) {
            writer.writeString((String) value);
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
            writer.writeRegularExpression(getRegularExpressionForPattern((Pattern) value));
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

    private void appendFlag(Pattern pattern, int flagToCheck, char characterEquivalent, StringBuilder sb) {
        if ((pattern.flags() & flagToCheck) == flagToCheck) {
            sb.append(characterEquivalent);
        }
    }

    private BsonRegularExpression getRegularExpressionForPattern(final Pattern pattern) {
        String flags = "";
        if (pattern.flags() != 0) {
            StringBuilder sb = new StringBuilder();
            appendFlag(pattern, Pattern.CASE_INSENSITIVE, 'i', sb);
            appendFlag(pattern, Pattern.COMMENTS, 'x', sb);
            appendFlag(pattern, Pattern.MULTILINE, 'm', sb);
            appendFlag(pattern, Pattern.DOTALL, 's', sb);
            flags = sb.toString();
        }
        return new BsonRegularExpression(pattern.pattern(), flags);
    }

    @SuppressWarnings({"RedundantIfStatement", "unused"})
    @Override
    public boolean isKnownType(
        Object value
    ) {
        if (value instanceof String ||
            value instanceof Integer ||
            value instanceof Boolean ||
            value instanceof Short ||
            value instanceof Long ||
            value instanceof BigInteger ||
            value instanceof Float ||
            value instanceof Double ||
            value instanceof Byte ||
            value instanceof BigDecimal ||
            value instanceof UUID ||
            value instanceof byte[] ||
            value instanceof Date ||
            value instanceof Pattern ||
            value instanceof ObjectId ||
            value instanceof BsonSymbol ||
            value instanceof BsonObjectId ||
            value instanceof BsonBoolean ||
            value instanceof BsonString ||
            value instanceof BsonMaxKey ||
            value instanceof BsonMinKey ||
            value instanceof BsonInt64 ||
            value instanceof BsonInt32 ||
            value instanceof BsonDouble ||
            value instanceof BsonDecimal128 ||
            value instanceof BsonDateTime ||
            value instanceof BsonTimestamp ||
            value instanceof BsonUndefined ||
            value instanceof BsonRegularExpression ||
            value instanceof BsonBinary) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean isKnownClass(
        Class<?> value
    ) {
        if (value.equals(String.class) ||
            value.equals(Integer.class) ||
            value.equals(Boolean.class) ||
            value.equals(Short.class) ||
            value.equals(Long.class) ||
            value.equals(BigInteger.class) ||
            value.equals(Float.class) ||
            value.equals(Double.class) ||
            value.equals(Byte.class) ||
            value.equals(BigDecimal.class) ||
            value.equals(UUID.class) ||
            value.equals(byte[].class) ||
            value.equals(Date.class) ||
            value.equals(Pattern.class) ||
            value.equals(ObjectId.class) ||
            value.equals(BsonSymbol.class) ||
            value.equals(BsonObjectId.class) ||
            value.equals(BsonBoolean.class) ||
            value.equals(BsonString.class) ||
            value.equals(BsonMaxKey.class) ||
            value.equals(BsonMinKey.class) ||
            value.equals(BsonInt64.class) ||
            value.equals(BsonInt32.class) ||
            value.equals(BsonDouble.class) ||
            value.equals(BsonDecimal128.class) ||
            value.equals(BsonDateTime.class) ||
            value.equals(BsonTimestamp.class) ||
            value.equals(BsonUndefined.class) ||
            value.equals(BsonRegularExpression.class) ||
            value.equals(BsonBinary.class)) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Bson serializeFilter(
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
            DBEncoderBsonGenerator generator = new DBEncoderBsonGenerator(writer, attemptToExtractUuidRepresentation(registry))
        ) {
            serializeFilter(serializerProvider, serializer, query, registry, writer, generator);
            return document;
        } catch (Exception e) {
            return query;
        }
    }

    protected UuidRepresentation attemptToExtractUuidRepresentation(final CodecRegistry registry) {
        UuidRepresentation uuidRepresentation = UuidRepresentation.STANDARD;
        Codec<UUID> uuidCodec = registry.get(UUID.class);
        if (uuidCodec instanceof UuidCodec) {
            uuidRepresentation = ((UuidCodec) uuidCodec).getUuidRepresentation();
        } else if (registry instanceof JacksonCodecRegistry) {
            uuidRepresentation = ((JacksonCodecRegistry) registry).getUuidRepresentation();
        }
        return uuidRepresentation;
    }

    @SuppressWarnings("unchecked")
    protected void serializeFilter(
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

    protected void serializeFilter(
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
    protected void serializeFilterCondition(
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
        } else if (condition instanceof Pattern) {
            writer.writeRegularExpression(getRegularExpressionForPattern((Pattern) condition));
        } else {
            if (keyIsNotOperator(key)) {
                serializer = findQuerySerializer(false, key, serializerProvider, serializer);
            }
            serializeQueryField(condition, serializer, serializerProvider, writer, generator);
        }
    }

    /**
     * Serialize the given field
     *
     * @param value              The value to serialize
     * @param serializerProvider A SerializerProvider
     * @param registry           The codec registry to be used for serialization
     */
    @SuppressWarnings("unchecked")
    protected void serializeUpdateField(
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

    @Override
    public Bson serializeUpdates(
        Map<String, Map<String, UpdateOperationValue>> update,
        ObjectMapper objectMapper,
        JavaType javaType,
        CodecRegistry registry
    ) {
        SerializerProvider serializerProvider = JacksonAccessor.getSerializerProvider(objectMapper);

        JsonSerializer<?> serializer = JacksonAccessor.findValueSerializer(serializerProvider, javaType);

        final BsonDocument document = new BsonDocument();
        try (
            BsonDocumentWriter writer = new BsonDocumentWriter(document);
            DBEncoderBsonGenerator generator = new DBEncoderBsonGenerator(writer, attemptToExtractUuidRepresentation(registry))
        ) {
            writer.writeStartDocument();
            for (Entry<String, Map<String, UpdateOperationValue>> op : update.entrySet()) {
                writer.writeName(op.getKey());
                writer.writeStartDocument();
                for (Entry<String, UpdateOperationValue> field : op.getValue().entrySet()) {
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

    protected void serializeUpdateField(
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
    protected void serializeUpdateField(
        Object value,
        @SuppressWarnings("rawtypes") JsonSerializer serializer,
        SerializerProvider serializerProvider,
        DBEncoderBsonGenerator generator
    ) throws IOException {
        serializer.serialize(value, generator, serializerProvider);
    }

    @SuppressWarnings({"rawtypes", "StatementWithEmptyBody"})
    protected JsonSerializer<?> findUpdateSerializer(
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
                    JsonSerializer<?> temp = JacksonAccessor.findJsonSerializer(serializerProvider, (BeanSerializerBase) fieldSerializer, field);
                    if (temp != null) {
                        fieldSerializer = temp;
                    } else {
                        // give up
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
    protected JsonSerializer<?> getJsonSerializerForContainer(final SerializerProvider serializerProvider, JsonSerializer<?> fieldSerializer) {
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
    protected JsonSerializer<?> findQuerySerializer(
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
                    JsonSerializer<?> temp = JacksonAccessor.findJsonSerializer(serializerProvider, (BeanSerializerBase) fieldSerializer, field);
                    if (temp != null) {
                        fieldSerializer = temp;
                    } else {
                        // give up
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
}
