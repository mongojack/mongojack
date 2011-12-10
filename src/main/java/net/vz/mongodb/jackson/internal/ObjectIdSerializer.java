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
package net.vz.mongodb.jackson.internal;

import net.vz.mongodb.jackson.DBRef;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.util.Collection;

/**
 * Serializer for object ids, serialises strings or byte arrays to an ObjectId class
 *
 * @author James Roper
 * @since 1.0
 */
public class ObjectIdSerializer extends JsonSerializer {
    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        if (value instanceof Iterable) {
            jgen.writeStartArray();
            for (Object item : (Iterable) value) {
                jgen.writeObject(serialiseObject(value));
            }
            jgen.writeEndArray();
        } else {
            jgen.writeObject(serialiseObject(value));
        }
    }

    private Object serialiseObject(Object value) throws JsonMappingException {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return new ObjectId((String) value);
        } else if (value instanceof byte[]) {
            return new ObjectId((byte[]) value);
        } else if (value instanceof DBRef) {
            DBRef dbRef = (DBRef) value;
            Object id = serialiseObject(dbRef.getId());
            if (id == null) {
                return null;
            }
            return new com.mongodb.DBRef(null, dbRef.getCollectionName(), id);
        } else if (value instanceof ObjectId) {
            // Putting @ObjectId annotation on an ObjectId is redundant, but that doesn't mean we shouldn't support it
            return value;
        } else {
            throw new JsonMappingException("Cannot deserialise object of type " + value.getClass() + " to ObjectId");
        }
    }
}
