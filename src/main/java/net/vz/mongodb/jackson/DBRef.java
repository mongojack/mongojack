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
package net.vz.mongodb.jackson;

import org.codehaus.jackson.annotate.JsonCreator;

/**
 * A database reference object
 *
 * @author James Roper
 * @since 1.2
 */
public class DBRef<T, K> {
    private final K id;
    private final String collectionName;

    /**
     * Construct a new database reference with the given id and collection name
     *
     * @param id             The id of the database reference to construct
     * @param collectionName The name of the collection
     */
    public DBRef(K id, String collectionName) {
        this.id = id;
        this.collectionName = collectionName;
    }

    /**
     * Construct a new database reference with the given id and type.  The type must be annotated with
     * {@link MongoCollection}, so that the name can be worked out.
     *
     * @param id             The id of the database reference to construct
     * @param type           The type of the object
     * @throws MongoJsonMappingException If no MongoCollection annotation is found on the type
     */
    public DBRef(K id, Class<T> type) throws MongoJsonMappingException {
        this.id = id;
        MongoCollection collection = type.getAnnotation(MongoCollection.class);
        if (collection == null) {
            throw new MongoJsonMappingException("Only types that have the @MongoCollection annotation on them can be used with this constructor");
        }
        this.collectionName = collection.name();
    }

    /**
     * Get the ID of this object
     *
     * @return The ID of this object
     */
    public K getId() {
        return id;
    }

    /**
     * Get the name of the collection this object lives in
     *
     * @return The name of the collection this object lives in
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Fetch the object.  Should only be called for references that have been returned by the mapper, will return null
     * otherwise.
     * <p/>
     * Deserialisation is done by the ObjectMapper that fetched the object that this reference belongs to.
     *
     * @return If this DBRef was returned by the mapper, the referenced object, if it exists, or null
     */
    public T fetch() {
        return null;
    }
}
