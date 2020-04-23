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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mongodb.DBRef;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class TestQuerySerialization extends MongoDBTestBase {

    private JacksonMongoCollection<MockObject> coll;

    @Before
    public void setUp() {
        coll = getCollection(MockObject.class);
    }

    @Test
    public void testSimpleEquals() {
        coll.save(new MockObject());
        String id = coll.findOne().id;
        assertNotNull(coll.findOne(DBQuery.is("_id", id)));
        assertNotNull(coll.findOne(Filters.eq("_id", id)));
        assertNotNull(coll.find().filter(Filters.eq("_id", id)).iterator().next());
    }

    @Test
    public void testIn() {
        coll.save(new MockObject());
        String id = coll.findOne().id;
        assertNotNull(coll.find(DBQuery.in("_id", id, new org.bson.types.ObjectId().toString())).first());
        assertNotNull(coll.find(Filters.in("_id", id, new org.bson.types.ObjectId().toString())).first());
        assertNotNull(coll.find().filter(Filters.in("_id", id, new org.bson.types.ObjectId().toString())).first());
    }

    @Test
    public void testIn_collectionOfStrings() {
        final JacksonMongoCollection<MockObjectWithList> c2 = getCollection(MockObjectWithList.class);
        MockObjectWithList o = new MockObjectWithList();
        o.simpleList = Arrays.asList("a", "d");
        c2.save(o);
        assertNotNull(o._id);
        List<String> x = new ArrayList<>();
        x.add("a");
        x.add("b");

        assertEquals(o._id, c2.findOne(DBQuery.in("simpleList", x))._id);
        assertEquals(o._id, c2.findOne(Filters.in("simpleList", x))._id);
        assertEquals(o._id, Objects.requireNonNull(c2.find().filter(Filters.in("simpleList", x)).first())._id);
    }

    @Test
    public void testEqual_collectionOfStrings() {
        final JacksonMongoCollection<MockObjectWithList> c2 = getCollection(MockObjectWithList.class);
        MockObjectWithList o = new MockObjectWithList();
        o.simpleList = Arrays.asList("a", "d");
        c2.save(o);
        assertNotNull(o._id);

        assertEquals(o._id, c2.findOne(DBQuery.is("simpleList", "a"))._id);
        assertEquals(o._id, c2.findOne(Filters.eq("simpleList", "d"))._id);
        assertEquals(o._id, Objects.requireNonNull(c2.find().filter(Filters.eq("simpleList", "a")).first())._id);
    }

    @Test
    public void testIn_collectionOfRefs() {
        final JacksonMongoCollection<MockObjectWithList> c2 = getCollection(MockObjectWithList.class);
        MockObjectWithList o = new MockObjectWithList();
        org.bson.types.ObjectId oid = new org.bson.types.ObjectId();
        o.refList = Arrays.asList(new DBRef("db1", "c1", "id1"), new DBRef("db1", "c1", oid));
        c2.save(o);
        assertNotNull(o._id);
        List<DBRef> x = new ArrayList<>();
        x.add(new DBRef("db2", "c2", new org.bson.types.ObjectId()));
        x.add(new DBRef("db1", "c1", "id1"));

        assertEquals(o._id, c2.findOne(DBQuery.in("refList", x))._id);
        assertEquals(o._id, c2.findOne(Filters.in("refList", x))._id);
        assertEquals(o._id, Objects.requireNonNull(c2.find().filter(Filters.in("refList", x)).first())._id);
    }

    @Test
    public void testEqual_collectionOfRefs() {
        final JacksonMongoCollection<MockObjectWithList> c2 = getCollection(MockObjectWithList.class);
        MockObjectWithList o = new MockObjectWithList();
        org.bson.types.ObjectId oid = new org.bson.types.ObjectId();
        o.refList = Arrays.asList(new DBRef("db1", "c1", "id1"), new DBRef("db1", "c1", oid));
        c2.save(o);
        assertNotNull(o._id);

        assertEquals(o._id, c2.findOne(DBQuery.is("refList", new DBRef("db1", "c1", "id1")))._id);
        assertEquals(o._id, c2.findOne(Filters.eq("refList", new DBRef("db1", "c1", oid)))._id);
        assertEquals(o._id, Objects.requireNonNull(c2.find().filter(Filters.eq("refList", new DBRef("db1", "c1", "id1"))).first())._id);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testLessThan() {
        MockObject o = new MockObject();
        o.i = 5;
        coll.save(o);
        // Ensure that the serializer actually worked
        assertThat(getMongoCollection(coll.getName(), Document.class).find().first().getInteger("i"), equalTo(15));
        assertNotNull(coll.find(DBQuery.lessThan("i", 12)).first());
        assertNotNull(coll.find(Filters.lt("i", 12)).first());
        assertNotNull(coll.find().filter(Filters.lt("i", 12)).first());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testLessThanWithoutCustomFilters() {
        JacksonMongoCollection<MockObject> localColl = getCollection(
            MockObject.class,
            JacksonMongoCollection.builder()
                .withSerializationOptions(
                    SerializationOptions.builder()
                        .withSimpleFilterSerialization(true)
                        .build()
                )
        );
        MockObject o = new MockObject();
        o.i = 5;
        localColl.save(o);
        // Ensure that the serializer actually worked
        assertThat(getMongoCollection(localColl.getName(), Document.class).find().first().getInteger("i"), equalTo(15));
        assertNull(localColl.find(Filters.lt("i", 12)).first());
        assertNull(localColl.find().filter(Filters.lt("i", 12)).first());

        assertNotNull(localColl.find(DBQuery.lessThan("i", 18)).first());
        assertNotNull(localColl.find(Filters.lt("i", 18)).first());
        assertNotNull(localColl.find().filter(Filters.lt("i", 18)).first());
    }

    @Test
    public void testAnd() {
        MockObject o = new MockObject();
        o.i = 5;
        coll.save(o);
        // Ensure that the serializer actually worked
        assertNotNull(
            coll.find(DBQuery.and(DBQuery.lessThan("i", 12), DBQuery.greaterThan("i", 4))).first());
        assertNull(
            coll.find(DBQuery.and(DBQuery.lessThan("i", 12), DBQuery.greaterThan("i", 9))).first());
        assertNotNull(
            coll.find(Filters.and(Filters.lt("i", 12), Filters.gt("i", 4))).first());
        assertNull(
            coll.find(Filters.and(Filters.lt("i", 12), Filters.gt("i", 9))).first());
        assertNotNull(
            coll.find()
                .filter(Filters.and(Filters.lt("i", 12), Filters.gt("i", 4))).first());
        assertNull(
            coll.find()
                .filter(Filters.and(Filters.lt("i", 12), Filters.gt("i", 9))).first());
    }

    @Test
    public void testAll() {
        MockObject o = new MockObject();
        MockEmbedded o1 = new MockEmbedded();
        o1.id = new org.bson.types.ObjectId().toString();
        o.items = Collections.singletonList(o1);
        coll.save(o);

        // Ensure that the serializer actually worked
        // with DBCursor
        assertNotNull(coll.find(DBQuery.all("items", o1)).first());
        assertNotNull(coll.find(Filters.all("items", o1)).first());
        assertNotNull(coll.find().filter(Filters.all("items", o1)).first());
    }

    @Test
    public void testList() {
        MockObject o = new MockObject();
        MockEmbedded o1 = new MockEmbedded();
        o1.id = new org.bson.types.ObjectId().toString();
        o.items = Collections.singletonList(o1);
        coll.save(o);

        assertNotNull(coll.find(DBQuery.is("items.id", o1.id)).first());
        assertNotNull(coll.find(Filters.eq("items.id", o1.id)).first());
        assertNotNull(coll.find().filter(Filters.eq("items.id", o1.id)).first());
    }

    @Test
    public void testArrayEquals() {
        MockObject o = new MockObject();
        MockEmbedded o1 = new MockEmbedded();
        o1.id = new org.bson.types.ObjectId().toString();
        o.items = Collections.singletonList(o1);
        coll.save(o);

        assertNotNull(coll.find(DBQuery.is("items", Collections.singletonList(o1))).first());
        assertNotNull(coll.find(Filters.eq("items", Collections.singletonList(o1))).first());
        assertNotNull(coll.find().filter(Filters.eq("items", Collections.singletonList(o1))).first());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSearchForCustomSerializedFields() {
        MockObject o1 = new MockObject();
        o1.wrappedString = new WrappedString("foo:bar");
        MockObject o2 = new MockObject();
        o2.wrappedString = new WrappedString("baz:qux");
        coll.insertMany(Arrays.asList(o1, o2));

        // some sanity checks
        assertNotNull(o1.id);
        assertNotNull(o2.id);
        final MongoCollection<Document> underlyingCollection = getMongoCollection(coll.getName(), Document.class);
        final Document found = underlyingCollection.find(Filters.eq("wrappedString", "foo:bar")).first();
        assertEquals("foo:bar", found.getString("wrappedString"));

        assertEquals(o1.id, coll.find(DBQuery.is("wrappedString", new WrappedString("foo:bar"))).first().id);
        assertEquals(o1.id, coll.find().filter(DBQuery.is("wrappedString", new WrappedString("foo:bar"))).first().id);
        assertEquals(o1.id, coll.find(DBQuery.is("wrappedString", "foo:bar")).first().id);
        assertEquals(o1.id, coll.find().filter(DBQuery.is("wrappedString", "foo:bar")).first().id);
        assertEquals(o1.id, coll.find(DBQuery.regex("wrappedString", Pattern.compile("foo:.*"))).first().id);
        assertEquals(o1.id, coll.find().filter(DBQuery.regex("wrappedString", Pattern.compile("foo:.*"))).first().id);

        assertEquals(o1.id, coll.find(Filters.eq("wrappedString", new WrappedString("foo:bar"))).first().id);
        assertEquals(o1.id, coll.find().filter(Filters.eq("wrappedString", new WrappedString("foo:bar"))).first().id);
        assertEquals(o1.id, coll.find(Filters.eq("wrappedString", "foo:bar")).first().id);
        assertEquals(o1.id, coll.find().filter(Filters.eq("wrappedString", "foo:bar")).first().id);
        assertEquals(o1.id, coll.find(Filters.regex("wrappedString", "foo:.*")).first().id);
        assertEquals(o1.id, coll.find().filter(Filters.regex("wrappedString", "foo:.*")).first().id);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSearchForCustomSerializedFieldsWithSimpleSerialization() {
        JacksonMongoCollection<MockObject> localColl = getCollection(
            MockObject.class,
            JacksonMongoCollection.builder()
                .withSerializationOptions(
                    SerializationOptions.builder()
                        .withSimpleFilterSerialization(true)
                        .build()
                )
        );
        MockObject o1 = new MockObject();
        o1.wrappedString = new WrappedString("foo:bar");
        MockObject o2 = new MockObject();
        o2.wrappedString = new WrappedString("baz:qux");
        localColl.insertMany(Arrays.asList(o1, o2));

        // some sanity checks
        assertNotNull(o1.id);
        assertNotNull(o2.id);
        final MongoCollection<Document> underlyingCollection = getMongoCollection(localColl.getName(), Document.class);
        final Document found = underlyingCollection.find(Filters.eq("wrappedString", "foo:bar")).first();
        assertEquals("foo:bar", found.getString("wrappedString"));

        assertEquals(o1.id, localColl.find(DBQuery.is("wrappedString", new WrappedString("foo:bar"))).first().id);
        assertEquals(o1.id, localColl.find().filter(DBQuery.is("wrappedString", new WrappedString("foo:bar"))).first().id);
        assertEquals(o1.id, localColl.find(DBQuery.is("wrappedString", "foo:bar")).first().id);
        assertEquals(o1.id, localColl.find().filter(DBQuery.is("wrappedString", "foo:bar")).first().id);
        assertEquals(o1.id, localColl.find(DBQuery.regex("wrappedString", Pattern.compile("foo:.*"))).first().id);
        assertEquals(o1.id, localColl.find().filter(DBQuery.regex("wrappedString", Pattern.compile("foo:.*"))).first().id);

        assertEquals(o1.id, localColl.find(Filters.eq("wrappedString", new WrappedString("foo:bar"))).first().id);
        assertEquals(o1.id, localColl.find().filter(Filters.eq("wrappedString", new WrappedString("foo:bar"))).first().id);
        assertEquals(o1.id, localColl.find(Filters.eq("wrappedString", "foo:bar")).first().id);
        assertEquals(o1.id, localColl.find().filter(Filters.eq("wrappedString", "foo:bar")).first().id);
        assertEquals(o1.id, localColl.find(Filters.regex("wrappedString", "foo:.*")).first().id);
        assertEquals(o1.id, localColl.find().filter(Filters.regex("wrappedString", "foo:.*")).first().id);
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @Test
    public void testSearchForCustomSerializedFieldsInList() {
        MockObject o1 = new MockObject();
        o1.wrappedStringList = Collections.singletonList(new WrappedString("foo:bar"));
        MockObject o2 = new MockObject();
        o2.wrappedStringList = Collections.singletonList(new WrappedString("baz:qux"));
        coll.insertMany(Arrays.asList(o1, o2));

        // some sanity checks
        assertNotNull(o1.id);
        assertNotNull(o2.id);
        final MongoCollection<Document> underlyingCollection = getMongoCollection(coll.getName(), Document.class);
        final Document found = underlyingCollection.find(Filters.eq("wrappedStringList", "foo:bar")).first();
        assertEquals("foo:bar", ((List<String>) found.get("wrappedStringList")).get(0));

        assertEquals(o1.id, coll.find(DBQuery.is("wrappedStringList", new WrappedString("foo:bar"))).first().id);
        assertEquals(o1.id, coll.find().filter(DBQuery.is("wrappedStringList", new WrappedString("foo:bar"))).first().id);
        assertEquals(o1.id, coll.find(DBQuery.is("wrappedStringList", "foo:bar")).first().id);
        assertEquals(o1.id, coll.find().filter(DBQuery.is("wrappedStringList", "foo:bar")).first().id);
        assertEquals(o1.id, coll.find(DBQuery.regex("wrappedStringList", Pattern.compile("foo:.*"))).first().id);
        assertEquals(o1.id, coll.find().filter(DBQuery.regex("wrappedStringList", Pattern.compile("foo:.*"))).first().id);

        assertEquals(o1.id, coll.find(Filters.eq("wrappedStringList", new WrappedString("foo:bar"))).first().id);
        assertEquals(o1.id, coll.find().filter(Filters.eq("wrappedStringList", new WrappedString("foo:bar"))).first().id);
        assertEquals(o1.id, coll.find(Filters.eq("wrappedStringList", "foo:bar")).first().id);
        assertEquals(o1.id, coll.find().filter(Filters.eq("wrappedStringList", "foo:bar")).first().id);
        assertEquals(o1.id, coll.find(Filters.regex("wrappedStringList", "foo:.*")).first().id);
        assertEquals(o1.id, coll.find().filter(Filters.regex("wrappedStringList", "foo:.*")).first().id);
    }

    static class MockObject {
        @ObjectId
        @Id
        public String id;

        @JsonSerialize(using = PlusTenSerializer.class)
        @JsonDeserialize(using = MinusTenDeserializer.class)
        public int i;

        public List<MockEmbedded> items;

        public WrappedString wrappedString;

        public List<WrappedString> wrappedStringList;
    }

    static class MockEmbedded {

        public String id;

    }

    @SuppressWarnings("unused")
    static class MockObjectWithList {

        @Id
        private String _id;

        @JsonProperty
        private List<String> simpleList;

        private List<DBRef> refList;

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

        public List<DBRef> getRefList() {
            return refList;
        }

        public void setRefList(final List<DBRef> refList) {
            this.refList = refList;
        }
    }

    @JsonSerialize(using = WrappedStringSerializer.class)
    interface StringWrapper {
        String getValue();
    }

    static class WrappedString implements StringWrapper {

        private final String value;

        WrappedString(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    static class PlusTenSerializer extends JsonSerializer<Integer> {
        @Override
        public void serialize(
            Integer value, JsonGenerator jgen,
            SerializerProvider provider
        ) throws IOException {
            jgen.writeNumber(value + 10);
        }
    }

    static class MinusTenDeserializer extends JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
            return jp.getValueAsInt() - 10;
        }
    }

    static class WrappedStringSerializer extends JsonSerializer<StringWrapper> {
        @Override
        public void serialize(
            StringWrapper value, JsonGenerator jgen,
            SerializerProvider provider
        ) throws IOException {
            if (value == null) {
                jgen.writeNull();
            } else {
                jgen.writeString(value.getValue());
            }
        }

        @Override
        public Class<StringWrapper> handledType() {
            return StringWrapper.class;
        }
    }

}
