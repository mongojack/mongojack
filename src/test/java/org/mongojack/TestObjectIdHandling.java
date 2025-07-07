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

import com.fasterxml.jackson.annotation.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class TestObjectIdHandling extends MongoDBTestBase {

    @Test
    public void testObjectIdGenerated() {

        ObjectIdId object = new ObjectIdId();

        JacksonMongoCollection<ObjectIdId> coll = getCollection(ObjectIdId.class);

        coll.insert(object);
        org.bson.types.ObjectId id = coll.findOne()._id;
        ObjectIdId result = coll.findOneById(id);
        assertThat(result._id).isEqualTo(id);
        assertThat(getUnderlyingCollection(coll).find().first().get("_id")).isEqualTo(id);
    }

    @Test
    public void testObjectIdSaved() {
        ObjectIdId object = new ObjectIdId();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;

        JacksonMongoCollection<ObjectIdId> coll = getCollection(ObjectIdId.class);

        coll.insert(object);
        ObjectIdId result = coll.findOneById(id);
        assertThat(result._id).isEqualTo(id);
        assertThat(getUnderlyingCollection(coll).find().first().get("_id")).isEqualTo(id);
    }

    public static class ObjectIdId {
        public org.bson.types.ObjectId _id;
    }

    @Test
    public void testObjectIdAnnotationOnStringGenerated() {
        StringId object = new StringId();

        JacksonMongoCollection<StringId> coll = getCollection(StringId.class);

        coll.insert(object);
        String id = coll.findOne()._id;
        // Check that it's a valid object id
        assertThat(org.bson.types.ObjectId.isValid(id)).isTrue();
        StringId result = coll.findOneById(id);
        assertThat(result._id).isEqualTo(id);
        assertThat(getUnderlyingCollection(coll).find().first().get("_id").toString()).isEqualTo(id);
    }

    @Test
    public void testObjectIdAnnotationOnStringSaved() {
        StringId object = new StringId();
        String id = new org.bson.types.ObjectId().toString();
        object._id = id;

        JacksonMongoCollection<StringId> coll = getCollection(StringId.class);

        coll.insert(object);
        StringId result = coll.findOneById(id);
        assertThat(result._id).isEqualTo(id);
        assertThat(getUnderlyingCollection(coll).find().first().get("_id").toString()).isEqualTo(id);
    }

    @Test
    public void testRemoveByIdWithObjectId() {
        JacksonMongoCollection<StringId> coll = getCollection(StringId.class);
        coll.insert(new StringId());
        String id = coll.findOne()._id;
        coll.insert(new StringId());
        assertThat(coll.find().into(new ArrayList<>())).hasSize(2);
        coll.removeById(id);
        List<StringId> results = coll.find().into(new ArrayList<>());
        assertThat(results).hasSize(1);
        assertThat(results.get(0)._id).isNotEqualTo(id);
    }

    @Test
    public void testFindOneByIdWithObjectId() {
        JacksonMongoCollection<StringId> coll = getCollection(StringId.class);
        StringId object = new StringId();
        coll.insert(object);
        assertThat(getUnderlyingCollection(coll).find().first().get("_id")).isInstanceOf(org.bson.types.ObjectId.class);
        String id = coll.findOne()._id;
        assertThat(id).isInstanceOf(String.class);
        StringId result = coll.findOneById(id);
        assertThat(result._id).isEqualTo(id);
    }

    public static class StringId {
        @org.mongojack.ObjectId
        public String _id;
    }

    @Test
    public void testObjectIdAnnotationOnByteArrayGenerated() {
        ByteArrayId object = new ByteArrayId();

        JacksonMongoCollection<ByteArrayId> coll = getCollection(ByteArrayId.class);

        coll.insert(object);
        byte[] id = coll.findOne()._id;
        // Check that it's a valid object id, should be 12 bytes
        assertThat(id.length).isEqualTo(12);
        ByteArrayId result = coll.findOneById(id);
        assertThat(result._id).isEqualTo(id);
    }

    @Test
    public void testObjectIdAnnotationOnByteArraySaved() {
        ByteArrayId object = new ByteArrayId();
        byte[] id = new org.bson.types.ObjectId().toByteArray();
        object._id = id;

        JacksonMongoCollection<ByteArrayId> coll = getCollection(ByteArrayId.class);

        coll.insert(object);
        ByteArrayId result = coll.findOneById(id);
        assertThat(result._id).isEqualTo(id);
    }

    public static class ByteArrayId {
        @org.mongojack.ObjectId
        public byte[] _id;
    }

    @Test
    public void testCollectionOfObjectIds() {
        ObjectIdCollection object = new ObjectIdCollection();
        object._id = "id";
        object.list = Arrays.asList(org.bson.types.ObjectId.get(), org.bson.types.ObjectId.get());

        JacksonMongoCollection<ObjectIdCollection> coll = getCollection(ObjectIdCollection.class);
        coll.insert(object);

        ObjectIdCollection result = coll.findOneById("id");
        assertThat(result.list).isEqualTo(object.list);
    }

    public static class ObjectIdCollection {
        public String _id;
        public List<org.bson.types.ObjectId> list;
    }

    @Test
    public void testCollectionOfObjectIdStrings() {
        StringIdCollection object = new StringIdCollection();
        object._id = "id";
        object.list = Arrays.asList(org.bson.types.ObjectId.get().toString(), org.bson.types.ObjectId.get().toString());

        JacksonMongoCollection<StringIdCollection> coll = getCollection(StringIdCollection.class);
        coll.insert(object);

        StringIdCollection result = coll.findOneById("id");
        assertThat(result.list).isEqualTo(object.list);
    }

    public static class StringIdCollection {
        public String _id;
        @org.mongojack.ObjectId
        public List<String> list;
    }

    @Test
    public void testCollectionOfObjectIdByteArrays() {
        ByteArrayIdCollection object = new ByteArrayIdCollection();
        object._id = "id";
        object.list = Arrays.asList(org.bson.types.ObjectId.get().toByteArray(), org.bson.types.ObjectId.get()
            .toByteArray());

        JacksonMongoCollection<ByteArrayIdCollection> coll = getCollection(ByteArrayIdCollection.class);
        coll.insert(object);

        ByteArrayIdCollection result = coll.findOneById("id");
        assertThat(result.list).hasSize(2);
        assertThat(result.list.get(0)).isEqualTo(object.list.get(0));
        assertThat(result.list.get(1)).isEqualTo(object.list.get(1));
    }

    public static class ByteArrayIdCollection {
        public String _id;
        @org.mongojack.ObjectId
        public List<byte[]> list;
    }

    public static class StringIdMethods {
        private String _id;

        @org.mongojack.ObjectId
        @Id
        public String getId() {
            return _id;
        }

        @org.mongojack.ObjectId
        @Id
        public void setId(String key) {
            _id = key;
        }
    }

    @Test
    public void testObjectIdAnnotationOnMethodsGenerated() {
        StringIdMethods object = new StringIdMethods();

        JacksonMongoCollection<StringIdMethods> coll = getCollection(StringIdMethods.class);

        coll.insert(object);
        String id = coll.findOne().getId();
        // Check that it's a valid object id
        assertThat(org.bson.types.ObjectId.isValid(id)).isTrue();
        StringIdMethods result = coll.findOneById(id);
        assertThat(result.getId()).isEqualTo(id);
        assertThat(getUnderlyingCollection(coll).find().first().get("_id").toString()).isEqualTo(id);
    }

    @Test
    public void testObjectIdAnnotationOnMethodsSaved() {
        StringIdMethods object = new StringIdMethods();
        String id = new org.bson.types.ObjectId().toString();
        object.setId(id);

        JacksonMongoCollection<StringIdMethods> coll = getCollection(StringIdMethods.class);

        coll.insert(object);
        StringIdMethods result = coll.findOneById(id);
        assertThat(result.getId()).isEqualTo(id);
        assertThat(getUnderlyingCollection(coll).find().first().get("_id").toString()).isEqualTo(id);
    }

    public static class ByteArrayIdMethods {
        private byte[] _id;

        @org.mongojack.ObjectId
        @Id
        public byte[] getId() {
            return _id;
        }

        @org.mongojack.ObjectId
        @Id
        public void setId(byte[] key) {
            _id = key;
        }
    }

    @Test
    public void testByteArrayObjectIdMethods() {
        ByteArrayIdMethods object = new ByteArrayIdMethods();
        JacksonMongoCollection<ByteArrayIdMethods> coll = getCollection(ByteArrayIdMethods.class);
        coll.insert(object);
        byte[] id = coll.findOne().getId();
        // Check that it's a valid object id
        new ObjectId(id);
        ByteArrayIdMethods result = coll.findOneById(id);
        assertThat(result.getId()).isEqualTo(id);
        assertThat(((ObjectId) getUnderlyingCollection(coll).find().first().get("_id")).toByteArray()).isEqualTo(id);
    }

    @Test
    public void testObjectIdAnnotationOnConvertedSaved() {
        ConvertedId object = new ConvertedId();
        ConvertibleId id = new ConvertibleId(UUID.randomUUID().toString());
        object._id = id;

        JacksonMongoCollection<ConvertedId> coll = getCollection(ConvertedId.class);

        coll.insert(object);
        ConvertedId result = coll.findOneById(id);
        assertThat(result._id).isEqualTo(id);
        assertThat(getUnderlyingCollection(coll).find().first().get("_id").toString()).isEqualTo(id.getValue());
    }

    @Test
    public void testRemoveByIdWithConvertedId() {
        JacksonMongoCollection<ConvertedId> coll = getCollection(ConvertedId.class);
        {
            ConvertedId object = new ConvertedId();
            object._id = new ConvertibleId(UUID.randomUUID().toString());

            coll.insert(object);
        }
        ConvertibleId id = coll.findOne()._id;
        {
            ConvertedId object = new ConvertedId();
            object._id = new ConvertibleId(UUID.randomUUID().toString());

            coll.insert(object);
        }
        assertThat(coll.find().into(new ArrayList<>())).hasSize(2);
        coll.removeById(id);
        List<ConvertedId> results = coll.find().into(new ArrayList<>());
        assertThat(results).hasSize(1);
        assertThat(results.get(0)._id).isNotEqualTo(id);
    }

    @Test
    public void testFindOneByIdWithConvertedId() {
        JacksonMongoCollection<ConvertedId> coll = getCollection(ConvertedId.class);
        ConvertedId object = new ConvertedId();
        object._id = new ConvertibleId(UUID.randomUUID().toString());

        coll.insert(object);
        assertThat(getUnderlyingCollection(coll).find().first().get("_id")).isInstanceOf(String.class);
        ConvertibleId id = coll.findOne()._id;
        assertThat(id).isInstanceOf(ConvertibleId.class);
        ConvertedId result = coll.findOneById(id);
        assertThat(result._id).isEqualTo(id);
    }

    public static class ConvertibleId {

        private final String value;

        @JsonCreator
        public ConvertibleId(final String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final ConvertibleId that = (ConvertibleId) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    public static class ConvertedId {
        @Id
        public ConvertibleId _id;
    }

    @Test
    public void testObjectIdAnnotationOnComplexSaved() {
        ObjectWithComplexId object = new ObjectWithComplexId();
        ComplexId id = new ComplexId(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        object._id = id;

        JacksonMongoCollection<ObjectWithComplexId> coll = getCollection(ObjectWithComplexId.class);

        coll.insert(object);
        ObjectWithComplexId result = coll.findOneById(id);
        assertThat(result._id).isEqualTo(id);
        assertThat(getUnderlyingCollection(coll).find().first().get("_id", Document.class).get("value1", String.class)).isEqualTo(id.getValue1());
        assertThat(getUnderlyingCollection(coll).find().first().get("_id", Document.class).get("value2", String.class)).isEqualTo(id.getValue2());
    }

    @Test
    public void testRemoveByIdWithComplexId() {
        JacksonMongoCollection<ObjectWithComplexId> coll = getCollection(ObjectWithComplexId.class);
        {
            ObjectWithComplexId object = new ObjectWithComplexId();
            object._id = new ComplexId(UUID.randomUUID().toString(), UUID.randomUUID().toString());

            coll.insert(object);
        }
        ComplexId id = coll.findOne()._id;
        {
            ObjectWithComplexId object = new ObjectWithComplexId();
            object._id = new ComplexId(UUID.randomUUID().toString(), UUID.randomUUID().toString());

            coll.insert(object);
        }
        assertThat(coll.find().into(new ArrayList<>())).hasSize(2);
        coll.removeById(id);
        List<ObjectWithComplexId> results = coll.find().into(new ArrayList<>());
        assertThat(results).hasSize(1);
        assertThat(results.get(0)._id).isNotEqualTo(id);
    }

    @Test
    public void testFindOneByIdWithComplexId() {
        JacksonMongoCollection<ObjectWithComplexId> coll = getCollection(ObjectWithComplexId.class);
        ObjectWithComplexId object = new ObjectWithComplexId();
        object._id = new ComplexId(UUID.randomUUID().toString(), UUID.randomUUID().toString());

        coll.insert(object);
        assertThat(getUnderlyingCollection(coll).find().first().get("_id")).isInstanceOf(Document.class);
        ComplexId id = coll.findOne()._id;
        assertThat(id).isInstanceOf(ComplexId.class);
        ObjectWithComplexId result = coll.findOneById(id);
        assertThat(result._id).isEqualTo(id);
    }

    @SuppressWarnings("unused")
    public static class ComplexId {

        private String value1;

        private String value2;

        public ComplexId() {
        }

        public ComplexId(final String value1, final String value2) {
            this.value1 = value1;
            this.value2 = value2;
        }

        public String getValue1() {
            return value1;
        }

        public void setValue1(final String value1) {
            this.value1 = value1;
        }

        public String getValue2() {
            return value2;
        }

        public void setValue2(final String value2) {
            this.value2 = value2;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final ComplexId complexId = (ComplexId) o;
            return Objects.equals(value1, complexId.value1) && Objects.equals(value2, complexId.value2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value1, value2);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ComplexId.class.getSimpleName() + "[", "]")
                .add("value1='" + value1 + "'")
                .add("value2='" + value2 + "'")
                .toString();
        }
    }

    public static class ObjectWithComplexId {

        @Id
        public ComplexId _id;

    }

    @Test
    public void testObjectIdAnnotationOnUuidSaved() {
        ObjectWithUuidId object = new ObjectWithUuidId();
        UUID id = UUID.randomUUID();
        object._id = id;

        JacksonMongoCollection<ObjectWithUuidId> coll = getCollection(ObjectWithUuidId.class);

        coll.insert(object);
        ObjectWithUuidId result = coll.findOneById(id);
        assertThat(result._id).isEqualTo(id);
        assertThat(getUnderlyingCollection(coll).find().first().get("_id", UUID.class)).isEqualTo(id);
    }

    @Test
    public void testRemoveByIdWithUuidId() {
        JacksonMongoCollection<ObjectWithUuidId> coll = getCollection(ObjectWithUuidId.class);
        {
            ObjectWithUuidId object = new ObjectWithUuidId();
            object._id = UUID.randomUUID();

            coll.insert(object);
        }
        UUID id = coll.findOne()._id;
        {
            ObjectWithUuidId object = new ObjectWithUuidId();
            object._id = UUID.randomUUID();

            coll.insert(object);
        }
        assertThat(coll.find().into(new ArrayList<>())).hasSize(2);
        coll.removeById(id);
        List<ObjectWithUuidId> results = coll.find().into(new ArrayList<>());
        assertThat(results).hasSize(1);
        assertThat(results.get(0)._id).isNotEqualTo(id);
    }

    @Test
    public void testFindOneByIdWithUuidId() {
        JacksonMongoCollection<ObjectWithUuidId> coll = getCollection(ObjectWithUuidId.class);
        ObjectWithUuidId object = new ObjectWithUuidId();
        object._id = UUID.randomUUID();

        coll.insert(object);
        assertThat(getUnderlyingCollection(coll).find().first().get("_id")).isInstanceOf(UUID.class);
        UUID id = coll.findOne()._id;
        assertThat(id).isInstanceOf(UUID.class);
        ObjectWithUuidId result = coll.findOneById(id);
        assertThat(result._id).isEqualTo(id);
    }

    public static class ObjectWithUuidId {

        @Id
        public UUID _id;

    }

    @Test
    public void testObjectIdWithNoSetterSaved() {
        ObjectWithConstructorOnlyObjectId object = new ObjectWithConstructorOnlyObjectId(null);

        JacksonMongoCollection<ObjectWithConstructorOnlyObjectId> coll = getCollection(ObjectWithConstructorOnlyObjectId.class);

        coll.insert(object);

        assertThat(object.getId()).isNotNull();

        ObjectWithConstructorOnlyObjectId result = coll.findOne();
        assertThat(result.getId()).isNotNull();
    }

    public static class ObjectWithConstructorOnlyObjectId {

        @JsonIgnore
        private ObjectId id;

        @JsonCreator
        public ObjectWithConstructorOnlyObjectId(@Id  ObjectId id) {
            this.id = id;
        }

        @JsonGetter
        @Id
        public ObjectId getId() {
            return id;
        }

        @JsonSetter
        @Id
        public void setId(ObjectId id) {
            this.id = id;
        }
    }

}
