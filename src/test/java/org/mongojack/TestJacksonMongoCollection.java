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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongojack.mock.MockObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;


public class TestJacksonMongoCollection extends MongoDBTestBase {
    private JacksonMongoCollection<MockObject> coll;

    @BeforeEach
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
        assertThat(results).hasSize(2);
        assertThat(results).contains(o1, o2);
    }

    @Test
    public void testInsertAndQuery() {
        MockObject o1 = new MockObject("ten", 10);
        MockObject o2 = new MockObject("ten", 10);
        coll.insert(o1, o2, new MockObject("twenty", 20));

        List<MockObject> results = coll
            .find(Filters.in("_id", o1._id, o2._id))
            .into(new ArrayList<>());
        assertThat(results).hasSize(2);
        assertThat(results).contains(o1, o2);
    }

    @JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS)
    interface GenericFieldValue {

    }

    public static class GenericFieldValue1 implements GenericFieldValue {

        public String a;

        public String b;

        public GenericFieldValue1() {
        }

        public GenericFieldValue1(final String a, final String b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final GenericFieldValue1 that = (GenericFieldValue1) o;
            return Objects.equals(a, that.a) && Objects.equals(b, that.b);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", GenericFieldValue1.class.getSimpleName() + "[", "]")
                .add("a='" + a + "'")
                .add("b='" + b + "'")
                .toString();
        }
    }

    public static class GenericFieldValue2 implements GenericFieldValue {

        public String c;

        public String d;

        public GenericFieldValue2() {
        }

        public GenericFieldValue2(final String c, final String d) {
            this.c = c;
            this.d = d;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final GenericFieldValue2 that = (GenericFieldValue2) o;
            return Objects.equals(c, that.c) && Objects.equals(d, that.d);
        }

        @Override
        public int hashCode() {
            return Objects.hash(c, d);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", GenericFieldValue2.class.getSimpleName() + "[", "]")
                .add("c='" + c + "'")
                .add("d='" + d + "'")
                .toString();
        }
    }

    public static class ClassWithGenericField<T extends GenericFieldValue> {

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
    }

    @Test
    public void testInsertAndQueryWithPolymorphicField() {
        JacksonMongoCollection<ClassWithGenericField> coll2 = getCollection(ClassWithGenericField.class);

        ClassWithGenericField<GenericFieldValue1> o1 = new ClassWithGenericField<>(new GenericFieldValue1("a", "b"));
        ClassWithGenericField<GenericFieldValue2> o2 = new ClassWithGenericField<>(new GenericFieldValue2("c", "d"));
        coll2.insert(o1, o2, new ClassWithGenericField<>(null));

        List<ClassWithGenericField> results = coll2
            .find(Filters.in("_id", o1._id, o2._id))
            .into(new ArrayList<>());
        assertThat(results).hasSize(2);
        assertThat(results).contains(o1, o2);
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
        assertThat(saveResult.getUpsertedId()).isNotNull();
        assertThat(o3._id).isNotNull();

        o3.string = "ten";
        coll.save(o3);

        List<MockObject> results = coll
            .find(new Document("string", "ten")).into(new ArrayList<>());
        assertThat(results).hasSize(3);
        assertThat(results).contains(o1, o2, o3);

        assertThat(coll.findOne(Filters.eq("_id", o3._id))).isEqualTo(o3);
    }

    @Test
    public void testRemove() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        MockObject object = new MockObject("1", "twenty", 20);
        coll.insert(object);

        coll.deleteMany(new Document("string", "ten"));

        List<MockObject> remaining = coll.find().into(new ArrayList<>());
        assertThat(remaining).hasSize(1);
        assertThat(remaining).contains(object);
    }

    @Test
    public void testRemoveById() {
        coll.insert(new MockObject("id1", "ten", 10));
        coll.insert(new MockObject("id2", "ten", 100));
        MockObject object = new MockObject("id3", "twenty", 20);
        coll.insert(object);

        coll.removeById("id3");

        List<MockObject> remaining = coll.find().into(new ArrayList<>());
        assertThat(remaining).hasSize(2);
        assertThat(remaining).doesNotContain(object);
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
            Filters.eq("_id", "id1"),
            Updates.combine(Updates.set("integer", 20), Updates.set("string", "twenty")),
            new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(false)
        );
        assertThat(result1.integer).isEqualTo(20);
        assertThat(result1.string).isEqualTo("twenty");

        MockObject result2 = coll.findOneAndUpdate(
            Filters.eq("_id", "id2"),
            Updates.combine(Updates.set("integer", 30), Updates.set("string", "thirty")),
            new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(false)
        );
        assertThat(result2.integer).isEqualTo(30);
        assertThat(result2.string).isEqualTo("thirty");

        MockObject result3 = coll.findOneAndUpdate(
            Filters.eq("_id", "id3"),
            Updates.pushEach("simpleList", Arrays.asList("1", "2", "3")),
            new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(false)
        );
        assertThat(result3.simpleList).hasSize(5);
        assertThat(result3.simpleList).contains("1", "2", "3");

        coll.removeById("id1");
        coll.removeById("id2");
        coll.removeById("id3");
    }

    @Test
    public void testReplaceOneByNonIdQuery() {
        coll.insert(new MockObject("id1", "ten", 10));

        coll.withWriteConcern(WriteConcern.W1).replaceOne(
            Filters.eq("string", "ten"),
            new MockObject("id1", "twenty", 20)
        );

        MockObject found = coll.findOne(Filters.eq("_id", "id1"));

        assertThat(found).isEqualTo(new MockObject("id1", "twenty", 20));
    }

    @Test
    public void testReplaceOneByUsesQueryNotId() {
        coll.insert(new MockObject("id1", "ten", 10));

        coll.withWriteConcern(WriteConcern.W1).replaceOne(
            Filters.eq("string", "ten"),
            new MockObject(null, "twenty", 20)
        );

        MockObject found = coll.findOne(Filters.eq("_id", "id1"));

        assertThat(found).isEqualTo(new MockObject("id1", "twenty", 20));
    }

    @Test
    public void testReplaceOneUpsertsIfNoDocumentExistsByQueryAndUpsertTrue() {
        coll.replaceOne(
            Filters.eq("string", "ten"),
            new MockObject(null, "twenty", 20),
            new ReplaceOptions().upsert(true)
        );

        MockObject found = coll.findOne(Filters.eq("string", "twenty"));

        assertThat(found).isEqualTo(new MockObject(found._id, "twenty", 20));
    }

    @Test
    public void testReplaceOneDoesNotUpsertIfUpsertFalse() {
        coll.replaceOne(
            Filters.eq("string", "ten"),
            new MockObject(null, "twenty", 20)
        );

        MockObject found = coll.findOne(Filters.eq("string", "twenty"));

        assertThat(found).isNull();
    }

    @Test
    public void testReplaceOneByIdUsesIdProvided() {
        coll.insert(new MockObject("id1", "ten", 10));

        coll.replaceOneById("id1", new MockObject(null, "twenty", 20));

        MockObject found = coll.findOne(Filters.eq("_id", "id1"));

        assertThat(found).isEqualTo(new MockObject("id1", "twenty", 20));
    }

    @Test
    public void testQueryWithLimitedKeys() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        coll.insert(new MockObject("twenty", 20));

        List<MockObject> results = StreamSupport.stream(coll.find(
            new BasicDBObject("string", "ten")
        ).projection(new BasicDBObject("string", true)).spliterator(), false).collect(Collectors.toList());
        assertThat(results).hasSize(2);
        assertThat(results.get(0).integer).isNull();
        assertThat(results.get(0).string).isEqualTo("ten");
        assertThat(results.get(1).integer).isNull();
        assertThat(results.get(1).string).isEqualTo("ten");
    }
}
