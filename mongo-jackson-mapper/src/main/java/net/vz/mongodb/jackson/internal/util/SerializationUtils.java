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
package net.vz.mongodb.jackson.internal.util;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.MongoJsonMappingException;
import net.vz.mongodb.jackson.internal.object.BsonObjectGenerator;
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

}
