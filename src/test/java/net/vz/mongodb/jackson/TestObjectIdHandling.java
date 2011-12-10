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

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestObjectIdHandling extends MongoDBTestBase {

    @Test
    public void testObjectIdGenerated() {
        ObjectIdId object = new ObjectIdId();

        JacksonDBCollection<ObjectIdId, org.bson.types.ObjectId> coll = getCollection(ObjectIdId.class,
                org.bson.types.ObjectId.class);

        org.bson.types.ObjectId id = coll.insert(object).getSavedId();
        ObjectIdId result = coll.findOneById(id);
        assertThat(result._id, equalTo(id));
    }

    @Test
    public void testObjectIdSaved() {
        ObjectIdId object = new ObjectIdId();
        org.bson.types.ObjectId id = new org.bson.types.ObjectId();
        object._id = id;

        JacksonDBCollection<ObjectIdId, org.bson.types.ObjectId> coll = getCollection(ObjectIdId.class,
                org.bson.types.ObjectId.class);

        coll.insert(object);
        ObjectIdId result = coll.findOneById(id);
        assertThat(result._id, equalTo(id));
    }

    public static class ObjectIdId {
        public org.bson.types.ObjectId _id;
    }

    @Test
    public void testObjectIdAnnotationOnStringGenerated() {
        StringId object = new StringId();

        JacksonDBCollection<StringId, String> coll = getCollection(StringId.class, String.class);

        String id = coll.insert(object).getSavedId();
        // Check that it's a valid object id
        assertTrue(org.bson.types.ObjectId.isValid(id));
        StringId result = coll.findOneById(id);
        assertThat(result._id, equalTo(id));
    }

    @Test
    public void testObjectIdAnnotationOnStringSaved() {
        StringId object = new StringId();
        String id = new org.bson.types.ObjectId().toString();
        object._id = id;

        JacksonDBCollection<StringId, String> coll = getCollection(StringId.class, String.class);

        coll.insert(object);
        StringId result = coll.findOneById(id);
        assertThat(result._id, equalTo(id));
    }

    @Test
    public void testRemoveByIdWithObjectId() {
        JacksonDBCollection<StringId, String> coll = getCollection(StringId.class, String.class);
        String id = coll.insert(new StringId()).getSavedId();
        coll.insert(new StringId());
        assertThat(coll.find().toArray(), hasSize(2));
        coll.removeById(id);
        List<StringId> results = coll.find().toArray();
        assertThat(results, hasSize(1));
        assertThat(results.get(0)._id, not(Matchers.equalTo(id)));
    }

    @Test
    public void testFindOneByIdWithObjectId() {
        JacksonDBCollection<StringId, String> coll = getCollection(StringId.class, String.class);
        StringId object = new StringId();
        net.vz.mongodb.jackson.WriteResult<StringId, String> writeResult = coll.insert(object);
        assertThat(writeResult.getDbObject().get("_id"), instanceOf(org.bson.types.ObjectId.class));
        String id = writeResult.getSavedId();
        assertThat(id, instanceOf(String.class));
        StringId result = coll.findOneById(id);
        assertThat(result._id, Matchers.equalTo(id));
    }

    public static class StringId {
        @ObjectId
        public String _id;
    }

    @Test
    public void testObjectIdAnnotationOnByteArrayGenerated() {
        ByteArrayId object = new ByteArrayId();

        JacksonDBCollection<ByteArrayId, byte[]> coll = getCollection(ByteArrayId.class, byte[].class);

        byte[] id = coll.insert(object).getSavedId();
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

        JacksonDBCollection<ByteArrayId, byte[]> coll = getCollection(ByteArrayId.class, byte[].class);

        coll.insert(object);
        ByteArrayId result = coll.findOneById(id);
        assertThat(result._id, equalTo(id));
    }

    public static class ByteArrayId {
        @ObjectId
        public byte[] _id;
    }
}
