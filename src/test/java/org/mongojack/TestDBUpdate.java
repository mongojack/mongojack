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
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongojack.mock.MockEmbeddedObject;
import org.mongojack.mock.MockObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test a Updates item
 */
public class TestDBUpdate extends MongoDBTestBase {

    private JacksonMongoCollection<MockObject> coll;

    @BeforeEach
    public void setup() {
        coll = getCollection(MockObject.class);
    }

    @Test
    public void testIncByOne() {
        coll.insert(new MockObject("blah", "string", 10));
        coll.updateById("blah", Updates.inc("integer", 1));
        assertThat(coll.findOneById("blah").integer).isEqualTo(11);
    }

    @Test
    public void testInc() {
        coll.insert(new MockObject("blah", "string", 10));
        coll.updateById("blah", Updates.inc("integer", 2));
        assertThat(coll.findOneById("blah").integer).isEqualTo(12);
    }

    @Test
    public void testSet() {
        coll.insert(new MockObject("blah", "string", 10));
        coll.updateById("blah", Updates.set("integer", 2));
        assertThat(coll.findOneById("blah").integer).isEqualTo(2);
    }

    @Test
    public void testUnset() {
        coll.insert(new MockObject("blah", "string", 10));
        coll.updateById("blah", Updates.unset("integer"));
        assertThat(coll.findOneById("blah").integer).isNull();
    }

    @Test
    public void testPush() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Collections.singletonList("hello");
        coll.insert(toSave);
        coll.updateById("blah", Updates.push("simpleList", "world"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList).hasSize(2);
        assertThat(updated.simpleList).contains("world");
    }

    @Test
    public void testPushAllList() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Collections.singletonList("hello");
        coll.insert(toSave);
        coll.updateById(
            "blah",
            Updates.pushEach("simpleList", Arrays.asList("world", "!"))
        );
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList).hasSize(3);
        assertThat(updated.simpleList).contains("world");
        assertThat(updated.simpleList).contains("!");
    }

    @Test
    public void testAddToSetSingle() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Collections.singletonList("hello");
        coll.insert(toSave);
        coll.updateById("blah", Updates.addToSet("simpleList", "world"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList).hasSize(2);
        assertThat(updated.simpleList).contains("world");
        // Try again, this time should not be updated
        coll.updateById("blah", Updates.addToSet("simpleList", "world"));
        updated = coll.findOneById("blah");
        assertThat(updated.simpleList).hasSize(2);
        assertThat(updated.simpleList).contains("world");
    }

    @Test
    public void testAddToSetList() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById(
            "blah",
            Updates.addEachToSet("simpleList", Arrays.asList("world", "!"))
        );
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList).hasSize(3);
        assertThat(updated.simpleList).contains("!");
    }

    @Test
    public void testAddToSetListWithUpsert() {
        coll.updateOne(
            Filters.eq("_id", "blah"),
            Updates.addEachToSet(
                "simpleList",
                Arrays.asList("hello", "world")
            ),
            new UpdateOptions().upsert(true)
        );
        MockObject inserted = coll.findOneById("blah");
        assertThat(inserted.simpleList).hasSize(2);
        assertThat(inserted.simpleList).contains("hello");
        assertThat(inserted.simpleList).contains("world");
    }

    @Test
    public void testAddToSetVarArgs() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById("blah", Updates.addEachToSet("simpleList", Arrays.asList("world", "!")));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList).hasSize(3);
        assertThat(updated.simpleList).contains("!");
    }

    @Test
    public void testPopFirst() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById("blah", Updates.popFirst("simpleList"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList).hasSize(1);
        assertThat(updated.simpleList).contains("world");
    }

    @Test
    public void testPopLast() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById("blah", Updates.popLast("simpleList"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList).hasSize(1);
        assertThat(updated.simpleList).contains("hello");
    }

    @Test
    public void testPull() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById("blah", Updates.pull("simpleList", "world"));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList).hasSize(1);
        assertThat(updated.simpleList).contains("hello");
    }

    @Test
    public void testPullWithQuery() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world");
        coll.insert(toSave);
        coll.updateById(
            "blah",
            Updates.pull(
                "simpleList",
                new BasicDBObject("$regex", Pattern.compile("w??ld"))
            )
        );
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList).hasSize(1);
        assertThat(updated.simpleList).contains("hello");
    }

    @Test
    public void testPullAllList() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world", "!");
        coll.insert(toSave);
        coll.updateById(
            "blah",
            Updates.pullAll("simpleList", Arrays.asList("hello", "!"))
        );
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList).hasSize(1);
        assertThat(updated.simpleList).contains("world");
    }

    @Test
    public void testPullAllVarArgs() {
        MockObject toSave = new MockObject("blah", "string", 10);
        toSave.simpleList = Arrays.asList("hello", "world", "!");
        coll.insert(toSave);
        coll.updateById("blah", Updates.pullAll("simpleList", Arrays.asList("hello", "!")));
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.simpleList).hasSize(1);
        assertThat(updated.simpleList).contains("world");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testRename() {
        coll.insert(new MockObject("blah", "some string", 10));
        coll.updateById("blah", Updates.rename("string", "something"));
        Document object = db.getCollection(coll.getMongoCollection().getNamespace().getCollectionName()).find(Filters.eq("_id", "blah")).first();
        assertThat(object.get("string")).isNull();
        assertThat(object.get("something")).isEqualTo("some string");
    }

    @Test
    public void testBit() {
        coll.insert(new MockObject("blah", "some string", 1 + 4 + 8));
        coll.updateById("blah", Updates.bitwiseAnd("integer", 4 + 8 + 16));
        assertThat(coll.findOneById("blah").integer).isEqualTo(4 + 8);
    }

    @Test
    public void testBitTwoOperations() {
        coll.insert(new MockObject("blah", "some string", 1 + 4 + 8));
        coll.updateById(
            "blah",
            new Document(
                "$bit",
                new Document(
                    "integer",
                    new Document("and", 4 + 8 + 16)
                        .append("or", 32)
                )
            )
        );
        assertThat(coll.findOneById("blah").integer).isEqualTo(4 + 8 + 32);
    }

    @Test
    public void testBitwiseAnd() {
        coll.insert(new MockObject("blah", "some string", 1 + 4 + 8));
        coll.updateById("blah", Updates.bitwiseAnd("integer", 4 + 8 + 16));
        assertThat(coll.findOneById("blah").integer).isEqualTo(4 + 8);
    }

    @Test
    public void testBitwiseOr() {
        coll.insert(new MockObject("blah", "some string", 1 + 4 + 8));
        coll.updateById("blah", Updates.bitwiseOr("integer", 4 + 8 + 16));
        assertThat(coll.findOneById("blah").integer).isEqualTo(1 + 4 + 8 + 16);
    }

    @Test
    public void testObjectSerialisation() {
        coll.insert(new MockObject("blah", "some string", 10));
        coll.updateById(
            "blah",
            Updates.set("object", new MockEmbeddedObject("hello"))
        );
        assertThat(coll.findOneById("blah").object).isEqualTo(new MockEmbeddedObject("hello"));
    }

    @Test
    public void testObjectInListSerialisation() {
        coll.insert(new MockObject("blah", "some string", 10));
        coll.updateById(
            "blah",
            Updates.pushEach(
                "complexList",
                Collections.singletonList(new MockEmbeddedObject("hello"))
            )
        );
        assertThat(coll.findOneById("blah").complexList).contains(new MockEmbeddedObject("hello"));
    }

    @Test
    public void testSameOperationTwice() {
        coll.insert(new MockObject("blah", "some string", 10));
        coll.updateById(
            "blah",
            Updates.combine(
                Updates.set("string", "other string"),
                Updates.set("integer", 20)
            )
        );
        MockObject updated = coll.findOneById("blah");
        assertThat(updated.string).isEqualTo("other string");
        assertThat(updated.integer).isEqualTo(20);
    }
}
