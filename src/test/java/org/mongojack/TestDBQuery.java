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

import com.mongodb.client.MongoCursor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongojack.mock.MockEmbeddedObject;
import org.mongojack.mock.MockEmbeddedObject.MockEmbeddedListElement;
import org.mongojack.mock.MockObject;

import java.util.Arrays;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;


public class TestDBQuery extends MongoDBTestBase {

    private JacksonMongoCollection<MockObject> coll;

    @BeforeEach
    public void setup() {
        coll = getCollection(MockObject.class);
    }

    @Test
    public void testIsPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.is("integer", 10)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testIsNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.is("integer", 9)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testGreaterThanPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.greaterThan("integer", 9)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testGreaterThanNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.greaterThan("integer", 10)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testGreaterThanEqPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.greaterThanEquals("integer",
            10)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testGreaterThanEqNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.greaterThanEquals("integer",
            11)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testLessThanPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.lessThan("integer", 11)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testLessThanNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.lessThan("integer", 10)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testLessThanEqPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.lessThanEquals("integer", 10)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testLessThanEqualsNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.lessThanEquals("integer", 9)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testNotEqualsPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.notEquals("integer", 9)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testNotEqualsNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.notEquals("integer", 10)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testInPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.in("integer", 9, 10, 11)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testInCollectionPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.in("integer",
            Arrays.asList(9, 10, 11))).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testInNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.in("integer", 9, 11)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testNotInPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.notIn("integer", 9, 11)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testNotInNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.notIn("integer", 9, 10, 11)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testExistsPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.exists("integer")).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testExistsNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.exists("integerfun")).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testNotExistsPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.notExists("integerfun")).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testNotExistsNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.notExists("integer")).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testModPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.mod("integer", 3, 1)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testModNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.mod("integer", 3, 2)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testRegexPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.regex("string",
            Pattern.compile("h.llo"))).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testRegexNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.regex("string",
            Pattern.compile("hllo"))).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testOrPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.or(DBQuery.is("integer", 9),
            DBQuery.greaterThan("integer", 9),
            DBQuery.lessThan("integer", 9))).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testOrNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.or(DBQuery.is("integer", 9),
            DBQuery.lessThan("integer", 9))).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testAndPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.and(
            DBQuery.greaterThan("integer", 9),
            DBQuery.lessThan("integer", 11))).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testAndNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.and(
            DBQuery.greaterThan("integer", 9),
            DBQuery.is("string", "blah"), DBQuery.lessThan("integer", 11))).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testNorPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.nor(
            DBQuery.lessThan("integer", 9),
            DBQuery.greaterThan("integer", 11))).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testNorNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.and(
            DBQuery.lessThan("integer", 9), DBQuery.is("string", "hello"),
            DBQuery.greaterThan("integer", 11))).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testSizePositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.size("simpleList", 3)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testSizeNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.size("simpleList", 4)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testAllPositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.all("simpleList", "a", "b")).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testAllNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.all("simpleList", "a",
            "banana", "b")).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testAllEmbeddedPositive() {
        MockObject mockObject = insertMockObjectWithEmbedded();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.all("object.objectList.id", 1, 2)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testAllEmbeddedNegative() {
        insertMockObjectWithEmbedded();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.all("object.objectList.id", 1, 99, 2)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testElemMatchPositive() {
        MockObject mockObject = insertMockObjectWithComplexList();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.elemMatch("complexList",
            DBQuery.in("value", "foo", "la").size("list", 3))).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testElemMatchNegative() {
        insertMockObjectWithComplexList();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.elemMatch("complexList",
            DBQuery.in("value", "foo", "la").size("list", 2))).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testWherePositive() {
        MockObject mockObject = insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.where("this.integer > 9")).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testWhereNegative() {
        insertMockObject();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.where("this.integer < 9")).iterator();
        assertThat(cursor.hasNext()).isEqualTo(false);
    }

    @Test
    public void testSerializationFromDBCursor() {
        MockObject mockObject = insertMockObjectWithEmbedded();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.is("object",
            mockObject.object)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
    }

    @Test
    public void testSerializationFromInFind() {
        MockObject mockObject = insertMockObjectWithEmbedded();
        final MongoCursor<MockObject> cursor = coll.find(DBQuery.in("object",
            mockObject.object)).iterator();
        assertThat(cursor.hasNext()).isEqualTo(true);
        assertThat(cursor.next()).isEqualTo(mockObject);
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
        embeddedObject.objectList = Arrays.asList(
            new MockEmbeddedListElement(1),
            new MockEmbeddedListElement(2),
            new MockEmbeddedListElement(3));
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
        mockObject.complexList = Arrays
            .asList(embeddedObject1, embeddedObject2);
        coll.insert(mockObject);
        return mockObject;
    }

}
