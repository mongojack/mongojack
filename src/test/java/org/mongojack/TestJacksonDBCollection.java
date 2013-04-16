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
package org.mongojack;

import com.mongodb.*;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.mock.MockObject;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;

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
        MockObject o1 = new MockObject("1", "ten", 10);
        MockObject o2 = new MockObject("2", "ten", 10);
        coll.insert(o1, o2, new MockObject("twenty", 20));

        List<MockObject> results = coll.find(new BasicDBObject("string", "ten")).toArray();
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
    public void testRemove() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        MockObject object = new MockObject("1", "twenty", 20);
        coll.insert(object);

        coll.remove(new BasicDBObject("string", "ten"));

        List<MockObject> remaining = coll.find().toArray();
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

        List<MockObject> remaining = coll.find().toArray();
        assertThat(remaining, Matchers.hasSize(2));
        assertThat(remaining, not(contains(object)));
    }
    
    @Test
    public void testFindAndModifyWithBuilder(){
    	coll.insert(new MockObject("id1", "ten", 10));
    	coll.insert(new MockObject("id2", "ten", 10));
    	
    	MockObject result1 = coll.findAndModify(DBQuery.is("_id", "id1"), null, null, false, DBUpdate.set("integer", 20).set("string", "twenty"), true, false);
    	assertThat(result1.integer, equalTo(20));
    	assertThat(result1.string, equalTo("twenty"));
    	
    	MockObject result2 = coll.findAndModify(DBQuery.is("_id", "id2"), null, null, false, DBUpdate.set("integer", 30).set("string", "thirty"), true, false);
    	assertThat(result2.integer, equalTo(30));
    	assertThat(result2.string, equalTo("thirty"));
    	
    	coll.removeById("id1");
    	coll.removeById("id2");
    	
    }

}
