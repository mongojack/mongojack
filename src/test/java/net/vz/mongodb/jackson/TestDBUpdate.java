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
import net.vz.mongodb.jackson.mock.MockEmbeddedObject;
import net.vz.mongodb.jackson.mock.MockObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test a DBUpdate item
 */
public class TestDBUpdate {
    private Mongo mongo;
    private DB db;
    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setup() throws Exception {
        mongo = new Mongo();
        db = mongo.getDB("test");
        coll = JacksonDBCollection.wrap(db.getCollection("mockObject"), MockObject.class, String.class);
    }

    @After
    public void tearDown() throws Exception {
        coll.getDbCollection().drop();
        mongo.close();
    }

    @Test
    public void testIncByOne() throws Exception {
        coll.insert(new MockObject("blah", "string", 10));
        coll.updateById("blah", DBUpdate.inc("integer"));
        assertThat(coll.findOneById("blah").integer, equalTo(11));
    }

    @Test
    public void testInc() throws Exception {
        coll.insert(new MockObject("blah", "string", 10));
        coll.updateById("blah", DBUpdate.inc("integer", 2));
        assertThat(coll.findOneById("blah").integer, equalTo(12));
    }

    @Test
    public void testSet() throws Exception {
        coll.insert(new MockObject("blah", "string", 10));
        coll.updateById("blah", DBUpdate.set("integer", 2));
        assertThat(coll.findOneById("blah").integer, equalTo(2));
    }

    @Test
    public void testUnset() throws Exception {
        coll.insert(new MockObject("blah", "string", 10));
        coll.updateById("blah", DBUpdate.unset("integer"));
        assertThat(coll.findOneById("blah").integer, nullValue());
    }

    @Test
    public void testPush() throws Exception {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.push("simpleList", "world"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(2));
        assertThat(updated.simpleList, hasItem("world"));
    }

    @Test
    public void testPushAllList() throws Exception {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.pushAll("simpleList", Arrays.asList("world", "!")));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(3));
        assertThat(updated.simpleList, hasItem("world"));
        assertThat(updated.simpleList, hasItem("!"));
    }

    @Test
    public void testPushAllVarArgs() throws Exception {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.pushAll("simpleList", "world", "!"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(3));
        assertThat(updated.simpleList, hasItem("world"));
        assertThat(updated.simpleList, hasItem("!"));
    }

    @Test
    public void testAddToSetSingle() throws Exception {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello");
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
    public void testAddToSetList() throws Exception {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.addToSet("simpleList", Arrays.asList("world", "!")));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(3));
        assertThat(updated.simpleList, hasItem("!"));
    }

    @Test
    public void testAddToSetVarArgs() throws Exception {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.addToSet("simpleList", "world", "!"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(3));
        assertThat(updated.simpleList, hasItem("!"));
    }

    @Test
    public void testPopFirst() throws Exception {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.popFirst("simpleList"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(1));
        assertThat(updated.simpleList, hasItem("world"));
    }

    @Test
    public void testPopLast() throws Exception {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.popLast("simpleList"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(1));
        assertThat(updated.simpleList, hasItem("hello"));
    }

    @Test
    public void testPull() throws Exception {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.pull("simpleList", "world"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(1));
        assertThat(updated.simpleList, hasItem("hello"));
    }

    @Test
    public void testPullWithQuery() throws Exception {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.pull("simpleList", new BasicDBObject("$regex", Pattern.compile("w??ld"))));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(1));
        assertThat(updated.simpleList, hasItem("hello"));
    }

    @Test
    public void testPullAllList() throws Exception {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world", "!");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.pullAll("simpleList", Arrays.asList("hello", "!")));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(1));
        assertThat(updated.simpleList, hasItem("world"));
    }

    @Test
    public void testPullAllVarArgs() throws Exception {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world", "!");
        coll.insert(toSave);
        coll.updateById("blah", DBUpdate.pullAll("simpleList", "hello", "!"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList, hasSize(1));
        assertThat(updated.simpleList, hasItem("world"));
    }

    @Test
    public void testRename() throws Exception {
        coll.insert(new MockObject("blah", "some string", 10));
        coll.updateById("blah", DBUpdate.rename("string", "something"));
        DBObject object = coll.getDbCollection().findOne("blah");
        assertThat(object.get("string"), nullValue());
        assertThat((String) object.get("something"), equalTo("some string"));
    }

    @Test
    public void testBit() throws Exception {
        coll.insert(new MockObject("blah", "some string", 1 + 4 + 8));
        coll.updateById("blah", DBUpdate.bit("integer", "and", 4 + 8 + 16));
        assertThat(coll.findOneById("blah").integer, equalTo(4 + 8));
    }

    @Test
    public void testBitTwoOperations() throws Exception {
        coll.insert(new MockObject("blah", "some string", 1 + 4 + 8));
        coll.updateById("blah", DBUpdate.bit("integer", "and", 4 + 8 + 16, "or", 32));
        assertThat(coll.findOneById("blah").integer, equalTo(4 + 8 + 32));
    }

    @Test
    public void testBitwiseAnd() throws Exception {
        coll.insert(new MockObject("blah", "some string", 1 + 4 + 8));
        coll.updateById("blah", DBUpdate.bitwiseAnd("integer", 4 + 8 + 16));
        assertThat(coll.findOneById("blah").integer, equalTo(4 + 8));
    }

    @Test
    public void testBitwiseOr() throws Exception {
        coll.insert(new MockObject("blah", "some string", 1 + 4 + 8));
        coll.updateById("blah", DBUpdate.bitwiseOr("integer", 4 + 8 + 16));
        assertThat(coll.findOneById("blah").integer, equalTo(1 + 4 + 8 + 16));
    }

    @Test
    public void testObjectSerialisation() throws Exception {
        coll.insert(new MockObject("blah", "some string", 10));
        coll.updateById("blah", DBUpdate.set("object", new MockEmbeddedObject("hello")));
        assertThat(coll.findOneById("blah").object, equalTo(new MockEmbeddedObject("hello")));
    }

    @Test
    public void testObjectInListSerialisation() throws Exception {
        coll.insert(new MockObject("blah", "some string", 10));
        coll.updateById("blah", DBUpdate.pushAll("complexList", Arrays.asList(new MockEmbeddedObject("hello"))));
        assertThat(coll.findOneById("blah").complexList, hasItem(new MockEmbeddedObject("hello")));
    }

    @Test
    public void testSameOperationTwice() throws Exception {
        coll.insert(new MockObject("blah", "some string", 10));
        coll.updateById("blah", DBUpdate.set("string", "other string").set("integer", 20));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.string, equalTo("other string"));
        assertThat(updated.integer, equalTo(20));
    }
}
