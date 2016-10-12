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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mongojack.DBQuery.Query;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mongodb.DBCollection;

public class TestQuerySerialization extends MongoDBTestBase {

    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setUp() {
        coll = getCollection(MockObject.class, String.class);
    }

    @Test
    public void testSimpleEquals() {
        coll.save(new MockObject());
        String id = coll.findOne().id;
        // with DBCursor
        assertNotNull(coll.find().is("_id", id).next());
        // with DBQuery
        assertNotNull(coll.findOne(DBQuery.is("_id", id)));
    }

    @Test
    public void testIn() {
        coll.save(new MockObject());
        String id = coll.findOne().id;
        // with DBCursor
        assertThat(
                coll.find()
                        .in("_id", id, new org.bson.types.ObjectId().toString())
                        .toArray(), hasSize(1));
        // with DBQuery
        assertThat(
                coll.find(DBQuery.in("_id", id, new org.bson.types.ObjectId().toString()))
                        .toArray(), hasSize(1));
    }

    @Test @Ignore // This test needs to be fixed
    public void testIn_collectionOfStrings() {
        DBCollection c1 = getCollection("blah_" + Math.round(Math.random() * 10000d));
        JacksonDBCollection<MockObjectWithList, String> c2 = JacksonDBCollection.wrap(c1, MockObjectWithList.class, String.class);
        List<String> x = new ArrayList<String>();
        x.add("a");
        x.add("b");
        Query q = DBQuery.in("simpleList", x);
        c2.find(q);
    }

    @Test
    public void testLessThan() {
        MockObject o = new MockObject();
        o.i = 5;
        coll.save(o);
        // Ensure that the serializer actually worked
        assertThat((Integer) coll.getDbCollection().findOne().get("i"),
                equalTo(15));
        // with DBCursor
        assertThat(coll.find().lessThan("i", 12).toArray(), hasSize(1));
        // with DBQuery
        assertThat(coll.find(DBQuery.lessThan("i", 12)).toArray(), hasSize(1));
    }

    @Test
    public void testAnd() {
        MockObject o = new MockObject();
        o.i = 5;
        coll.save(o);
        // Ensure that the serializer actually worked
        // with DBCursor
        assertThat(
                coll.find()
                        .and(DBQuery.lessThan("i", 12),
                                DBQuery.greaterThan("i", 4)).toArray(),
                hasSize(1));
        assertThat(
                coll.find()
                        .and(DBQuery.lessThan("i", 12),
                                DBQuery.greaterThan("i", 9)).toArray(),
                hasSize(0));
        // with DBQuery
        assertThat(
            coll.find(DBQuery.and(DBQuery.lessThan("i", 12), DBQuery.greaterThan("i", 4))).toArray(),
                hasSize(1));
        assertThat(
            coll.find(DBQuery.and(DBQuery.lessThan("i", 12), DBQuery.greaterThan("i", 9))).toArray(),
                hasSize(0));
    }

    @Test
    public void testAll() {
        MockObject o = new MockObject();
        MockObject o1 = new MockObject();
        o1.id = new org.bson.types.ObjectId().toString();
        o.items = Arrays.asList(o1);
        coll.save(o);

        // Ensure that the serializer actually worked
        // with DBCursor
        assertThat(coll.find().all("items", o1).toArray(), hasSize(1));
        // with DBQuery
        assertThat(coll.find(DBQuery.all("items", o1)).toArray(), hasSize(1));
    }

    @Test
    public void testList() {
        MockObject o = new MockObject();
        MockObject o1 = new MockObject();
        o1.id = new org.bson.types.ObjectId().toString();
        o.items = Arrays.asList(o1);
        coll.save(o);

        // with DBCursor
        assertThat(coll.find().is("items._id", o1.id).toArray(), hasSize(1));
        // with DBQuery
        assertThat(coll.find(DBQuery.is("items._id", o1.id)).toArray(), hasSize(1));
    }

    @Test
    public void testArrayEquals() {
        MockObject o = new MockObject();
        MockObject o1 = new MockObject();
        o1.id = new org.bson.types.ObjectId().toString();
        o.items = Arrays.asList(o1);
        coll.save(o);

        // with DBCursor
        assertThat(coll.find().is("items", Arrays.asList(o1)).toArray(),
                hasSize(1));
        // with DBQuery
        assertThat(coll.find(DBQuery.is("items", Arrays.asList(o1))).toArray(),
                hasSize(1));
    }

    static class MockObject {
        @ObjectId
        @Id
        public String id;

        @JsonSerialize(using = PlusTenSerializer.class)
        @JsonDeserialize(using = MinusTenDeserializer.class)
        public int i;

        public List<MockObject> items;
    }

    static class MockObjectWithList {

        @Id
        private String _id;

        @JsonProperty
        private List<String> simpleList;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public List<String> getSimpleList() {
            return simpleList;
        }

        public void setSimpleList(List<String> simpleList) {
            this.simpleList = simpleList;
        }
    }

    static class PlusTenSerializer extends JsonSerializer<Integer> {
        @Override
        public void serialize(Integer value, JsonGenerator jgen,
                SerializerProvider provider) throws IOException,
                JsonProcessingException {
            jgen.writeNumber(value + 10);
        }
    }

    static class MinusTenDeserializer extends JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            return jp.getValueAsInt() - 10;
        }
    }

}
