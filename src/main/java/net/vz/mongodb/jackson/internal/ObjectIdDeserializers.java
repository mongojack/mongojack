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

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;

import java.io.IOException;

/**
 * Deserialiser for object ids that deserialises into String
 *
 * @author James Roper
 * @since 1.0
 */
public class ObjectIdDeserializers {

    public static class ToStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            Object object = jp.getEmbeddedObject();
            if (object == null) {
                return null;
            } else if (object instanceof ObjectId) {
                return object.toString();
            } else {
                throw ctxt.instantiationException(String.class,
                        "Expected an ObjectId to deserialise to string, but found " + object.getClass());
            }
        }
    }

    public static class ToByteArrayDeserializer extends JsonDeserializer<byte[]> {
        @Override
        public byte[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            Object object = jp.getEmbeddedObject();
            if (object == null) {
                return null;
            } else if (object instanceof ObjectId) {
                return ((ObjectId) object).toByteArray();
            } else {
                throw ctxt.instantiationException(String.class,
                        "Expected an ObjectId to deserialise to byte array, but found " + object.getClass());
            }
        }
    }

    public static class ToObjectIdDeserializer extends JsonDeserializer<ObjectId> {
        @Override
        public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            Object object = jp.getEmbeddedObject();
            if (object == null) {
                return null;
            } else if (object instanceof ObjectId) {
                return (ObjectId) object;
            } else {
                throw ctxt.instantiationException(String.class,
                        "Expected an ObjectId, but found " + object.getClass());
            }
        }
    }
}
