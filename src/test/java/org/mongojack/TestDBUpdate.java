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

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.mock.MockEmbeddedObject;
import org.mongojack.mock.MockObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;

/**
 * Test a DBUpdate item
 */
public class TestDBUpdate extends MongoDBTestBase {

    private JacksonMongoCollection<MockObject> coll;

    @Before
    public void setup() {
        coll = getCollection(MockObject.class);
    }

    @Test
    public void testIncByOne() {
        coll.insert(new MockObject("blah", "string", 10));
        coll.updateById("blah", DBUpdate.inc("integer"));
        assertThat(coll.findOneById("blah").integer, equalTo(11));
    }

    @Test
    public void testInc() {
        coll.insert(new MockObject("blah", "string", 10));
        coll.updateById("blah", DBUpdate.inc("integer", 2));
        assertThat(coll.findOneById("blah").integer, equalTo(12));
    }

    @Test
    public void testSet() {
        coll.insert(new MockObject("blah", "string", 10));
        coll.updateById("blah", DBUpdate.set("integer", 2));
        assertThat(coll.findOneById("blah").integer, equalTo(2));
    }

    @Test
    public void testUnset() {
        coll.insert(new MockObject("blah", "string", 10));
        coll.updateById("blah", DBUpdate.unset("integer"));
        assertThat(coll.findOneById("blah").integer, nullValue());
    }

    @Test
    public void testPush() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Collections.singletonList("hello");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.push("simpleList", "world"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(2));
        assertThat(updated.simpleList, hasItem("world"));
    }

    @Test
    public void testPushAllList() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Collections.singletonList("hello");
        coll.insert(toSave);
        coll.updateById(
            "blah",
            DBUpdate.pushAll("simpleList", Arrays.asList("world", "!"))
        );
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(3));
        assertThat(updated.simpleList, hasItem("world"));
        assertThat(updated.simpleList, hasItem("!"));
    }

    @Test
    public void testPushAllVarArgs() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Collections.singletonList("hello");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.pushAll("simpleList", "world", "!"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(3));
        assertThat(updated.simpleList, hasItem("world"));
        assertThat(updated.simpleList, hasItem("!"));
    }

    @Test
    public void testAddToSetSingle() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Collections.singletonList("hello");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.addToSet("simpleList", "world"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(2));
        assertThat(updated.simpleList, hasItem("world"));
        // Try again, this time should not be updated
        coll.updateById("blah", DBUpdate.addToSet("simpleList", "world"));
        updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(2));
        assertThat(updated.simpleList, hasItem("world"));
    }

    @Test
    public void testAddToSetList() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById(
            "blah",
            DBUpdate.addToSet("simpleList", Arrays.asList("world", "!"))
        );
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(3));
        assertThat(updated.simpleList, hasItem("!"));
    }

    @Test
    public void testAddToSetListWithUpsert() {
        coll.updateOne(
            DBQuery.is("_id", "blah"),
            DBUpdate.addToSet(
                "simpleList",
                Arrays.asList("hello", "world")
            ),
            new UpdateOptions().upsert(true)
        );
        MockObject inserted = coll.findOneById("blah");
        assertThat(inserted.simpleList, hasSize(2));
        assertThat(inserted.simpleList, hasItem("hello"));
        assertThat(inserted.simpleList, hasItem("world"));
    }

    @Test
    public void testAddToSetVarArgs() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.addToSet("simpleList", "world", "!"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(3));
        assertThat(updated.simpleList, hasItem("!"));
    }

    @Test
    public void testPopFirst() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.popFirst("simpleList"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(1));
        assertThat(updated.simpleList, hasItem("world"));
    }

    @Test
    public void testPopLast() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.popLast("simpleList"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(1));
        assertThat(updated.simpleList, hasItem("hello"));
    }

    @Test
    public void testPull() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.pull("simpleList", "world"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(1));
        assertThat(updated.simpleList, hasItem("hello"));
    }

    @Test
    public void testPullWithQuery() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById(
            "blah",
            DBUpdate.pull(
                "simpleList",
                new BasicDBObject("$regex", Pattern.compile("w??ld"))
            )
        );
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(1));
        assertThat(updated.simpleList, hasItem("hello"));
    }

    @Test
    public void testPullAllList() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world", "!");
        coll.insert(toSave);
        coll.updateById(
            "blah",
            DBUpdate.pullAll("simpleList", Arrays.asList("hello", "!"))
        );
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(1));
        assertThat(updated.simpleList, hasItem("world"));
    }

    @Test
    public void testPullAllVarArgs() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world", "!");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.pullAll("simpleList", "hello", "!"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(1));
        assertThat(updated.simpleList, hasItem("world"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testRename() {
        coll.insert(new MockObject("blah", "some string", 10));
        coll.updateById("blah", DBUpdate.rename("string", "something"));
        Document object = db.getCollection(coll.getMongoCollection().getNamespace().getCollectionName()).find(Filters.eq("_id", "blah")).first();
        assertThat(object.get("string"), nullValue());
        assertThat(object.get("something"), equalTo("some string"));
    }

    @Test
    public void testBit() {
        coll.insert(new MockObject("blah", "some string", 1 + 4 + 8));
        coll.updateById("blah", DBUpdate.bit("integer", "and", 4 + 8 + 16));
        assertThat(coll.findOneById("blah").integer, equalTo(4 + 8));
    }

    @Test
    public void testBitTwoOperations() {
        coll.insert(new MockObject("blah", "some string", 1 + 4 + 8));
        coll.updateById(
            "blah",
            DBUpdate.bit("integer", "and", 4 + 8 + 16, "or", 32)
        );
        assertThat(coll.findOneById("blah").integer, equalTo(4 + 8 + 32));
    }

    @Test
    public void testBitwiseAnd() {
        coll.insert(new MockObject("blah", "some string", 1 + 4 + 8));
        coll.updateById("blah", DBUpdate.bitwiseAnd("integer", 4 + 8 + 16));
        assertThat(coll.findOneById("blah").integer, equalTo(4 + 8));
    }

    @Test
    public void testBitwiseOr() {
        coll.insert(new MockObject("blah", "some string", 1 + 4 + 8));
        coll.updateById("blah", DBUpdate.bitwiseOr("integer", 4 + 8 + 16));
        assertThat(coll.findOneById("blah").integer, equalTo(1 + 4 + 8 + 16));
    }

    @Test
    public void testObjectSerialisation() {
        coll.insert(new MockObject("blah", "some string", 10));
        coll.updateById(
            "blah",
            DBUpdate.set("object", new MockEmbeddedObject("hello"))
        );
        assertThat(
            coll.findOneById("blah").object,
            equalTo(new MockEmbeddedObject("hello"))
        );
    }

    @Test
    public void testObjectInListSerialisation() {
        coll.insert(new MockObject("blah", "some string", 10));
        coll.updateById(
            "blah",
            DBUpdate.pushAll(
                "complexList",
                Collections.singletonList(new MockEmbeddedObject("hello"))
            )
        );
        assertThat(
            coll.findOneById("blah").complexList,
            hasItem(new MockEmbeddedObject("hello"))
        );
    }

    @Test
    public void testSameOperationTwice() {
        coll.insert(new MockObject("blah", "some string", 10));
        coll.updateById(
            "blah",
            DBUpdate.set("string", "other string").set("integer", 20)
        );
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.string, equalTo("other string"));
        assertThat(updated.integer, equalTo(20));
    }

    @Test
    public void testDBUpdateIsEmpty() {
        DBUpdate.Builder builder = new DBUpdate.Builder();
        assertTrue(builder.isEmpty());
        builder.inc("blah");
        assertFalse(builder.isEmpty());
    }
}
