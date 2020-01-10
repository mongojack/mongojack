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

import org.bson.types.ObjectId;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class TestObjectIdHandling extends MongoDBTestBase {

    @Test
    public void testObjectIdGenerated() {
        
        ObjectIdId object = new ObjectIdId();

        JacksonMongoCollection<ObjectIdId> coll = getCollection(ObjectIdId.class);

        coll.insert(object);
        org.bson.types.ObjectId id = coll.findOne()._id;
        ObjectIdId result = coll.findOneById(id);
        assertThat(result._id, equalTo(id));
        assertThat(getUnderlyingCollection(coll).find().first().get("_id"), equalTo(id));
    }

    @Test
    public void testObjectIdSaved() {
        ObjectIdId object = new ObjectIdId();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;

        JacksonMongoCollection<ObjectIdId> coll = getCollection(ObjectIdId.class);

        coll.insert(object);
        ObjectIdId result = coll.findOneById(id);
        assertThat(result._id, equalTo(id));
        assertThat(getUnderlyingCollection(coll).find().first().get("_id"), equalTo(id));
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
        assertTrue(org.bson.types.ObjectId.isValid(id));
        StringId result = coll.findOneById(id);
        assertThat(result._id, equalTo(id));
        assertThat(getUnderlyingCollection(coll).find().first().get("_id").toString(), equalTo(id));
    }

    @Test
    public void testObjectIdAnnotationOnStringSaved() {
        StringId object = new StringId();
        String id = new org.bson.types.ObjectId().toString();
        object._id = id;

        JacksonMongoCollection<StringId> coll = getCollection(StringId.class);

        coll.insert(object);
        StringId result = coll.findOneById(id);
        assertThat(result._id, equalTo(id));
        assertThat(getUnderlyingCollection(coll).find().first().get("_id").toString(), equalTo(id));
    }

    @Test
    public void testRemoveByIdWithObjectId() {
        JacksonMongoCollection<StringId> coll = getCollection(StringId.class);
        coll.insert(new StringId());
        String id = coll.findOne()._id;
        coll.insert(new StringId());
        assertThat(coll.find().into(new ArrayList<>()), hasSize(2));
        coll.removeById(id);
        List<StringId> results = coll.find().into(new ArrayList<>());
        assertThat(results, hasSize(1));
        assertThat(results.get(0)._id, not(Matchers.equalTo(id)));
    }

    @Test
    public void testFindOneByIdWithObjectId() {
        JacksonMongoCollection<StringId> coll = getCollection(StringId.class);
        StringId object = new StringId();
        coll.insert(object);
        assertThat(getUnderlyingCollection(coll).find().first().get("_id"), instanceOf(org.bson.types.ObjectId.class));
        String id = coll.findOne()._id;
        assertThat(id, instanceOf(String.class));
        StringId result = coll.findOneById(id);
        assertThat(result._id, Matchers.equalTo(id));
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
        assertEquals(12, id.length);
        ByteArrayId result = coll.findOneById(id);
        assertThat(result._id, equalTo(id));
    }

    @Test
    public void testObjectIdAnnotationOnByteArraySaved() {
        ByteArrayId object = new ByteArrayId();
        byte[] id = new org.bson.types.ObjectId().toByteArray();
        object._id = id;

        JacksonMongoCollection<ByteArrayId> coll = getCollection(ByteArrayId.class);

        coll.insert(object);
        ByteArrayId result = coll.findOneById(id);
        assertThat(result._id, equalTo(id));
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
        assertThat(result.list, equalTo(object.list));
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
        assertThat(result.list, equalTo(object.list));
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
        assertThat(result.list, hasSize(2));
        assertThat(result.list.get(0), equalTo(object.list.get(0)));
        assertThat(result.list.get(1), equalTo(object.list.get(1)));
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
        assertTrue(org.bson.types.ObjectId.isValid(id));
        StringIdMethods result = coll.findOneById(id);
        assertThat(result.getId(), equalTo(id));
        assertThat(getUnderlyingCollection(coll).find().first().get("_id").toString(), equalTo(id));
    }

    @Test
    public void testObjectIdAnnotationOnMethodsSaved() {
        StringIdMethods object = new StringIdMethods();
        String id = new org.bson.types.ObjectId().toString();
        object.setId(id);

        JacksonMongoCollection<StringIdMethods> coll = getCollection(StringIdMethods.class);

        coll.insert(object);
        StringIdMethods result = coll.findOneById(id);
        assertThat(result.getId(), equalTo(id));
        assertThat(getUnderlyingCollection(coll).find().first().get("_id").toString(), equalTo(id));
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
        assertThat(result.getId(), equalTo(id));
        assertThat(((ObjectId)getUnderlyingCollection(coll).find().first().get("_id")).toByteArray(), equalTo(id));
    }
}
