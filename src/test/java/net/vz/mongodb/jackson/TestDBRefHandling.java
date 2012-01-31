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

import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class TestDBRefHandling extends MongoDBTestBase {

    @Test
    public void simpleDbRefShouldBeSavedAsDbRef() {
        JacksonDBCollection<Owner, String> coll = getCollection(Owner.class, String.class);
        JacksonDBCollection<Referenced, String> refColl = getCollection(Referenced.class, String.class);

        refColl.insert(new Referenced("hello", 10));
        coll.insert(new Owner(new DBRef<Referenced, String>("hello", refColl.getName())));
        String id = coll.findOne()._id;

        Owner saved = coll.findOneById(id);
        assertThat(saved.ref, notNullValue());
        assertThat(saved.ref.getId(), equalTo("hello"));
        assertThat(saved.ref.getCollectionName(), equalTo(refColl.getName()));

        // Try loading it
        Referenced ref = saved.ref.fetch();
        assertThat(ref, notNullValue());
        assertThat(ref._id, equalTo("hello"));
        assertThat(ref.i, equalTo(10));
    }

    @Test
    public void dbRefWithObjectIdShouldBeSavedAsDbRef() {
        JacksonDBCollection<ObjectIdOwner, String> coll = getCollection(ObjectIdOwner.class, String.class);
        JacksonDBCollection<ObjectIdReferenced, byte[]> refColl = getCollection(ObjectIdReferenced.class, byte[].class);

        byte[] refId = new org.bson.types.ObjectId().toByteArray();
        refColl.insert(new ObjectIdReferenced(refId, 10));
        coll.insert(new ObjectIdOwner(new DBRef<ObjectIdReferenced, byte[]>(refId, refColl.getName())));
        String id = coll.findOne()._id;

        ObjectIdOwner saved = coll.findOneById(id);
        assertThat(saved.ref, notNullValue());
        assertThat(saved.ref.getId(), equalTo(refId));
        assertThat(saved.ref.getCollectionName(), equalTo(refColl.getName()));

        // Try loading it
        ObjectIdReferenced ref = saved.ref.fetch();
        assertThat(ref, notNullValue());
        assertThat(ref._id, equalTo(refId));
        assertThat(ref.i, equalTo(10));
    }

    @Test
    public void testUsingMongoCollectionAnnotation() {
        JacksonDBCollection<Owner, String> coll = getCollection(Owner.class, String.class);
        JacksonDBCollection<Referenced, String> refColl = getCollection(Referenced.class, String.class, "referenced");

        refColl.insert(new Referenced("hello", 10));
        coll.insert(new Owner(new DBRef<Referenced, String>("hello", Referenced.class)));
        String id = coll.findOne()._id;

        Owner saved = coll.findOneById(id);
        assertThat(saved.ref, notNullValue());
        assertThat(saved.ref.getId(), equalTo("hello"));
        assertThat(saved.ref.getCollectionName(), equalTo("referenced"));

        // Try loading it
        Referenced ref = saved.ref.fetch();
        assertThat(ref, notNullValue());
        assertThat(ref._id, equalTo("hello"));
        assertThat(ref.i, equalTo(10));
    }

    public static class Owner {
        public Owner(DBRef<Referenced, String> ref) {
            this.ref = ref;
        }

        public Owner() {
        }

        @ObjectId
        public String _id;
        public DBRef<Referenced, String> ref;
    }

    @MongoCollection(name = "referenced")
    public static class Referenced {
        public Referenced(String _id, int i) {
            this._id = _id;
            this.i = i;
        }

        public Referenced() {
        }

        public String _id;
        public int i;
    }

    public static class ObjectIdOwner {
        public ObjectIdOwner(DBRef<ObjectIdReferenced, byte[]> ref) {
            this.ref = ref;
        }

        public ObjectIdOwner() {
        }

        @ObjectId
        public String _id;
        @ObjectId
        public DBRef<ObjectIdReferenced, byte[]> ref;
    }

    public static class ObjectIdReferenced {
        public ObjectIdReferenced(byte[] id, int i) {
            this._id = id;
            this.i = i;
        }

        public ObjectIdReferenced() {
        }

        @ObjectId
        public byte[] _id;
        public int i;
    }

    @Test
    public void collectionOfDbRefsShouldBeSavedAsDbRefs() {
        JacksonDBCollection<CollectionOwner, String> coll = getCollection(CollectionOwner.class, String.class);
        JacksonDBCollection<Referenced, String> refColl = getCollection(Referenced.class, String.class, "referenced");

        refColl.insert(new Referenced("hello", 10));
        refColl.insert(new Referenced("world", 20));

        CollectionOwner owner = new CollectionOwner();
        owner.list = Arrays.asList(new DBRef<Referenced, String>("hello", refColl.getName()), new DBRef<Referenced, String>("world", refColl.getName()));
        owner._id = "foo";
        coll.insert(owner);

        CollectionOwner saved = coll.findOneById("foo");
        assertThat(saved.list, notNullValue());
        assertThat(saved.list, hasSize(2));
        assertThat(saved.list.get(0).getId(), equalTo("hello"));
        assertThat(saved.list.get(0).getCollectionName(), equalTo(refColl.getName()));
        assertThat(saved.list.get(1).getId(), equalTo("world"));
        assertThat(saved.list.get(1).getCollectionName(), equalTo(refColl.getName()));

        // Try loading them
        Referenced ref = saved.list.get(0).fetch();
        assertThat(ref, notNullValue());
        assertThat(ref._id, equalTo("hello"));
        assertThat(ref.i, equalTo(10));

        ref = saved.list.get(1).fetch();
        assertThat(ref, notNullValue());
        assertThat(ref._id, equalTo("world"));
        assertThat(ref.i, equalTo(20));
    }

    @Test
    public void fetchCollectionOfDBRefsShouldReturnRightResults() {
        JacksonDBCollection<CollectionOwner, String> coll = getCollection(CollectionOwner.class, String.class);
        JacksonDBCollection<Referenced, String> refColl = getCollection(Referenced.class, String.class, "referenced");

        refColl.insert(new Referenced("hello", 10));
        refColl.insert(new Referenced("world", 20));

        CollectionOwner owner = new CollectionOwner();
        owner.list = Arrays.asList(new DBRef<Referenced, String>("hello", refColl.getName()), new DBRef<Referenced, String>("world", refColl.getName()));
        owner._id = "foo";
        coll.insert(owner);

        CollectionOwner saved = coll.findOneById("foo");
        List<Referenced> fetched = coll.fetch(saved.list);
        assertThat(fetched, hasSize(2));
        assertThat(fetched.get(0)._id, equalTo("hello"));
        assertThat(fetched.get(0).i, equalTo(10));
        assertThat(fetched.get(1)._id, equalTo("world"));
        assertThat(fetched.get(1).i, equalTo(20));
    }

    public static class CollectionOwner {
        public String _id;
        public List<DBRef<Referenced, String>> list;
    }

    @Test
    public void collectionOfObjectIdDbRefsShouldBeSavedAsObjectIdDbRefs() {
        JacksonDBCollection<ObjectIdCollectionOwner, String> coll = getCollection(ObjectIdCollectionOwner.class, String.class);
        JacksonDBCollection<ObjectIdReferenced, byte[]> refColl = getCollection(ObjectIdReferenced.class, byte[].class, "referenced");

        byte[] refId1 = new org.bson.types.ObjectId().toByteArray();
        refColl.insert(new ObjectIdReferenced(refId1, 10));
        byte[] refId2 = new org.bson.types.ObjectId().toByteArray();
        refColl.insert(new ObjectIdReferenced(refId2, 20));

        ObjectIdCollectionOwner owner = new ObjectIdCollectionOwner();
        owner.list = Arrays.asList(new DBRef<ObjectIdReferenced, byte[]>(refId1, refColl.getName()), new DBRef<ObjectIdReferenced, byte[]>(refId2, refColl.getName()));
        owner._id = "foo";
        coll.insert(owner);

        ObjectIdCollectionOwner saved = coll.findOneById("foo");
        assertThat(saved.list, notNullValue());
        assertThat(saved.list, hasSize(2));
        assertThat(saved.list.get(0).getId(), equalTo(refId1));
        assertThat(saved.list.get(0).getCollectionName(), equalTo(refColl.getName()));
        assertThat(saved.list.get(1).getId(), equalTo(refId2));
        assertThat(saved.list.get(1).getCollectionName(), equalTo(refColl.getName()));


        // Try loading them
        ObjectIdReferenced ref = saved.list.get(0).fetch();
        assertThat(ref, notNullValue());
        assertThat(ref._id, equalTo(refId1));
        assertThat(ref.i, equalTo(10));

        ref = saved.list.get(1).fetch();
        assertThat(ref, notNullValue());
        assertThat(ref._id, equalTo(refId2));
        assertThat(ref.i, equalTo(20));
    }

    public static class ObjectIdCollectionOwner {
        public String _id;
        @ObjectId
        public List<DBRef<ObjectIdReferenced, byte[]>> list;
    }

}
