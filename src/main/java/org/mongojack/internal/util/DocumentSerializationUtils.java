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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.mongodb.MongoException;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonDocumentWriter;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.Aggregation;
import org.mongojack.Aggregation.Expression;
import org.mongojack.Aggregation.Group.Accumulator;
import org.mongojack.Aggregation.Pipeline;
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
import java.util.function.Supplier;
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
     * @param objectMapper The object mapper to use to do the serialization
     * @param object       The object to serialize the fields of
     * @param registry     Codec registry
     * @return The Document, safe for serialization to MongoDB
     */
    public static Bson serializeFields(
        ObjectMapper objectMapper,
        Bson object,
        CodecRegistry registry
    ) {
        return object.toBsonDocument(Document.class, registry);
    }

    public static Bson serializeQuery(
        ObjectMapper objectMapper,
        JavaType type, DBQuery.Query query
    ) {
        SerializerProvider serializerProvider = JacksonAccessor
            .getSerializerProvider(objectMapper);
        JsonSerializer serializer = JacksonAccessor.findValueSerializer(
            serializerProvider, type);
        return serializeQuery(serializerProvider, serializer, query);
    }

    private static Bson serializeQuery(
        SerializerProvider serializerProvider, JsonSerializer<?> serializer,
        DBQuery.Query query
    ) {
        Document serializedQuery = new Document();
        for (Map.Entry<String, QueryCondition> field : query.conditions()) {
            String key = field.getKey();
            QueryCondition condition = field.getValue();
            serializedQuery.put(
                key,
                serializeQueryCondition(serializerProvider, serializer, key, condition)
            );
        }
        return serializedQuery;
    }


    private static Object serializeQueryCondition(
        SerializerProvider serializerProvider, JsonSerializer<?> serializer,
        String key, QueryCondition condition
    ) {
        if (condition instanceof SimpleQueryCondition) {
            SimpleQueryCondition simple = (SimpleQueryCondition) condition;
            if (!simple.requiresSerialization() || simple.getValue() == null) {
                return simple.getValue();
            } else {
                if (!isOperator(key)) {
                    serializer = findQuerySerializer(false, key,
                        serializerProvider, serializer
                    );
                }
                return serializeQueryField(simple.getValue(), serializer,
                    serializerProvider, key
                );
            }
        } else if (condition instanceof CollectionQueryCondition) {
            CollectionQueryCondition coll = (CollectionQueryCondition) condition;
            if (!isOperator(key)) {
                serializer = findQuerySerializer(coll.targetIsCollection(),
                    key, serializerProvider, serializer
                );
            }
            List<Object> serializedConditions = new ArrayList<>();
            for (QueryCondition item : coll.getValues()) {
                serializedConditions.add(serializeQueryCondition(
                    serializerProvider, serializer, "$", item));
            }
            return serializedConditions;
        } else {
            CompoundQueryCondition compound = (CompoundQueryCondition) condition;
            if (!isOperator(key)) {
                serializer = findQuerySerializer(compound.targetIsCollection(), key, serializerProvider, serializer);
            }
            return serializeQuery(serializerProvider, serializer,
                compound.getQuery()
            );
        }
    }

    private static boolean isOperator(String key) {
        return key.startsWith("$");
    }

    @SuppressWarnings("unchecked")
    private static Object serializeQueryField(
        Object value,
        JsonSerializer serializer,
        SerializerProvider serializerProvider,
        String op
    ) {
        if (serializer == null) {
            if (value == null || BASIC_TYPES.contains(value.getClass())) {
                // Return as is
                return value;
            } else if (value instanceof Collection) {
                Collection<?> coll = (Collection<?>) value;
                List<Object> copy = null;
                int position = 0;
                for (Object item : coll) {
                    Object returned = serializeQueryField(item, null,
                        serializerProvider, op
                    );
                    if (returned != item) {
                        if (copy == null) {
                            copy = new ArrayList<>(coll);
                        }
                        copy.set(position, returned);
                    }
                    position++;
                }
                if (copy != null) {
                    return copy;
                } else {
                    return coll;
                }
            } else if (value.getClass().isArray()) {
                if (BASIC_TYPES.contains(value.getClass().getComponentType())) {
                    return value;
                }
                Object[] array = (Object[]) value;
                Object[] copy = null;
                for (int i = 0; i < array.length; i++) {
                    Object returned = serializeQueryField(array[i], null,
                        serializerProvider, op
                    );
                    if (returned != array[i]) {
                        if (copy == null) {
                            copy = new Object[array.length];
                            System.arraycopy(array, 0, copy, 0, array.length);
                        }
                        copy[i] = returned;
                    }
                }
                if (copy != null) {
                    return copy;
                } else {
                    return array;
                }
            } else {
                // We don't know what it is, just find a serializer for it
                serializer = JacksonAccessor.findValueSerializer(
                    serializerProvider, value.getClass());
            }
        }
        if (serializer.handledType() != null &&
            value != null &&
            !serializer.handledType().isAssignableFrom(value.getClass()) &&
            (BASIC_TYPES.contains(value.getClass()) || value instanceof BsonValue)) {
            return value;
        }

        final JsonSerializer ser = serializer;

        return serializeField(
            generator -> ser.serialize(value, generator, serializerProvider),
            () -> "Error serializing value "
                + value + " in DBQuery operation " + op
        );

    }

    public static Bson serializeFilter(
        ObjectMapper objectMapper,
        JavaType type, Bson query, CodecRegistry registry
    ) {
        SerializerProvider serializerProvider = JacksonAccessor
            .getSerializerProvider(objectMapper);
        JsonSerializer serializer = JacksonAccessor.findValueSerializer(
            serializerProvider, type);
        try {
            return serializeFilter(serializerProvider, serializer, query, registry);
        } catch (Exception e) {
            throw new MongoException("Error parsing query", e);
        }
    }

    private static Bson serializeFilter(
        SerializerProvider serializerProvider,
        JsonSerializer<?> serializer,
        Bson query,
        CodecRegistry registry
    ) {
        // not sure this is the best way to do it.  But you can't get anything out of a Bson but a BsonDocument...
        final Map<String, Object> decoded = registry.get(Map.class).decode(new BsonDocumentReader(query.toBsonDocument(Document.class, registry)), DecoderContext.builder().build());
        return serializeFilter(serializerProvider, serializer, decoded);
    }

    private static Bson serializeFilter(final SerializerProvider serializerProvider, final JsonSerializer<?> serializer, final Map<String, Object> decoded) {
        Document serializedQuery = new Document();
        for (Entry<String, Object> field : decoded.entrySet()) {
            String key = field.getKey();
            Object condition = field.getValue();
            serializedQuery.put(
                key,
                serializeFilterCondition(serializerProvider, serializer, key, condition, key.matches("^\\$(?:in|nin|all)$"))
            );
        }
        return serializedQuery;
    }

    private static Object serializeFilterCondition(
        SerializerProvider serializerProvider,
        JsonSerializer<?> serializer,
        String key,
        Object condition,
        boolean targetIsCollection
    ) {
        if (condition instanceof Collection) {
            if (!isOperator(key)) {
                serializer = findQuerySerializer(targetIsCollection, key, serializerProvider, serializer);
            }
            List<Object> serializedConditions = new ArrayList<>();
            for (Object item : (Collection<Object>) condition) {
                serializedConditions.add(serializeFilterCondition(serializerProvider, serializer, "$", item, targetIsCollection));
            }
            return serializedConditions;
        } else if (condition instanceof Map) {
            if (!isOperator(key)) {
                serializer = findQuerySerializer(targetIsCollection, key, serializerProvider, serializer);
            }
            return serializeFilter(serializerProvider, serializer, (Map<String, Object>) condition);
        } else {
            if (!isOperator(key)) {
                serializer = findQuerySerializer(false, key, serializerProvider, serializer);
            }
            return serializeQueryField(condition, serializer, serializerProvider, key);
        }
    }

    /**
     * Serialize the given field
     *
     * @param objectMapper The object mapper to serialize it with
     * @param value        The value to serialize
     * @param registry     The codec registry to be used for serialization
     * @return The serialized field. May return the same object if no
     * serialization was necessary.
     */
    public static Object serializeField(ObjectMapper objectMapper, Object value, CodecRegistry registry) {
        if (value == null || BASIC_TYPES.contains(value.getClass())) {
            // Return as is
            return value;
        } else if (value instanceof Bson) {
            return serializeFields(objectMapper, (Bson) value, registry);
        } else if (value instanceof Collection) {
            Collection<?> coll = (Collection<?>) value;
            List<Object> copy = null;
            int position = 0;
            for (Object item : coll) {
                Object returned = serializeField(objectMapper, item, registry);
                if (returned != item) {
                    if (copy == null) {
                        copy = new ArrayList<>(coll);
                    }
                    copy.set(position, returned);
                }
                position++;
            }
            if (copy != null) {
                return copy;
            } else {
                return coll;
            }
        } else if (value.getClass().isArray()) {
            if (BASIC_TYPES.contains(value.getClass().getComponentType())) {
                return value;
            }
            Object[] array = (Object[]) value;
            Object[] copy = null;
            for (int i = 0; i < array.length; i++) {
                Object returned = serializeField(objectMapper, array[i], registry);
                if (returned != array[i]) {
                    if (copy == null) {
                        copy = new Object[array.length];
                        System.arraycopy(array, 0, copy, 0, array.length);
                    }
                    copy[i] = returned;
                }
            }
            if (copy != null) {
                return copy;
            } else {
                return array;
            }
        } else {
            // We don't know what it is, serialise it
            return serializeField(
                generator -> objectMapper.writeValue(generator, value),
                () -> "Somehow got an IOException writing to memory"
            );
        }
    }

    public static Bson serializeDBUpdate(
        Map<String, Map<String, UpdateOperationValue>> update,
        ObjectMapper objectMapper, JavaType javaType, CodecRegistry registry
    ) {
        SerializerProvider serializerProvider = JacksonAccessor
            .getSerializerProvider(objectMapper);
        Document dbObject = new Document();

        JsonSerializer<?> serializer = null;

        for (Map.Entry<String, Map<String, UpdateOperationValue>> op : update
            .entrySet()) {
            Document opObject = new Document();
            for (Map.Entry<String, UpdateOperationValue> field : op.getValue()
                .entrySet()) {
                Object value;
                if (field.getValue().requiresSerialization()) {

                    if (serializer == null) {
                        serializer = JacksonAccessor.findValueSerializer(
                            serializerProvider, javaType);
                    }

                    JsonSerializer<?> fieldSerializer = findUpdateSerializer(field
                            .getValue().isTargetCollection(), field.getKey(),
                        serializerProvider, serializer
                    );
                    if (fieldSerializer != null) {
                        value = serializeUpdateField(field.getValue(),
                            fieldSerializer, serializerProvider,
                            op.getKey(), field.getKey()
                        );
                    } else {
                        // Try default serializers
                        value = serializeField(objectMapper, field.getValue()
                            .getValue(), registry);
                    }
                } else {
                    value = field.getValue().getValue();
                }
                if ((op.getKey().equals("$addToSet") || op.getKey().equals("$push"))
                    && field.getValue() instanceof MultiUpdateOperationValue) {
                    // Add to set needs $each for multi values
                    // Same for $push with MultiUpdateOperation
                    opObject.put(field.getKey(), new Document(
                        "$each",
                        value
                    ));
                } else {
                    opObject.put(field.getKey(), value);
                }
            }
            dbObject.append(op.getKey(), opObject);
        }
        return dbObject;
    }

    private static Object serializeUpdateField(
        UpdateOperationValue value,
        JsonSerializer<?> serializer, SerializerProvider serializerProvider,
        String op, String field
    ) {
        if (value instanceof MultiUpdateOperationValue) {
            List<Object> results = new ArrayList<>();
            for (Object item : ((MultiUpdateOperationValue) value).getValues()) {
                results.add(serializeUpdateField(item, serializer,
                    serializerProvider, op, field
                ));
            }
            return results;
        } else {
            return serializeUpdateField(value.getValue(), serializer,
                serializerProvider, op, field
            );
        }
    }

    private static Object serializeUpdateField(
        Object value,
        JsonSerializer serializer,
        SerializerProvider serializerProvider,
        String op,
        String field
    ) {
        return serializeField(
            jsonGenerator -> serializer.serialize(value, jsonGenerator, serializerProvider),
            () -> "Error serializing value " + value + " in operation " + op
        );
    }

    private static Object serializeField(
        MappingConsumer<JsonGenerator> serializer,
        Supplier<String> messageProvider
    ) {
        final BsonDocument document = new BsonDocument();
        try (
            BsonDocumentWriter writer = new BsonDocumentWriter(document);
            DBEncoderBsonGenerator generator = new DBEncoderBsonGenerator(writer)
        ) {
            writer.writeStartDocument();
            writer.writeName("value");
            serializer.accept(generator);
            writer.writeEndDocument();
            return document.get("value");
        } catch (IOException e) {
            throw new MongoJsonMappingException(messageProvider.get(), e);
        }
    }

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
                    } else {
                        // Give up, don't attempt to serialise it
                        return null;
                    }
                } else if (fieldSerializer instanceof BeanSerializerBase) {
                    BeanPropertyWriter writer = JacksonAccessor
                        .findPropertyWriter(
                            (BeanSerializerBase) fieldSerializer, field);
                    if (writer != null) {
                        fieldSerializer = writer.getSerializer();
                        if (fieldSerializer == null) {
                            // Do a generic lookup
                            fieldSerializer = JacksonAccessor
                                .findValueSerializer(
                                    serializerProvider,
                                    writer.getType()
                                );
                        }
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
                    }
                }

                if (isIndex) {
                    if (fieldSerializer instanceof ContainerSerializer) {
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
                    } else {
                        // Give up, don't attempt to serialise it
                        return null;
                    }
                } else if (fieldSerializer instanceof BeanSerializerBase) {
                    BeanPropertyWriter writer = JacksonAccessor
                        .findPropertyWriter(
                            (BeanSerializerBase) fieldSerializer, field);
                    if (writer != null) {
                        fieldSerializer = writer.getSerializer();
                        if (fieldSerializer == null) {
                            // Do a generic lookup
                            fieldSerializer = JacksonAccessor
                                .findValueSerializer(
                                    serializerProvider,
                                    writer.getType()
                                );
                        }
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

    public static List<Bson> serializePipeline(ObjectMapper objectMapper, JavaType type, Pipeline<?> pipeline) {
        SerializerProvider serializerProvider = JacksonAccessor
            .getSerializerProvider(objectMapper);
        JsonSerializer<?> serializer = JacksonAccessor.findValueSerializer(
            serializerProvider, type);
        List<Bson> serializedPipeline = new ArrayList<>();
        for (Aggregation.Stage<?> stage : pipeline.stages()) {
            serializedPipeline.add(serializePipelineStage(serializerProvider, serializer, stage));
        }
        return serializedPipeline;
    }

    public static Bson serializePipelineStage(ObjectMapper objectMapper, JavaType type, Aggregation.Stage<?> stage) {
        SerializerProvider serializerProvider = JacksonAccessor
            .getSerializerProvider(objectMapper);
        JsonSerializer<?> serializer = JacksonAccessor.findValueSerializer(
            serializerProvider, type);
        return serializePipelineStage(serializerProvider, serializer, stage);
    }

    private static Bson serializePipelineStage(
        SerializerProvider serializerProvider, JsonSerializer<?> serializer,
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
            return new Document("$match", serializeQuery(serializerProvider, serializer,
                ((Aggregation.Match) stage).query()
            ));
        }
        if (stage instanceof Aggregation.Project) {
            ProjectionBuilder builder = ((Aggregation.Project) stage).builder();
            Document object = new Document();
            for (Entry<String, Object> entry : builder.entrySet()) {
                if (entry.getValue() instanceof Expression<?>) {
                    object.append(
                        entry.getKey(),
                        serializeExpression(serializerProvider, serializer, (Expression<?>) entry.getValue())
                    );
                } else {
                    object.append(entry.getKey(), entry.getValue());
                }
            }
            return new Document("$project", object);
        }
        if (stage instanceof Aggregation.Group) {
            Aggregation.Group group = (Aggregation.Group) stage;
            Document object = new Document("_id", serializeExpression(serializerProvider, serializer, group.key()));
            for (Map.Entry<String, Aggregation.Group.Accumulator> field : group.calculatedFields()) {
                object.append(field.getKey(), serializeAccumulator(serializerProvider, serializer, field.getValue()));
            }
            return new Document("$group", object);
        }
        if (stage instanceof Aggregation.Out) {
            return new Document("$out", ((Object) ((Aggregation.Out) stage).collectionName()));
        }
        throw new IllegalArgumentException(stage.getClass().getName());
    }

    private static Bson serializeAccumulator(SerializerProvider serializerProvider, JsonSerializer<?> serializer, Accumulator accumulator) {
        return new Document(
            accumulator.operator.name(),
            serializeExpression(serializerProvider, serializer, accumulator.expression)
        );
    }

    private static Object serializeExpression(SerializerProvider serializerProvider, JsonSerializer<?> serializer, Expression<?> expression) {
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
                    serializeExpression(serializerProvider, serializer, property.getValue())
                );
            }
            return object;
        }
        if (expression instanceof Aggregation.OperatorExpression) {
            Aggregation.OperatorExpression<?> oe = (Aggregation.OperatorExpression<?>) expression;
            List<Object> operands = new ArrayList<>();
            for (Expression<?> e : oe.operands()) {
                operands.add(serializeExpression(serializerProvider, serializer, e));
            }
            return new Document(oe.operator(), operands);
        }
        throw new IllegalArgumentException(expression.getClass().getName());
    }
}
