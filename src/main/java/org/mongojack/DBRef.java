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
package org.mongojack;

import org.bson.conversions.Bson;

/**
 * A database reference object
 * 
 * @author James Roper
 * @since 1.2
 */
public class DBRef<T, K> {
    private final K id;
    private final Class<T> objectClass;
    private final String collectionName;
    private final String databaseName;

    /**
     * Construct a new database reference with the given id and collection name
     *  @param id The id of the database reference to construct
     * @param objectClass
     * @param collectionName The name of the collection
     */
    public DBRef(K id, final Class<T> objectClass, String collectionName, String databaseName) {
        this.id = id;
        this.objectClass = objectClass;
        this.collectionName = collectionName;
        this.databaseName = databaseName;
    }

    /**
     * Construct a new database reference with the given id and type. The type must be annotated with
     * {@link MongoCollection}, so that the name can be worked out.
     * 
     * @param id The id of the database reference to construct
     * @param type The type of the object
     * @throws MongoJsonMappingException If no MongoCollection annotation is found on the type
     */
    public DBRef(K id, Class<T> type) throws MongoJsonMappingException {
        this.id = id;
        this.objectClass = type;
        MongoCollection collection = type.getAnnotation(MongoCollection.class);
        if (collection == null) {
            throw new MongoJsonMappingException("Only types that have the @MongoCollection annotation on them can be used with this constructor");
        }
        this.collectionName = collection.name();
        this.databaseName = null;
    }

    /**
     * Get the ID of this object
     * 
     * @return The ID of this object
     */
    public K getId() {
        return id;
    }

    public Class<T> getObjectClass() {
        return objectClass;
    }

    /**
     * Get the name of the collection this object lives in
     * 
     * @return The name of the collection this object lives in
     */
    public String getCollectionName() {
        return collectionName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Fetch the object. Should only be called for references that have been returned by the mapper, will return null
     * otherwise.
     * <p>
     * Deserialisation is done by the ObjectMapper that fetched the object that this reference belongs to.
     * 
     * @return If this DBRef was returned by the mapper, the referenced object, if it exists, or null
     */
    public T fetch() {
        return null;
    }

    /**
     * Fetch only the given fields of the object. Should only be called for references that have been returned by the
     * mapper, will return null otherwise.
     * <p>
     * Deserialisation is done by the ObjectMapper that fetched the object that this reference belongs to.
     * <p>
     * Unlike the <code>fetch()</code> method, calls to this are not cached.
     * 
     * @param fields The fields to fetch
     * @return If this DBRef was returned by the mapper, the referenced object, if it exists, or null
     */
    public T fetch(Bson fields) {
        return null;
    }
}
