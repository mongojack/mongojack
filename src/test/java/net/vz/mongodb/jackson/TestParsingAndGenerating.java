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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import net.vz.mongodb.jackson.DBQuery.Query;
import net.vz.mongodb.jackson.mock.MockEmbeddedObject;
import net.vz.mongodb.jackson.mock.MockObject;
import net.vz.mongodb.jackson.mock.MockObjectIntId;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * Test for parser and generator
 */
public class TestParsingAndGenerating extends MongoDBTestBase {
    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setup() throws Exception {
        coll = getCollection(MockObject.class, String.class);
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
    public void testInsertRetrieveDate() {
        MockObject object = new MockObject();
        object.date = new Date(10000);
        coll.insert(object);
        MockObject result = coll.findOne();
        assertEquals(object.date, result.date);
    }

    @Test
    public void testDateIsStoredAsBsonDate() {
        MockObject object = new MockObject();
        object.date = new Date(10000);
        coll.insert(object);
        DBObject result = coll.getDbCollection().findOne();
        assertEquals(object.date, result.get("date"));
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

        JacksonDBCollection<MockObjectIntId, Integer> coll = getCollectionAs(MockObjectIntId.class, Integer.class);

        coll.insert(object);
        MockObjectIntId result = coll.findOne();
        assertEquals(object._id, result._id);
    }

    @Test(expected = MongoException.class)
    public void testParseErrors() {
        DBCursor<MockObject> cursor = coll.find(new BasicDBObject("integer", new BasicDBObject("$thisisinvalid", "true")));
        cursor.hasNext();
    }


    @Test
    public void testByteArray() throws Exception {
        ObjectWithByteArray object = new ObjectWithByteArray();
        object._id = "id";
        object.bytes = new byte[] {1, 2, 3, 4, 5};

        JacksonDBCollection<ObjectWithByteArray, String> coll = getCollectionAs(ObjectWithByteArray.class, String.class);
        coll.insert(object);

        ObjectWithByteArray result = coll.findOne();
        assertThat(result.bytes, equalTo(object.bytes));

        // Ensure that it is actually stored as binary
        DBObject dbObject = coll.getDbCollection().findOne();
        assertThat(dbObject.get("bytes"), instanceOf(byte[].class));
    }

    public static class ObjectWithByteArray {
        public String _id;
        public byte[] bytes;
    }

    private <T, K> JacksonDBCollection<T, K> getCollectionAs(Class<T> type, Class<K> keyType) {
        return getCollection(type, keyType, coll.getName());
    }

    @Test
    public void testHugeData() throws Exception {

        BiggerObjectWithByteArray o = new BiggerObjectWithByteArray();

        File dataFile = new File(TestParsingAndGenerating.class.getResource("dataFile").getFile());
        assertEquals(true, dataFile.exists());
        o.bytes = IOUtils.toByteArray(new FileInputStream(dataFile));;
        o.name = "ANAME";
        o.size = "ASIZE";
        JacksonDBCollection<BiggerObjectWithByteArray, String> coll = getCollectionAs(BiggerObjectWithByteArray.class, String.class);

        for (int i = 0; i < 1500; i++) {

            o._id = "id" + i;
            coll.insert(o);
        }

        for (int i = 0; i < 1500; i++) {
            final Query q = DBQuery.is("_id", o._id)
                                   .is("name", o.name)
                                   .is("size", o.size);

            final DBCursor<BiggerObjectWithByteArray> cursor = coll.find(q);

            assertEquals(true, cursor.hasNext());

            assertThat(cursor.next().bytes, equalTo(o.bytes));
        }
    }

    public static class BiggerObjectWithByteArray {
        public String _id;
        public String name;
        public String size;
        public byte[] bytes;
    }
}
