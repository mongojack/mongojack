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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.mock.MockObject;

public class TestJacksonMongoCollection extends MongoDBTestBase {
    private JacksonMongoCollection<MockObject> coll;

    @Before
    public void setup() throws Exception {
        coll = JacksonMongoCollection.<MockObject> builder().build(getMongoCollection("testJacksonMongoCollection"), MockObject.class);
    }

    @Test
    public void testQuery() {
        MockObject o1 = new MockObject("1", "ten", 10);
        MockObject o2 = new MockObject("2", "ten", 10);
        coll.insert(o1, o2, new MockObject("twenty", 20));

        List<MockObject> results = coll
                .find(new Document("string", "ten")).into(new ArrayList<>());
        assertThat(results, hasSize(2));
        assertThat(results, contains(o1, o2));
    }

    @Test
    public void testRemove() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        MockObject object = new MockObject("1", "twenty", 20);
        coll.insert(object);

        coll.remove(new Document("string", "ten"));

        List<MockObject> remaining = coll.find().into(new ArrayList<>());
        assertThat(remaining, Matchers.hasSize(1));
        assertThat(remaining, contains(object));
    }

    @Test
    public void testRemoveById() {
        coll.insert(new MockObject("id1", "ten", 10));
        coll.insert(new MockObject("id2", "ten", 100));
        MockObject object = new MockObject("id3", "twenty", 20);
        coll.insert(object);

        coll.removeById("id3");

        List<MockObject> remaining = coll.find().into(new ArrayList<>());
        assertThat(remaining, Matchers.hasSize(2));
        assertThat(remaining, not(contains(object)));
    }

    @Test
    public void testFindAndModifyWithBuilder() {
        coll.insert(new MockObject("id1", "ten", 10));
        coll.insert(new MockObject("id2", "ten", 10));

        MockObject result1 = coll.findAndModify(DBQuery.is("_id", "id1"), null,
                null, DBUpdate.set("integer", 20)
                        .set("string", "twenty"), true, false);
        assertThat(result1.integer, equalTo(20));
        assertThat(result1.string, equalTo("twenty"));

        MockObject result2 = coll.findAndModify(DBQuery.is("_id", "id2"), null,
                null, DBUpdate.set("integer", 30)
                        .set("string", "thirty"), true, false);
        assertThat(result2.integer, equalTo(30));
        assertThat(result2.string, equalTo("thirty"));

        coll.removeById("id1");
        coll.removeById("id2");

    }

    @Test
    public void testFindAndModifyWithParameterizedType() {
        coll.insert(new MockObject("ten", 10));

        MockObject init = coll.findOne(DBQuery.is("string", "ten").is(
                "integer", 10));

        MockObject result1 = coll.findAndModify(DBQuery.is("_id", init._id),
                null, null, new MockObject("twenty", 20), true, true);
        assertThat(result1.integer, equalTo(20));
        assertThat(result1.string, equalTo("twenty"));

        MockObject result2 = coll.findAndModify(DBQuery.is("_id", "id2"), null,
                null, new MockObject("id2", "thirty", 30), true, true);
        assertThat(result2._id, equalTo("id2"));
        assertThat(result2.integer, equalTo(30));
        assertThat(result2.string, equalTo("thirty"));

        coll.removeById("id1");
        coll.removeById("id2");
    }

}
