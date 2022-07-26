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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.internal.MongoJackModule;
import org.mongojack.mock.MockObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;

public class TestJacksonMongoCollection extends MongoDBTestBase {
    private JacksonMongoCollection<MockObject> coll;

    @Before
    public void setup() {
        coll = JacksonMongoCollection.builder().build(getMongoCollection("testJacksonMongoCollection", MockObject.class), MockObject.class, uuidRepresentation);
    }

    @Test
    public void testQuery() {
        MockObject o1 = new MockObject("1", "ten", 10);
        MockObject o2 = new MockObject("2", "ten", 10);
        coll.insert(o1, o2, new MockObject("twenty", 20));

        List<MockObject> results = coll
            .find(new Document("string", "ten")).into(new ArrayList<>());
        assertThat(results, hasSize(2));
        assertThat(results, contains(o1, o2));
    }

    @Test
    public void testInsertAndQuery() {
        MockObject o1 = new MockObject("ten", 10);
        MockObject o2 = new MockObject("ten", 10);
        coll.insert(o1, o2, new MockObject("twenty", 20));

        List<MockObject> results = coll
            .find(Filters.in("_id", o1._id, o2._id))
            .into(new ArrayList<>());
        assertThat(results, hasSize(2));
        assertThat(results, contains(o1, o2));
    }

    public static class ClassWithGenericField<T> {

        public org.bson.types.ObjectId _id;

        public T field;

        public ClassWithGenericField() {
        }

        public ClassWithGenericField(T field) {
            this.field = field;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClassWithGenericField<?> that = (ClassWithGenericField<?>) o;
            return Objects.equals(_id, that._id) && Objects.equals(field, that.field);
        }

        @Override
        public int hashCode() {
            return Objects.hash(_id, field);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ClassWithGenericField.class.getSimpleName() + "[", "]")
                .add("_id=" + _id)
                .add("field=" + field)
                .toString();
        }
    }
    
    public static class AnEmbeddedObject {
        
        public String field1;
        
        public String field2;

        public AnEmbeddedObject() {
        }

        public AnEmbeddedObject(final String field1, final String field2) {
            this.field1 = field1;
            this.field2 = field2;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final AnEmbeddedObject that = (AnEmbeddedObject) o;
            return Objects.equals(field1, that.field1) && Objects.equals(field2, that.field2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(field1, field2);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", AnEmbeddedObject.class.getSimpleName() + "[", "]")
                .add("field1='" + field1 + "'")
                .add("field2='" + field2 + "'")
                .toString();
        }
    }

    @Test
    public void testInsertAndQueryWithJavaType() {
        ObjectMapper objectMapper = MongoJackModule.configure(new ObjectMapper());

        JacksonMongoCollection<ClassWithGenericField<String>> coll2 = JacksonMongoCollection.builder().build(
            getMongoCollection(ClassWithGenericField.class),
            objectMapper.constructType(new TypeReference<ClassWithGenericField<String>>() {
            }),
            uuidRepresentation
        );

        ClassWithGenericField<String> o1 = new ClassWithGenericField<>("ten");
        ClassWithGenericField<String> o2 = new ClassWithGenericField<>("ten");
        coll2.insert(o1, o2, new ClassWithGenericField<>("twenty"));

        List<ClassWithGenericField<String>> results = coll2
            .find(Filters.in("_id", o1._id, o2._id))
            .into(new ArrayList<>());
        assertThat(results, hasSize(2));
        assertThat(results, contains(o1, o2));
    }

    @Test
    public void testInsertAndQueryWithTypeReference() {
        JacksonMongoCollection<ClassWithGenericField<String>> coll2 = JacksonMongoCollection.builder().build(
            getMongoCollection(ClassWithGenericField.class),
            new TypeReference<ClassWithGenericField<String>>() {},
            uuidRepresentation
        );

        ClassWithGenericField<String> o1 = new ClassWithGenericField<>("ten");
        ClassWithGenericField<String> o2 = new ClassWithGenericField<>("ten");
        coll2.insert(o1, o2, new ClassWithGenericField<>("twenty"));

        List<ClassWithGenericField<String>> results = coll2
            .find(Filters.in("_id", o1._id, o2._id))
            .into(new ArrayList<>());
        assertThat(results, hasSize(2));
        assertThat(results, contains(o1, o2));
    }

    @Test
    public void testInsertAndQueryWithJavaTypeEmbedded() {
        ObjectMapper objectMapper = MongoJackModule.configure(new ObjectMapper());

        JacksonMongoCollection<ClassWithGenericField<AnEmbeddedObject>> coll2 = JacksonMongoCollection.builder().build(
            getMongoCollection(ClassWithGenericField.class),
            objectMapper.constructType(new TypeReference<ClassWithGenericField<AnEmbeddedObject>>() {
            }),
            uuidRepresentation
        );

        ClassWithGenericField<AnEmbeddedObject> o1 = new ClassWithGenericField<>(new AnEmbeddedObject("a", "b"));
        ClassWithGenericField<AnEmbeddedObject> o2 = new ClassWithGenericField<>(new AnEmbeddedObject("c", "d"));
        coll2.insert(o1, o2, new ClassWithGenericField<>(new AnEmbeddedObject("e", "f")));

        List<ClassWithGenericField<AnEmbeddedObject>> results = coll2
            .find(Filters.in("_id", o1._id, o2._id))
            .into(new ArrayList<>());
        assertThat(results, hasSize(2));
        assertThat(results, contains(o1, o2));
    }

    @Test
    public void testInsertAndQueryWithTypeReferenceEmbedded() {
        JacksonMongoCollection<ClassWithGenericField<AnEmbeddedObject>> coll2 = JacksonMongoCollection.builder().build(
            getMongoCollection(ClassWithGenericField.class),
            new TypeReference<ClassWithGenericField<AnEmbeddedObject>>() {},
            uuidRepresentation
        );

        ClassWithGenericField<AnEmbeddedObject> o1 = new ClassWithGenericField<>(new AnEmbeddedObject("a", "b"));
        ClassWithGenericField<AnEmbeddedObject> o2 = new ClassWithGenericField<>(new AnEmbeddedObject("c", "d"));
        coll2.insert(o1, o2, new ClassWithGenericField<>(new AnEmbeddedObject("e", "f")));

        List<ClassWithGenericField<AnEmbeddedObject>> results = coll2
            .find(Filters.in("_id", o1._id, o2._id))
            .into(new ArrayList<>());
        assertThat(results, hasSize(2));
        assertThat(results, contains(o1, o2));
    }

    @Test
    public void testSaveAndQuery() {
        MockObject o1 = new MockObject("1", "ten", 10);
        MockObject o2 = new MockObject("2", "ten", 10);
        coll.save(o1);
        coll.save(o2);

        MockObject o3 = new MockObject("twenty", 20);
        o3.date = new Date();
        UpdateResult saveResult = coll.save(o3);
        assertThat(saveResult.getUpsertedId(), notNullValue());
        assertThat(o3._id, notNullValue());

        o3.string = "ten";
        coll.save(o3);

        List<MockObject> results = coll
            .find(new Document("string", "ten")).into(new ArrayList<>());
        assertThat(results, hasSize(3));
        assertThat(results, contains(o1, o2, o3));

        assertThat(coll.findOne(DBQuery.is("_id", o3._id)), equalTo(o3));
    }

    @Test
    public void testRemove() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        MockObject object = new MockObject("1", "twenty", 20);
        coll.insert(object);

        coll.deleteMany(new Document("string", "ten"));

        List<MockObject> remaining = coll.find().into(new ArrayList<>());
        assertThat(remaining, Matchers.hasSize(1));
        assertThat(remaining, contains(object));
    }

    @Test
    public void testRemoveById() {
        coll.insert(new MockObject("id1", "ten", 10));
        coll.insert(new MockObject("id2", "ten", 100));
        MockObject object = new MockObject("id3", "twenty", 20);
        coll.insert(object);

        coll.removeById("id3");

        List<MockObject> remaining = coll.find().into(new ArrayList<>());
        assertThat(remaining, Matchers.hasSize(2));
        assertThat(remaining, not(contains(object)));
    }

    @Test
    public void testFindAndModifyWithBuilder() {
        coll.insert(new MockObject("id1", "ten", 10));
        coll.insert(new MockObject("id2", "ten", 10));

        MockObject mockObject3 = new MockObject("id3", "ten", 10);
        mockObject3.simpleList = new ArrayList<>();
        mockObject3.simpleList.add("a");
        mockObject3.simpleList.add("b");
        coll.insert(mockObject3);

        // Bson query, Bson fields, Bson sort, Bson update, boolean returnNew, boolean upsert

        MockObject result1 = coll.findOneAndUpdate(
            DBQuery.is("_id", "id1"),
            DBUpdate.set("integer", 20).set("string", "twenty"),
            new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(false)
        );
        assertThat(result1.integer, equalTo(20));
        assertThat(result1.string, equalTo("twenty"));

        MockObject result2 = coll.findOneAndUpdate(
            DBQuery.is("_id", "id2"),
            DBUpdate.set("integer", 30).set("string", "thirty"),
            new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(false)
        );
        assertThat(result2.integer, equalTo(30));
        assertThat(result2.string, equalTo("thirty"));

        MockObject result3 = coll.findOneAndUpdate(
            DBQuery.is("_id", "id3"),
            DBUpdate.pushAll("simpleList", Arrays.asList("1", "2", "3")),
            new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(false)
        );
        assertThat(result3.simpleList, hasSize(5));
        assertThat(result3.simpleList, hasItems("1", "2", "3"));

        coll.removeById("id1");
        coll.removeById("id2");
        coll.removeById("id3");
    }

    @Test
    public void testReplaceOneByNonIdQuery() {
        coll.insert(new MockObject("id1", "ten", 10));

        coll.withWriteConcern(WriteConcern.W1).replaceOne(
            DBQuery.is("string", "ten"),
            new MockObject("id1", "twenty", 20)
        );

        MockObject found = coll.findOne(DBQuery.is("_id", "id1"));

        assertThat(found, equalTo(new MockObject("id1", "twenty", 20)));
    }

    @Test
    public void testReplaceOneByUsesQueryNotId() {
        coll.insert(new MockObject("id1", "ten", 10));

        coll.withWriteConcern(WriteConcern.W1).replaceOne(
            DBQuery.is("string", "ten"),
            new MockObject(null, "twenty", 20)
        );

        MockObject found = coll.findOne(DBQuery.is("_id", "id1"));

        assertThat(found, equalTo(new MockObject("id1", "twenty", 20)));
    }

    @Test
    public void testReplaceOneUpsertsIfNoDocumentExistsByQueryAndUpsertTrue() {
        coll.replaceOne(
            DBQuery.is("string", "ten"),
            new MockObject(null, "twenty", 20),
            new ReplaceOptions().upsert(true)
        );

        MockObject found = coll.findOne(DBQuery.is("string", "twenty"));

        assertThat(found, equalTo(new MockObject(found._id, "twenty", 20)));
    }

    @Test
    public void testReplaceOneDoesNotUpsertIfUpsertFalse() {
        coll.replaceOne(
            DBQuery.is("string", "ten"),
            new MockObject(null, "twenty", 20)
        );

        MockObject found = coll.findOne(DBQuery.is("string", "twenty"));

        assertThat(found, nullValue());
    }

    @Test
    public void testReplaceOneByIdUsesIdProvided() {
        coll.insert(new MockObject("id1", "ten", 10));

        coll.replaceOneById("id1", new MockObject(null, "twenty", 20));

        MockObject found = coll.findOne(DBQuery.is("_id", "id1"));

        assertThat(found, equalTo(new MockObject("id1", "twenty", 20)));
    }

    @Test
    public void testQueryWithLimitedKeys() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        coll.insert(new MockObject("twenty", 20));

        List<MockObject> results = StreamSupport.stream(coll.find(
            new BasicDBObject("string", "ten")
        ).projection(new BasicDBObject("string", "something not null")).spliterator(), false).collect(Collectors.toList());
        assertThat(results, hasSize(2));
        assertThat(results.get(0).integer, nullValue());
        assertThat(results.get(0).string, equalTo("ten"));
        assertThat(results.get(1).integer, nullValue());
        assertThat(results.get(1).string, equalTo("ten"));
    }

}
