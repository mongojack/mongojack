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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mongojack.testing.DbManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Base class for unit tests that run against MongoDB. Assumes there is a
 * MongoDB instance listening on the default port on localhost, and that we can
 * do whatever we want to a database called "unittest".
 */
@RunWith(MongoDBTestCaseRunner.class)
public abstract class MongoDBTestBase {

    private static final Random rand = new Random();
    private static final String dbHostKey = "MONGOJACK_TESTDB_HOST";
    private static final Map<String, String> environment = System.getenv();

    protected MongoClient mongo;
    protected MongoDatabase db;
    private Set<String> collections;
    protected UuidRepresentation uuidRepresentation = UuidRepresentation.JAVA_LEGACY;

    @Before
    public void connectToDb() {
        if (environment.containsKey(dbHostKey)) {
            mongo = MongoClients.create(
                MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(String.format("mongodb://%s", environment.get(dbHostKey))))
                    .uuidRepresentation(uuidRepresentation)
                    .build()
            );
        } else {
            mongo = MongoClients.create(
                MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(String.format("mongodb://localhost:%d", DbManager.PORT)))
                    .uuidRepresentation(uuidRepresentation)
                    .build()
            );
        }

        String testDatabaseName = "unittest";
        db = mongo.getDatabase(testDatabaseName);
        collections = new HashSet<>();
    }

    @After
    public void disconnectFromDb() {
        for (String collection : collections) {
            db.getCollection(collection).drop();
        }
        mongo.close();
    }

    /**
     * Get a collection with the given name, and store it, so that it will be
     * dropped in clean up
     *
     * @param name The name of the collection
     * @return The collection
     */
    protected <T> MongoCollection<T> getMongoCollection(String name, Class<T> documentClass) {
        collections.add(name);
        return db.getCollection(name, documentClass);
    }

    /**
     * Get a collection with a random name. Should grant some degree of
     * isolation from tests running in parallel.
     *
     * @return The collection
     */
    protected <T> MongoCollection<T> getMongoCollection(Class<T> documentClass) {
        StringBuilder name = new StringBuilder();
        while (name.length() < 8) {
            char letter = (char) rand.nextInt(26);
            if (rand.nextBoolean()) {
                letter += 'a';
            } else {
                letter += 'A';
            }
            name.append(letter);
        }
        return getMongoCollection(name.toString(), documentClass);
    }

    protected <T> JacksonMongoCollection<T> configure(
        JacksonMongoCollection<T> collection
    ) {
        return collection;
    }

    protected <T> JacksonMongoCollection<T> getCollection(Class<T> type) {
        return configure(
            JacksonMongoCollection.builder()
                .build(getMongoCollection(type), type, uuidRepresentation)
        );
    }

    @SuppressWarnings("SameParameterValue")
    protected <T> JacksonMongoCollection<T> getCollectionWithView(Class<T> type, Class<?> view) {
        return configure(
            JacksonMongoCollection.builder()
                .withView(view)
                .build(getMongoCollection(type), type, uuidRepresentation)
        );
    }

    protected <T> JacksonMongoCollection<T> getCollection(
        Class<T> type,
        String collectionName
    ) {
        return configure(
            JacksonMongoCollection.builder()
                .build(getMongoCollection(collectionName, type), type, uuidRepresentation)
        );
    }

    protected <T> JacksonMongoCollection<T> getCollection(
        Class<T> type,
        ObjectMapper mapper
    ) {
        return configure(
            JacksonMongoCollection.builder()
                .withObjectMapper(mapper)
                .build(getMongoCollection(type), type, uuidRepresentation)
        );
    }

    protected MongoCollection<Document> getUnderlyingCollection(JacksonMongoCollection<?> coll) {
        return getMongoCollection(coll.getName(), Document.class);
    }

}
