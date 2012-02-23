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

import com.mongodb.*;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import net.vz.mongodb.jackson.mock.MockObject;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class TestJacksonDBCollection extends MongoDBTestBase {
    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setup() throws Exception {
        coll = getCollection(MockObject.class, String.class);
    }

    @Test
    public void testQuery() {
        MockObject o1 = coll.insert(new MockObject("ten", 10)).getSavedObject();
        MockObject o2 = coll.insert(new MockObject("ten", 100)).getSavedObject();
        coll.insert(new MockObject("twenty", 20));

        List<MockObject> results = coll.find(new BasicDBObject("string", "ten")).toArray();
        assertThat(results, hasSize(2));
        assertThat(results, contains(o1, o2));
    }

    @Test
    public void testQueryWithJavaObject() {
        MockObject o1 = coll.insert(new MockObject("ten", 10)).getSavedObject();
        MockObject o2 = coll.insert(new MockObject("ten", 100)).getSavedObject();
        coll.insert(new MockObject("twenty", 20));

        List<MockObject> results = coll.find(new MockObject("ten", null)).toArray();
        assertThat(results, hasSize(2));
        assertThat(results, contains(o1, o2));
    }

    @Test
    public void testQueryWithLimitedKeys() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        coll.insert(new MockObject("twenty", 20));

        List<MockObject> results = coll.find(new BasicDBObject("string", "ten"),
                new BasicDBObject("string", "something not null")).toArray();
        assertThat(results, hasSize(2));
        assertThat(results.get(0).integer, nullValue());
        assertThat(results.get(0).string, equalTo("ten"));
        assertThat(results.get(1).integer, nullValue());
        assertThat(results.get(1).string, equalTo("ten"));
    }

    @Test
    public void testQueryWithLimitedKeysFromJavaObject() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        coll.insert(new MockObject("twenty", 20));

        List<MockObject> results = coll.find(new MockObject("ten", null),
                new MockObject("something not null", null)).toArray();
        assertThat(results, hasSize(2));
        assertThat(results.get(0).integer, nullValue());
        assertThat(results.get(0).string, equalTo("ten"));
        assertThat(results.get(1).integer, nullValue());
        assertThat(results.get(1).string, equalTo("ten"));
    }

    @Test
    public void testRemove() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        MockObject object = coll.insert(new MockObject("twenty", 20)).getSavedObject();

        coll.remove(new BasicDBObject("string", "ten"));

        List<MockObject> remaining = coll.find().toArray();
        assertThat(remaining, Matchers.hasSize(1));
        assertThat(remaining, contains(object));
    }

    @Test
    public void testRemoveByJavaObject() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        MockObject object = coll.insert(new MockObject("twenty", 20)).getSavedObject();

        coll.remove(new MockObject("ten", null));

        List<MockObject> remaining = coll.find().toArray();
        assertThat(remaining, Matchers.hasSize(1));
        assertThat(remaining, contains(object));
    }

    @Test
    public void testRemoveByJavaObjectWithId() {
        coll.insert(new MockObject("id1", "ten", 10));
        coll.insert(new MockObject("id2", "ten", 100));
        MockObject object = coll.insert(new MockObject("id3", "twenty", 20)).getSavedObject();

        MockObject toRemove = new MockObject("id3", null, null);

        coll.remove(toRemove);

        List<MockObject> remaining = coll.find().toArray();
        assertThat(remaining, Matchers.hasSize(2));
        assertThat(remaining, not(contains(object)));
    }

    @Test
    public void testRemoveById() {
        coll.insert(new MockObject("id1", "ten", 10));
        coll.insert(new MockObject("id2", "ten", 100));
        MockObject object = coll.insert(new MockObject("id3", "twenty", 20)).getSavedObject();

        coll.removeById("id3");

        List<MockObject> remaining = coll.find().toArray();
        assertThat(remaining, Matchers.hasSize(2));
        assertThat(remaining, not(contains(object)));
    }

}
