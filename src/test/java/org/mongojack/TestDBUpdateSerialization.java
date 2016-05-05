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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mongojack.TestDBUpdateSerialization.NestedIdFieldWithDifferentType.NESTED_ID_FIELD_VALUE;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class TestDBUpdateSerialization extends MongoDBTestBase {

    private JacksonDBCollection<MockObject, String> coll;
    private JacksonDBCollection<NestedRepeatedAttributeName, String> coll2;

    @Before
    public void setUp() {
        coll = getCollection(MockObject.class, String.class);
    }

    @Test
    public void testSimpleCustomSerializer() {
        coll.save(new MockObject());
        coll.updateById("id", DBUpdate.set("simple", "foo"));
        assertThat(coll.findOneById("id").simple, equalTo("bar"));
    }

    @Test
    public void testSimpleCustomSerializerNotApplied() {
        MockObject o = new MockObject();
        o.simple = "blah";
        coll.save(o);
        coll.updateById("id", DBUpdate.unset("simple"));
        assertThat(coll.findOneById("id").simple, nullValue());
    }

    @Test
    @Ignore("Ignored until JACKSON-829 is fixed")
    public void testListSetCustomSerializer() {
        coll.save(new MockObject());
        coll.updateById("id",
                DBUpdate.set("list", Arrays.asList("some", "foo")));
        assertThat(coll.findOneById("id").list,
                equalTo(Arrays.asList("some", "bar")));
    }

    @Test
    @Ignore("Ignored until JACKSON-829 is fixed")
    public void testListSingleValueCustomSerializer() {
        coll.save(new MockObject());
        coll.updateById("id", DBUpdate.push("list", "foo"));
        assertThat(coll.findOneById("id").list, equalTo(Arrays.asList("bar")));
    }

    @Test
    @Ignore("Ignored until JACKSON-829 is fixed")
    public void testListMultiValueCustomSerializer() {
        coll.save(new MockObject());
        coll.updateById("id", DBUpdate.pushAll("list", "some", "foo"));
        assertThat(coll.findOneById("id").list,
                equalTo(Arrays.asList("some", "bar")));
    }

    @Test
    public void testNestedValueCustomSerializer() {
        MockObject o = new MockObject();
        o.child = new MockObject();
        coll.save(o);
        coll.updateById("id", DBUpdate.set("child.simple", "foo"));
        assertThat(coll.findOneById("id").child.simple, equalTo("bar"));
    }

    @Test
    public void testDollarsCustomSerializer() {
        MockObject o = new MockObject();
        MockObject c1 = new MockObject();
        c1.simple = "one";
        MockObject c2 = new MockObject();
        c2.simple = "two";
        o.childList = Arrays.asList(c1, c2);
        coll.save(o);
        coll.update(DBQuery.is("childList.simple", "one"),
                DBUpdate.set("childList.$.simple", "foo"));
        assertThat(coll.findOneById("id").childList.get(0).simple,
                equalTo("bar"));
        assertThat(coll.findOneById("id").childList.get(1).simple,
                equalTo("two"));
    }

    @Test
    @Ignore("Ignored until JACKSON-829 is fixed")
    public void testMapValueCustomSerializer() {
        MockObject o = new MockObject();
        o.customMap = new HashMap<String, String>();
        o.customMap.put("blah", "blah");
        coll.save(o);
        coll.updateById("id", DBUpdate.set("customMap.blah", "foo"));
        assertThat(coll.findOneById("id").customMap.get("blah"), equalTo("bar"));
    }

    @Test
    public void testSimpleObjectId() {
        MockObject o = new MockObject();
        coll.save(o);
        String objectId = new org.bson.types.ObjectId().toString();
        coll.updateById("id", DBUpdate.set("objectId", objectId));
        assertThat(coll.findOneById("id").objectId, equalTo(objectId));
    }

    @Test
    public void testObjectIdCollection() {
        MockObject o = new MockObject();
        coll.save(o);
        String objectId = new org.bson.types.ObjectId().toString();
        coll.updateById("id", DBUpdate.push("objectIds", objectId));
        assertThat(coll.findOneById("id").objectIds.get(0), equalTo(objectId));
    }

    @Test
    public void testSimpleMap() {
        MockObject o = new MockObject();
        coll.save(o);
        Map<String, String> map = new HashMap<String, String>();
        map.put("foo", "bar");
        coll.updateById("id", DBUpdate.set("map", map));
        assertThat(coll.findOneById("id").map, equalTo(map));
    }

    // Test to detect presence of issue https://github.com/mongojack/mongojack/issues/98
    @Test
    public void testUpdateOfNestedRepeatedAttributeName() {
        coll2 = getCollection(NestedRepeatedAttributeName.class, String.class);

        Date d1 = new Date(10000L);
        Date d2 = new Date(20000L);
    	
        NestedRepeatedAttributeName original = new NestedRepeatedAttributeName();
        original.inner.timestamp = d1;
        original.timestamp       = 30000;

        coll2.insert(original);
        coll2.updateById(original._id, DBUpdate.set("inner.timestamp", d2));

        NestedRepeatedAttributeName updated = coll2.findOneById(original._id);
        assertThat(updated, notNullValue());
        assertThat(updated.inner.timestamp, equalTo(d2));
        assertThat(updated.timestamp, equalTo(original.timestamp));
    }
    
    // Test to detect presence of issue https://github.com/mongojack/mongojack/issues/127
    @Test
    public void testUpdateOfNestedIdFieldWithDifferentType() {
        JacksonDBCollection<NestedIdFieldWithDifferentType, String> collection = getCollection(NestedIdFieldWithDifferentType.class, String.class);
        
        NestedIdFieldWithDifferentType original = new NestedIdFieldWithDifferentType();
        
        collection.insert(original);
        String newValue = "new value";
        collection.update(DBQuery.is("nested._id", NESTED_ID_FIELD_VALUE), DBUpdate.set("value", newValue));
        
        NestedIdFieldWithDifferentType updated = collection.findOneById(original._id);
        assertThat(updated, notNullValue());
        assertThat(updated.value, equalTo(newValue));
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
