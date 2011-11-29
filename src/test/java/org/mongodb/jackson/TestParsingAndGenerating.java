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
package org.mongodb.jackson;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mongodb.jackson.mock.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for parser and generator
 */
public class TestParsingAndGenerating {
    private Mongo mongo;
    private DB db;
    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setup() throws Exception {
        mongo = new Mongo();
        db = mongo.getDB("test");
        coll = JacksonDBCollection.wrap(db.createCollection("mockObject", new BasicDBObject()),
                MockObject.class, String.class);
    }

    @After
    public void tearDown() throws Exception {
        coll.getDbCollection().drop();
        mongo.close();
    }

    @Test
    public void testInsertNoId() {
        MockObject object = new MockObject();
        WriteResult<MockObject, String> result = coll.insert(object);
        assertNotNull(result.getSavedObject()._id);
    }

    @Test
    public void testInsertRetrieveAllEmpty() {
        MockObject object = new MockObject();
        object._id = "1";
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object, result);
    }

    @Test
    public void testInsertRetrieveString() {
        MockObject object = new MockObject();
        object.string = "a string";
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object.string, result.string);
    }

    @Test
    public void testInsertRetrieveInteger() {
        MockObject object = new MockObject();
        object.integer = 10;
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object.integer, result.integer);
    }

    @Test
    public void testInsertRetrieveLong() {
        MockObject object = new MockObject();
        object.longs = 10L;
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object.longs, result.longs);
    }

    @Test
    @Ignore("BSON doesn't yet know how to handle BigInteger")
    public void testInsertRetrieveBigInteger() {
        MockObject object = new MockObject();
        object.bigInteger = BigInteger.valueOf(100);
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object.bigInteger, result.bigInteger);
    }

    @Test
    public void testInsertRetrieveFloat() {
        MockObject object = new MockObject();
        object.floats = 3.0f;
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object.floats, result.floats);
    }

    @Test
    public void testInsertRetrieveDouble() {
        MockObject object = new MockObject();
        object.doubles = 4.65;
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object.doubles, result.doubles);
    }

    @Test
    @Ignore("BSON doesn't yet know how to handle BigDecimal")
    public void testInsertRetrieveBigDecimal() {
        MockObject object = new MockObject();
        object.bigDecimal = BigDecimal.valueOf(4, 6);
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object.bigDecimal, result.bigDecimal);
    }

    @Test
    public void testInsertRetrieveBoolean() {
        MockObject object = new MockObject();
        object.booleans = true;
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object.booleans, result.booleans);
    }

    @Test
    public void testInsertRetrieveEmptyList() {
        MockObject object = new MockObject();
        object.simpleList = Collections.emptyList();
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object.simpleList, result.simpleList);
    }

    @Test
    public void testInsertRetrievePopulatedSimpleList() {
        MockObject object = new MockObject();
        object.simpleList = Arrays.asList("1", "2");
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object.simpleList, result.simpleList);
    }

    @Test
    public void testInsertRetrievePopulatedComplexList() {
        MockObject object = new MockObject();
        MockEmbeddedObject o1 = new MockEmbeddedObject();
        o1.value = "o1";
        MockEmbeddedObject o2 = new MockEmbeddedObject();
        o2.value = "o2";
        object.complexList = Arrays.asList(o1, o2);
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object.complexList, result.complexList);
    }

    @Test
    public void testInsertRetrieveEmbeddedObject() {
        MockObject object = new MockObject();
        object.object = new MockEmbeddedObject();
        object.object.value = "blah";
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object.object, result.object);
    }

    @Test
    public void testInsertRetrieveEmebeddedObjectList() {
        MockObject object = new MockObject();
        object.object = new MockEmbeddedObject();
        object.object.list = Arrays.asList("1", "2");
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object.object, result.object);
    }

    @Test
    public void testEverything() {
        MockObject object = new MockObject();
        object._id = "theid";
        object.integer = 123;
        object.longs = 1234L;
        object.floats = 12.34f;
        object.doubles = 123.456;
        object.booleans = true;
        object.simpleList = Arrays.asList("simple1", "simple2");
        MockEmbeddedObject o1 = new MockEmbeddedObject();
        o1.value = "embedded 1";
        o1.list = Arrays.asList("e1 list1", "e1 list2");
        MockEmbeddedObject o2 = new MockEmbeddedObject();
        o2.value = "embedded 2";
        o2.list = Arrays.asList("e2 list1", "e2 list2");
        MockEmbeddedObject o3 = new MockEmbeddedObject();
        o3.value = "embedded 3";
        o3.list = Arrays.asList("e3 list1", "e3 list2");

        object.complexList = Arrays.asList(o1, o2);
        object.object = o3;

        coll.insert(object);
        assertEquals(object, coll.findOne());
    }

    @Test
    public void testIntId() {
        MockObjectIntId object = new MockObjectIntId();
        object._id = 123456;

        JacksonDBCollection<MockObjectIntId, Integer> coll = getCollectionAs(MockObjectIntId.class);

        coll.insert(object);
        MockObjectIntId result = coll.findOne();
        assertEquals(object._id, result._id);
    }

    @Test
    public void testObjectId() {
        MockObjectObjectId object = new MockObjectObjectId();

        JacksonDBCollection<MockObjectObjectId, ObjectId> coll = getCollectionAs(MockObjectObjectId.class);

        ObjectId id = coll.insert(object).getSavedId();
        MockObjectObjectId result = coll.findOneById(id);
        assertEquals(id, result._id);
    }

    @Test
    public void testObjectIdAnnotationOnString() {
        MockObjectObjectIdAnnotated object = new MockObjectObjectIdAnnotated();

        JacksonDBCollection<MockObjectObjectIdAnnotated, String> coll = getCollectionAs(MockObjectObjectIdAnnotated.class);

        ObjectId id = (ObjectId) coll.insert(object).getDbObject().get("_id");
        MockObjectObjectIdAnnotated result = coll.findOneById(id.toString());
        assertEquals(id.toString(), result._id);
    }

    @Test
    public void testObjectIdAnnotationOnByteArray() {
        MockObjectObjectIdAnnotated object = new MockObjectObjectIdAnnotated();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object.someId = id.toByteArray();

        JacksonDBCollection<MockObjectObjectIdAnnotated, String> coll = getCollectionAs(MockObjectObjectIdAnnotated.class);

        MockObjectObjectIdAnnotated saved = coll.insert(object).getSavedObject();
        MockObjectObjectIdAnnotated result = coll.findOneById(saved._id);
        assertEquals(id, new ObjectId(result.someId));
    }

    private <T, K> JacksonDBCollection<T, K> getCollectionAs(Class<T> type) {
        return (JacksonDBCollection) JacksonDBCollection.wrap(coll.getDbCollection(), type);
    }

}
