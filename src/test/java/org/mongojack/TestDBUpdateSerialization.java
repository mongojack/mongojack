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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mongojack.TestDBUpdateSerialization.NestedIdFieldWithDifferentType.*;

public class TestDBUpdateSerialization extends MongoDBTestBase {

    private JacksonMongoCollection<MockObject> coll;
    private JacksonMongoCollection<NestedRepeatedAttributeName> coll2;

    @BeforeEach
    public void setUp() {
        coll = getCollection(MockObject.class);
    }

    @Test
    @Disabled
    public void testSimpleCustomSerializer() {
        coll.save(new MockObject());
        coll.updateById("id", Updates.set("simple", "foo"));
        assertThat(coll.findOneById("id").simple).isEqualTo("bar");
    }

    @Test
    public void testSimpleCustomSerializerNotApplied() {
        MockObject o = new MockObject();
        o.simple = "blah";
        coll.save(o);
        coll.updateById("id", Updates.unset("simple"));
        assertThat(coll.findOneById("id").simple).isNull();
    }

    @Test
    public void testListCustomSerializerInObject() {
        final MockObject object = new MockObject();
        object.list = Arrays.asList("some", "foo");
        coll.save(object);
        assertThat(coll.findOneById("id").list).isEqualTo(Arrays.asList("some", "bar"));
    }

    @Test
    @Disabled
    public void testNestedValueCustomSerializer() {
        MockObject o = new MockObject();
        o.child = new MockObject();
        coll.save(o);
        coll.updateById("id", Updates.set("child.simple", "foo"));
        assertThat(coll.findOneById("id").child.simple).isEqualTo("bar");
    }

    @Test
    @Disabled
    public void testDollarsCustomSerializer() {
        MockObject o = new MockObject();
        MockObject c1 = new MockObject();
        c1.simple = "one";
        MockObject c2 = new MockObject();
        c2.simple = "two";
        o.childList = Arrays.asList(c1, c2);
        coll.save(o);
        coll.updateMany(
            Filters.eq("childList.simple", "one"),
            Updates.set("childList.$.simple", "foo")
        );
        assertThat(coll.findOneById("id").childList.get(0).simple).isEqualTo("bar");
        assertThat(coll.findOneById("id").childList.get(1).simple).isEqualTo("two");
    }

    @Test
    public void testMapValueCustomSerializerForObject() {
        MockObject o = new MockObject();
        o.customMap = new HashMap<>();
        o.customMap.put("blah", "foo");
        coll.save(o);
        assertThat(coll.findOneById("id").customMap.get("blah")).isEqualTo("bar");
    }

    @Test
    public void testSimpleObjectId() {
        MockObject o = new MockObject();
        coll.save(o);
        org.bson.types.ObjectId objectId = new org.bson.types.ObjectId();
        coll.updateById("id", Updates.set("objectId", objectId));
        assertThat(coll.findOneById("id").objectId).isEqualTo(objectId.toString());
    }

    @Test
    public void testObjectIdCollection() {
        MockObject o = new MockObject();
        coll.save(o);
        org.bson.types.ObjectId objectId = new org.bson.types.ObjectId();
        coll.updateById("id", Updates.push("objectIds", objectId));
        assertThat(coll.findOneById("id").objectIds.get(0)).isEqualTo(objectId.toString());
    }

    @Test
    public void testSimpleMap() {
        MockObject o = new MockObject();
        coll.save(o);
        Map<String, String> map = new HashMap<String, String>();
        map.put("foo", "bar");
        coll.updateById("id", Updates.set("map", map));
        assertThat(coll.findOneById("id").map).isEqualTo(map);
    }

    // Test to detect presence of issue https://github.com/mongojack/mongojack/issues/98
    @Test
    public void testUpdateOfNestedRepeatedAttributeName() {
        coll2 = getCollection(NestedRepeatedAttributeName.class);

        Date d1 = new Date(10000L);
        Date d2 = new Date(20000L);

        NestedRepeatedAttributeName original = new NestedRepeatedAttributeName();
        original.inner.timestamp = d1;
        original.timestamp = 30000;

        coll2.insert(original);
        coll2.updateById(original._id, Updates.set("inner.timestamp", d2));

        NestedRepeatedAttributeName updated = coll2.findOneById(original._id);
        assertThat(updated).isNotNull();
        assertThat(updated.inner.timestamp).isEqualTo(d2);
        assertThat(updated.timestamp).isEqualTo(original.timestamp);
    }

    // Test to detect presence of issue https://github.com/mongojack/mongojack/issues/127
    @Test
    public void testUpdateOfNestedIdFieldWithDifferentType() {
        JacksonMongoCollection<NestedIdFieldWithDifferentType> collection = getCollection(NestedIdFieldWithDifferentType.class);

        NestedIdFieldWithDifferentType original = new NestedIdFieldWithDifferentType();

        collection.insert(original);
        String newValue = "new value";
        collection.updateMany(Filters.eq("nested._id", NESTED_ID_FIELD_VALUE), Updates.set("value", newValue));

        NestedIdFieldWithDifferentType updated = collection.findOneById(original._id);
        assertThat(updated).isNotNull();
        assertThat(updated.value).isEqualTo(newValue);
    }

    public static class MockObject {
        public String _id = "id";
        @JsonSerialize(using = FooToBarSerializer.class)
        public String simple;
        @JsonSerialize(contentUsing = FooToBarSerializer.class)
        public List<String> list;
        public MockObject child;
        public List<MockObject> childList;
        public Map<String, String> map;
        @JsonSerialize(contentUsing = FooToBarSerializer.class)
        public Map<String, String> customMap;
        @ObjectId
        public String objectId;
        @ObjectId
        public List<String> objectIds;
    }

    public static class FooToBarSerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator jgen,
                              SerializerProvider provider) throws IOException,
            JsonProcessingException {
            if ("foo".equals(value)) {
                jgen.writeString("bar");
            } else {
                jgen.writeString(value);
            }
        }
    }

    public static class NestedRepeatedAttributeName {
        public static class Inner {
            public Date timestamp;
        }

        public String _id = "id";
        public Inner inner = new Inner();
        public long timestamp;
    }

    public static class NestedIdFieldWithDifferentType {
        public static final String DEFAULT_VALUE = "default-value";
        public static final Date NESTED_ID_FIELD_VALUE = new Date();

        public static class Nested {
            public Date _id = NESTED_ID_FIELD_VALUE;
        }

        public String _id = "id";
        public Nested nested = new Nested();
        public String value = DEFAULT_VALUE;
    }
}
