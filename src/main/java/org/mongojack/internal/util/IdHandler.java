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

import java.io.IOException;

import org.mongojack.internal.object.BsonObjectGenerator;
import org.mongojack.internal.object.BsonObjectTraversingParser;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;

/**
 * Handler for ids. Converts them between the objects type and the database type
 * 
 * @author James Roper
 * @since 1.0
 */
public interface IdHandler<K, D> {

    /**
     * Convert the given database id to the java objects id
     * 
     * @param dbId
     *            The database id to convert from
     * @return The converted id
     */
    K fromDbId(D dbId);

    /**
     * Convert the given java object id to the databases id
     * 
     * @param id
     *            The java object id to convert from
     * @return The converted database id
     */
    D toDbId(K id);

    public static class NoopIdHandler<K> implements IdHandler<K, K> {
        @Override
        public K fromDbId(K dbId) {
            return dbId;
        }

        @Override
        public K toDbId(K id) {
            return id;
        }
    }

    public static class JacksonIdHandler<K, D> implements IdHandler<K, D> {
        private final JsonSerializer<K> jsonSerializer;
        private final JsonDeserializer<K> jsonDeserializer;
        private final ObjectMapper objectMapper;

        public JacksonIdHandler(JsonSerializer<K> jsonSerializer,
                JsonDeserializer<K> jsonDeserializer, ObjectMapper objectMapper) {
            this.jsonSerializer = jsonSerializer;
            this.jsonDeserializer = jsonDeserializer;
            this.objectMapper = objectMapper;
        }

        /**
         * Convert the given database id to the java objects id
         * 
         * @param dbId
         *            The database id to convert from
         * @return The converted id
         */
        @Override
        public K fromDbId(D dbId) {
            try {
                return jsonDeserializer
                        .deserialize(new BsonObjectTraversingParser(null, dbId,
                                objectMapper), null);
            } catch (IOException e) {
                throw new MongoException("Error deserializing ID", e);
            }
        }

        /**
         * Convert the given java object id to the databases id
         * 
         * @param id
         *            The java object id to convert from
         * @return The converted database id
         */
        @Override
        public D toDbId(K id) {
            BsonObjectGenerator generator = new BsonObjectGenerator();
            try {
                jsonSerializer.serialize(id, generator, null);
            } catch (IOException e) {
                throw new MongoException("Error serializing ID", e);
            }
            return (D) generator.getValue();
        }
    }
}
