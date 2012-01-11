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
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import net.vz.mongodb.jackson.mock.MockEmbeddedObject;
import net.vz.mongodb.jackson.mock.MockObject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;

public class TestDBQuery extends MongoDBTestBase {
    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setup() throws Exception {
        coll = getCollection(MockObject.class, String.class);
    }

    @Test
    public void testIsPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().is("integer", 10);
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testIsNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().is("integer", 9);
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testGreaterThanPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().greaterThan("integer", 9);
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testGreaterThanNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().greaterThan("integer", 10);
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testGreaterThanEqPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().greaterThanEquals("integer", 10);
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testGreaterThanEqNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().greaterThanEquals("integer", 11);
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testLessThanPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().lessThan("integer", 11);
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testLessThanNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().lessThan("integer", 10);
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testLessThanEqPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().lessThanEquals("integer", 10);
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testLessThanEqualsNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().lessThanEquals("integer", 9);
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testNotEqualsPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().notEquals("integer", 9);
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testNotEqualsNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().notEquals("integer", 10);
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testInPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().in("integer", 9, 10, 11);
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testInCollectionPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().in("integer", Arrays.asList(9, 10, 11));
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testInNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().in("integer", 9, 11);
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testNotInPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().notIn("integer", 9, 11);
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testNotInNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().notIn("integer", 9, 10, 11);
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testExistsPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().exists("integer");
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testExistsNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().exists("integerfun");
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testNotExistsPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().notExists("integerfun");
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testNotExistsNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().notExists("integer");
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testModPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().mod("integer", 3, 1);
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testModNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().mod("integer", 3, 2);
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testRegexPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().regex("string", Pattern.compile("h.llo"));
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testRegexNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().regex("string", Pattern.compile("hllo"));
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testOrPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().or(DBQuery.is("integer", 9), DBQuery.greaterThan("integer", 9),
                DBQuery.lessThan("integer", 9));
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testOrNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().or(DBQuery.is("integer", 9), DBQuery.lessThan("integer", 9));
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    @Ignore("I'm currently testing on MongoDB 1.8 and $and is only supported in 2")
    public void testAndPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().and(DBQuery.greaterThan("integer", 9), DBQuery.lessThan("integer", 11));
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    @Ignore("I'm currently testing on MongoDB 1.8 and $and is only supported in 2")
    public void testAndNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().and(DBQuery.greaterThan("integer", 9), DBQuery.is("string", "blah"),
                DBQuery.lessThan("integer", 11));
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testNorPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().nor(DBQuery.lessThan("integer", 9), DBQuery.greaterThan("integer", 11));
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testNorNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().and(DBQuery.lessThan("integer", 9), DBQuery.is("string", "hello"),
                DBQuery.greaterThan("integer", 11));
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testSizePositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().size("simpleList", 3);
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testSizeNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().size("simpleList", 4);
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testAllPositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().all("simpleList", "a", "b");
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testAllNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().all("simpleList", "a", "banana", "b");
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testElemMatchPositive() throws Exception {
        MockObject mockObject = insertMockObjectWithComplexList();
        DBCursor<MockObject> cursor = coll.find().elemMatch("complexList",
                DBQuery.in("value", "foo", "la").size("list", 3));
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testElemMatchNegative() throws Exception {
        insertMockObjectWithComplexList();
        DBCursor<MockObject> cursor = coll.find().elemMatch("complexList",
                DBQuery.in("value", "foo", "la").size("list", 2));
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testWherePositive() throws Exception {
        MockObject mockObject = insertMockObject();
        DBCursor<MockObject> cursor = coll.find().where("this.integer > 9");
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testWhereNegative() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find().where("this.integer < 9");
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testSerializationFromDBCursor() throws Exception {
        MockObject mockObject = insertMockObjectWithEmbedded();
        DBCursor<MockObject> cursor = coll.find().is("object", mockObject.object);
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test
    public void testSerializationFromInFind() throws Exception {
        MockObject mockObject = insertMockObjectWithEmbedded();
        DBCursor<MockObject> cursor = coll.find(DBQuery.in("object", mockObject.object));
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(mockObject));
    }

    @Test(expected = MongoException.class)
    public void testQueryAfterExecution() throws Exception {
        insertMockObject();
        DBCursor<MockObject> cursor = coll.find();
        cursor.hasNext();
        cursor.in("blah", "blah");
    }

    @Test
    public void testSortByDate() {

        insertMockWithDate("1", new Date(456));
        insertMockWithDate("2", new Date(12));
        insertMockWithDate("3", new Date(123));

        final DBCursor<MockObject> result = coll.find(DBQuery.lessThan("date", new Date())).sort(new BasicDBObject("date", "-1"));
        assertThat(result.next().date.getTime(), equalTo(new Date(456).getTime()));
        assertThat(result.next().date.getTime(), equalTo(new Date(123).getTime()));
        assertThat(result.next().date.getTime(), equalTo(new Date(12).getTime()));

        final List<MockObject> array = coll.find(DBQuery.lessThan("date", new Date())).sort(new BasicDBObject("date", "-1")).toArray();
        assertThat(array.get(0).date.getTime(), equalTo(new Date(456).getTime()));
        assertThat(array.get(1).date.getTime(), equalTo(new Date(123).getTime()));
        assertThat(array.get(2).date.getTime(), equalTo(new Date(12).getTime()));
    }

    private MockObject insertMockObject() {
        MockObject mockObject = new MockObject("someid", "hello", 10);
        mockObject.simpleList = Arrays.asList("a", "b", "c");
        coll.insert(mockObject);
        return mockObject;
    }

    private MockObject insertMockObjectWithEmbedded() {
        MockObject mockObject = new MockObject("someid", "hello", 10);
        MockEmbeddedObject embeddedObject = new MockEmbeddedObject();
        embeddedObject.value = "hello";
        embeddedObject.list = Arrays.asList("a", "b", "c");
        mockObject.object = embeddedObject;
        coll.insert(mockObject);
        return mockObject;
    }

    private MockObject insertMockObjectWithComplexList() {
        MockObject mockObject = new MockObject("someid", "hello", 10);
        MockEmbeddedObject embeddedObject1 = new MockEmbeddedObject();
        embeddedObject1.value = "foo";
        embeddedObject1.list = Arrays.asList("a", "b", "c");
        MockEmbeddedObject embeddedObject2 = new MockEmbeddedObject();
        embeddedObject2.value = "bar";
        embeddedObject2.list = Arrays.asList("d", "e");
        mockObject.complexList = Arrays.asList(embeddedObject1, embeddedObject2);
        coll.insert(mockObject);
        return mockObject;
    }

    private MockObject insertMockWithDate(String id, Date date) {
        MockObject mockObject = new MockObject(id, id, 1);
        mockObject.date = date;
        coll.insert(mockObject);
        return mockObject;
    }
}
