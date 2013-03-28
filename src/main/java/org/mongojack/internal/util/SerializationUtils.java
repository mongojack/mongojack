/*
 * Copyright 2011 VZ Netzwerke Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mongojack.internal.util;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import org.mongojack.DBRef;
import org.mongojack.MongoJsonMappingException;
import org.mongojack.internal.ObjectIdSerializer;
import org.mongojack.internal.object.BsonObjectGenerator;
import org.mongojack.internal.update.MultiUpdateOperationValue;
import org.mongojack.internal.update.UpdateOperationValue;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Utilities for helping with serialisation
 */
public class SerializationUtils {

    private static final Set<Class<?>> BASIC_TYPES;

    static {
        Set<Class<?>> types = new HashSet<Class<?>>();
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
     * Serialize the fields of the given object using the given object mapper.  This will convert POJOs to DBObjects
     * where necessary.
     *
     * @param objectMapper The object mapper to use to do the serialization
     * @param object       The object to serialize the fields of
     * @return The DBObject, safe for serialization to MongoDB
     */
    public static DBObject serializeFields(ObjectMapper objectMapper, DBObject object) {
        BasicDBObject serialised = null;
        for (String field : object.keySet()) {
            Object value = object.get(field);
            Object serialisedValue = serializeField(objectMapper, value);
            if (value != serialisedValue) {
                // It's changed
                if (serialised == null) {
                    // Make a shallow copy of the object
                    serialised = new BasicDBObject();
                    for (String f : object.keySet()) {
                        serialised.put(f, object.get(f));
                    }
                }
                serialised.put(field, serialisedValue);
            }
        }
        if (serialised != null) {
            return serialised;
        } else {
            return object;
        }
    }

    /**
     * Serialize the given field
     *
     * @param objectMapper The object mapper to serialize it with
     * @param value        The value to serialize
     * @return The serialized field.  May return the same object if no serialization was necessary.
     */
    public static Object serializeField(ObjectMapper objectMapper, JavaType parentType, String name, Object value) {
        if (value == null) {
            // return as is
            return value;
        } else if (value instanceof DBObject) {
            // todo Maybe should be passing the parent type and name and stuff....
            return serializeFields(objectMapper, (DBObject) value);
        } else if (value instanceof Collection) {
            Collection<?> coll = (Collection<?>) value;
            List<Object> copy = null;
            int position = 0;
            for (Object item : coll) {
                Object returned = serializeField(objectMapper, parentType, name, item);
                if (returned != item) {
                    if (copy == null) {
                        copy = new ArrayList<Object>(coll);
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
                Object returned = serializeField(objectMapper, parentType, name, array[i]);
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
        }
        // It's not a collection, try finding a serializer
        JsonSerializer serializer = findSerializer(objectMapper, parentType, name);
        if (serializer != null) {
            BsonObjectGenerator gen = new BsonObjectGenerator();
            try {
                serializer.serialize(value, gen, JacksonAccessor.getSerializerProvider(objectMapper));
            } catch (JsonMappingException e) {
                throw new MongoJsonMappingException("Error serialising query field " + name, e);
            } catch (IOException e) {
                throw new MongoException("Unknown IOException while serialising query field " + name, e);
            }
            return gen.getValue();
        } else if (BASIC_TYPES.contains(value.getClass())) {
            // Return as is
            return value;
        } else {
            // We don't know what it is, serialise it
            BsonObjectGenerator generator = new BsonObjectGenerator();
            try {
                objectMapper.writeValue(generator, value);
            } catch (JsonMappingException e) {
                throw new MongoJsonMappingException(e);
            } catch (IOException e) {
                throw new RuntimeException("Somehow got an IOException writing to memory", e);
            }
            return generator.getValue();
        }
    }

    public static JsonSerializer findSerializer(ObjectMapper objectMapper, JavaType type, String name) {
        // Split the name into its 
        // Find the name of the current property, strip off anything starting in $
        String prop = "$";
        while (!prop.startsWith("$")) {
            String[] split = name.split("\\.", 2);
            prop = split[0];
            if (split.length == 1) {
                name = null;
                break;
            } else {
                name = split[1];
            }
        }


        // Get the serializer provider
        SerializerProvider provider = objectMapper.getSerializerProvider();
        // Find the serialiser for the given field name
        return null;
    }

    /**
     * Serialize the given field
     *
     * @param objectMapper The object mapper to serialize it with
     * @param value        The value to serialize
     * @return The serialized field.  May return the same object if no serialization was necessary.
     */
    public static Object serializeField(ObjectMapper objectMapper, Object value) {
        if (value == null || BASIC_TYPES.contains(value.getClass())) {
            // Return as is
            return value;
        } else if (value instanceof DBObject) {
            return serializeFields(objectMapper, (DBObject) value);
        } else if (value instanceof Collection) {
            Collection<?> coll = (Collection<?>) value;
            List<Object> copy = null;
            int position = 0;
            for (Object item : coll) {
                Object returned = serializeField(objectMapper, item);
                if (returned != item) {
                    if (copy == null) {
                        copy = new ArrayList<Object>(coll);
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
                Object returned = serializeField(objectMapper, array[i]);
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
            BsonObjectGenerator generator = new BsonObjectGenerator();
            try {
                objectMapper.writeValue(generator, value);
            } catch (JsonMappingException e) {
                throw new MongoJsonMappingException(e);
            } catch (IOException e) {
                throw new RuntimeException("Somehow got an IOException writing to memory", e);
            }
            return generator.getValue();
        }
    }

    public static DBObject serializeDBUpdate(Map<String, Map<String, UpdateOperationValue>> update,
                                             ObjectMapper objectMapper, JavaType javaType) {
        SerializerProvider serializerProvider = JacksonAccessor.getSerializerProvider(objectMapper);
        BasicDBObject dbObject = new BasicDBObject();
        for (Map.Entry<String, Map<String, UpdateOperationValue>> op : update.entrySet()) {
            BasicDBObject opObject = new BasicDBObject();
            for (Map.Entry<String, UpdateOperationValue> field : op.getValue().entrySet()) {
                Object value;
                if (field.getValue().requiresSerialization()) {
                    JsonSerializer serializer = findUpdateSerializer(field.getValue().isTargetCollection(),
                            field.getKey(), objectMapper, serializerProvider, javaType);
                    if (serializer != null) {
                        value = serializeUpdateField(field.getValue(), serializer, serializerProvider, op.getKey(),
                                field.getKey());
                    } else {
                        // Try default serializers
                        value = serializeField(objectMapper, field.getValue().getValue());
                    }
                } else {
                    value = field.getValue().getValue();
                }
                if (op.getKey().equals("$addToSet") && field.getValue() instanceof MultiUpdateOperationValue) {
                    // Add to set needs $each for multi values
                    opObject.put(field.getKey(), new BasicDBObject("$each", value));
                } else {
                    opObject.put(field.getKey(), value);
                }
            }
            dbObject.append(op.getKey(), opObject);
        }
        return dbObject;
    }

    private static Object serializeUpdateField(UpdateOperationValue value, JsonSerializer serializer,
                                               SerializerProvider serializerProvider,
                                               String op, String field) {
        if (value instanceof MultiUpdateOperationValue) {
            List<Object> results = new ArrayList<Object>();
            for (Object item : ((MultiUpdateOperationValue) value).getValues()) {
                results.add(serializeUpdateField(item, serializer, serializerProvider, op, field));
            }
            return results;
        } else {
            return serializeUpdateField(value.getValue(), serializer, serializerProvider, op, field);
        }
    }

    private static Object serializeUpdateField(Object value, JsonSerializer serializer, SerializerProvider serializerProvider,
                                               String op, String field) {
        BsonObjectGenerator objectGenerator = new BsonObjectGenerator();
        try {
            serializer.serialize(value, objectGenerator, serializerProvider);
        } catch (IOException e) {
            throw new MongoJsonMappingException("Error serializing value in DBUpdate operation " +
                    op + " field " + field, e);
        }
        return objectGenerator.getValue();
    }

    private static JsonSerializer<?> findUpdateSerializer(boolean targetIsCollection, String fieldPath,
                                                          ObjectMapper objectMapper, SerializerProvider serializerProvider,
                                                          JavaType javaType) {
        JsonSerializer serializer = JacksonAccessor.findValueSerializer(objectMapper, serializerProvider, javaType);
        if (serializer instanceof BeanSerializerBase) {
            JsonSerializer<?> fieldSerializer = serializer;
            // Iterate through the components of the field name
            String[] fields = fieldPath.split("\\.");
            for (String field : fields) {
                if (fieldSerializer == null) {
                    // We don't have a field serializer to look up the field on, so give up
                    return null;
                }
                if (field.equals("$")) {
                    // The current serializer must be a collection
                    if (fieldSerializer instanceof ContainerSerializer) {
                        JsonSerializer contentSerializer = ((ContainerSerializer) fieldSerializer).getContentSerializer();
                        if (contentSerializer == null) {
                            // Work it out
                            JavaType contentType = ((ContainerSerializer) fieldSerializer).getContentType();
                            if (contentType != null) {
                                contentSerializer = JacksonAccessor.findValueSerializer(objectMapper, serializerProvider, contentType);
                            }
                        }
                        fieldSerializer = contentSerializer;
                    } else {
                        // Give up, don't attempt to serialise it
                        return null;
                    }
                } else if (fieldSerializer instanceof BeanSerializerBase) {
                    BeanPropertyWriter writer = JacksonAccessor.findPropertyWriter((BeanSerializerBase) serializer, field);
                    if (writer != null) {
                        fieldSerializer = writer.getSerializer();
                        if (fieldSerializer == null) {
                            // Do a generic lookup
                            fieldSerializer = JacksonAccessor.findValueSerializer(objectMapper, serializerProvider, writer.getType());
                        }
                    } else {
                        // Give up
                        return null;
                    }
                } else if (fieldSerializer instanceof MapSerializer) {
                    fieldSerializer = ((MapSerializer) fieldSerializer).getContentSerializer();
                } else {
                    // Don't know how to find what the serialiser for this field is, return null
                }
            }
            // Now we have a serializer for the field, see if we're supposed to be serialising for a collection
            if (targetIsCollection) {
                if (fieldSerializer instanceof ContainerSerializer) {
                    fieldSerializer = ((ContainerSerializer) fieldSerializer).getContentSerializer();
                } else if (fieldSerializer instanceof ObjectIdSerializer) {
                    // Special case for ObjectIdSerializer, leave as is, the ObjectIdSerializer handles both single
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
