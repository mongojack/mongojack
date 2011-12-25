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

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Base class for unit tests that run against MongoDB.  Assumes there is a MongoDB instance listening on the default
 * port on localhost, and that we can do whatever we want to a database called "unittest".
 */
@RunWith(MongoDBTestCaseRunner.class)
public abstract class MongoDBTestBase {
    private static final Random rand = new Random();
    private boolean useStreamParser = true;

    protected Mongo mongo;
    protected DB db;
    private Set<String> collections;

    @Before
    public void connectToDb() throws Exception {
        mongo = new Mongo();
        db = mongo.getDB("unittest");
        collections = new HashSet<String>();
    }

    @After
    public void disconnectFromDb() throws Exception {
        for (String collection : collections) {
            db.getCollection(collection).drop();
        }
        mongo.close();
    }

    /**
     * Get a collection with the given name, and store it, so that it will be dropped in clean up
     *
     * @param name The name of the collection
     * @return The collection
     */
    protected DBCollection getCollection(String name) {
        collections.add(name);
        return db.getCollection(name);
    }

    /**
     * Get a collection with a random name.  Should grant some degree of isolation from tests running in parallel.
     *
     * @return The collection
     */
    protected DBCollection getCollection() {
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
        return getCollection(name.toString());
    }

    protected <T, K> JacksonDBCollection<T, K> getCollection(Class<T> type, Class<K> keyType) {
        JacksonDBCollection<T, K> collection = JacksonDBCollection.wrap(getCollection(), type, keyType);
        if (useStreamParser) {
            collection.enable(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION);
        } else {
            collection.disable(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION);
        }
        return collection;
    }

    protected <T, K> JacksonDBCollection<T, K> getCollection(Class<T> type, Class<K> keyType, String collectionName) {
        JacksonDBCollection<T, K> coll = JacksonDBCollection.wrap(getCollection(collectionName), type, keyType);
        if (useStreamParser) {
            coll.enable(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION);
        } else {
            coll.disable(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION);
        }
        return coll;
    }

    public void setUseStreamParser(boolean useStreamParser) {
        this.useStreamParser = useStreamParser;
    }
}
