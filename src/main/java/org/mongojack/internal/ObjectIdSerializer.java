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
package org.mongojack.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.types.ObjectId;
import org.mongojack.DBRef;

import java.io.IOException;

/**
 * Serializer for object ids, serialises strings or byte arrays to an ObjectId
 * class
 *
 * @author James Roper
 * @since 1.0
 */
public class ObjectIdSerializer extends EmbeddedObjectSerializer {
    @Override
    public void serialize(
        Object value, JsonGenerator jgen,
        SerializerProvider provider
    ) throws IOException,
        JsonProcessingException {
        if (value instanceof Iterable) {
            jgen.writeStartArray();
            for (Object item : (Iterable) value) {
                writeObject(serialiseObject(item, jgen), jgen);
            }
            jgen.writeEndArray();
        } else {
            writeObject(serialiseObject(value, jgen), jgen);
        }
    }

    private void writeObject(Object value, JsonGenerator jgen)
        throws IOException, JsonMappingException {
        if (value instanceof ObjectId) {
            writeEmbeddedObject(value, jgen);
        } else {
            jgen.writeObject(value);
        }
    }

    private Object serialiseObject(Object value, JsonGenerator jgen) throws JsonMappingException {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return new ObjectId((String) value);
        } else if (value instanceof byte[]) {
            return new ObjectId((byte[]) value);
        } else if (value instanceof DBRef) {
            DBRef dbRef = (DBRef) value;
            Object id = serialiseObject(dbRef.getId(), jgen);
            if (id == null) {
                return null;
            }
            return new com.mongodb.DBRef(dbRef.getDatabaseName(), dbRef.getCollectionName(), id);
        } else if (value instanceof ObjectId) {
            return value;
        } else {
            throw JsonMappingException.from(jgen, "Cannot deserialise object of type " + value.getClass() + " to ObjectId");
        }
    }
}
