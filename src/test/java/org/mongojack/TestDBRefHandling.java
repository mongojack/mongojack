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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class TestDBRefHandling extends MongoDBTestBase {

    private DbReferenceManager manager;

    @BeforeEach
    public void setUp() {
        manager = new DbReferenceManager(mongo, db.getName(), uuidRepresentation);
    }

    @Test
    public void simpleDbRefShouldBeSavedAsDbRef() {
        JacksonMongoCollection<Owner> coll = getCollection(Owner.class);
        JacksonMongoCollection<Referenced> refColl = getCollection(Referenced.class);

        refColl.insert(new Referenced("hello", 10));
        coll.insert(new Owner(new DBRef<>("hello", Referenced.class, refColl.getName(), refColl.getDatabaseName())));
        String id = coll.findOne()._id;

        Owner saved = coll.findOneById(id);
        assertThat(saved.ref).isNotNull();
        assertThat(saved.ref.getId()).isEqualTo("hello");
        assertThat(saved.ref.getCollectionName()).isEqualTo(refColl.getName());

        // Try loading it
        Referenced ref = manager.fetch(saved.ref);
        assertThat(ref).isNotNull();
        assertThat(ref._id).isEqualTo("hello");
        assertThat(ref.i).isEqualTo(10);
    }

    @Test
    public void dbRefWithObjectIdShouldBeSavedAsDbRef() {
        JacksonMongoCollection<ObjectIdOwner> coll = getCollection(ObjectIdOwner.class);
        JacksonMongoCollection<ObjectIdReferenced> refColl = getCollection(ObjectIdReferenced.class);

        byte[] refId = new org.bson.types.ObjectId().toByteArray();
        refColl.insert(new ObjectIdReferenced(refId, 10));
        coll.insert(new ObjectIdOwner(new DBRef<>(refId, ObjectIdReferenced.class, refColl.getName(), refColl.getDatabaseName())));
        String id = coll.findOne()._id;

        ObjectIdOwner saved = coll.findOneById(id);
        assertThat(saved.ref).isNotNull();
        assertThat(saved.ref.getId()).isEqualTo(refId);
        assertThat(saved.ref.getCollectionName()).isEqualTo(refColl.getName());

        // Try loading it
        ObjectIdReferenced ref = manager.fetch(saved.ref);
        assertThat(ref).isNotNull();
        assertThat(ref._id).isEqualTo(refId);
        assertThat(ref.i).isEqualTo(10);
    }

    @Test
    public void testUsingMongoCollectionAnnotation() {
        JacksonMongoCollection<Owner> coll = getCollection(Owner.class);
        JacksonMongoCollection<Referenced> refColl = getCollection(Referenced.class, "referenced");

        refColl.insert(new Referenced("hello", 10));
        coll.insert(new Owner(new DBRef<>(
            "hello",
            Referenced.class
        )));
        String id = coll.findOne()._id;

        Owner saved = coll.findOneById(id);
        assertThat(saved.ref).isNotNull();
        assertThat(saved.ref.getId()).isEqualTo("hello");
        assertThat(saved.ref.getCollectionName()).isEqualTo("referenced");

        // Try loading it
        Referenced ref = manager.fetch(saved.ref);
        assertThat(ref).isNotNull();
        assertThat(ref._id).isEqualTo("hello");
        assertThat(ref.i).isEqualTo(10);
    }

    @Test
    public void testDBUpdateWithDbRef() {
        JacksonMongoCollection<Owner> coll = getCollection(Owner.class);
        coll.insert(new Owner());
        String id = coll.findOne()._id;

        coll.updateById(id, DBUpdate.set("ref", new DBRef<>(
            "hello", Referenced.class)));
        assertThat(coll.findOneById(id).ref).isNotNull();
        assertThat(coll.findOneById(id).ref.getId()).isEqualTo("hello");
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
            _id = id;
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
        JacksonMongoCollection<CollectionOwner> coll = getCollection(CollectionOwner.class);
        JacksonMongoCollection<Referenced> refColl = getCollection(Referenced.class, "referenced");

        refColl.insert(new Referenced("hello", 10));
        refColl.insert(new Referenced("world", 20));

        CollectionOwner owner = new CollectionOwner();
        owner.list = Arrays.asList(new DBRef<>("hello",
            Referenced.class, refColl.getName(), refColl.getDatabaseName()
        ), new DBRef<>("world",
            Referenced.class, refColl.getName(), refColl.getDatabaseName()
        ));
        owner._id = "foo";
        coll.insert(owner);

        CollectionOwner saved = coll.findOneById("foo");
        assertThat(saved.list).isNotNull();
        assertThat(saved.list).hasSize(2);
        assertThat(saved.list.get(0).getId()).isEqualTo("hello");
        assertThat(saved.list.get(0).getCollectionName()).isEqualTo(refColl.getName());
        assertThat(saved.list.get(1).getId()).isEqualTo("world");
        assertThat(saved.list.get(1).getCollectionName()).isEqualTo(refColl.getName());

        // Try loading them
        Referenced ref = manager.fetch(saved.list.get(0));
        assertThat(ref).isNotNull();
        assertThat(ref._id).isEqualTo("hello");
        assertThat(ref.i).isEqualTo(10);

        ref = manager.fetch(saved.list.get(1));
        assertThat(ref).isNotNull();
        assertThat(ref._id).isEqualTo("world");
        assertThat(ref.i).isEqualTo(20);
    }

    @Test
    public void fetchCollectionOfDBRefsShouldReturnRightResults() {
        JacksonMongoCollection<CollectionOwner> coll = getCollection(CollectionOwner.class);
        JacksonMongoCollection<Referenced> refColl = getCollection(Referenced.class, "referenced");

        refColl.insert(new Referenced("hello", 10));
        refColl.insert(new Referenced("world", 20));

        CollectionOwner owner = new CollectionOwner();
        owner.list = Arrays.asList(new DBRef<>("hello",
            Referenced.class, refColl.getName(), refColl.getDatabaseName()
        ), new DBRef<>("world",
            Referenced.class, refColl.getName(), refColl.getDatabaseName()
        ));
        owner._id = "foo";
        coll.insert(owner);

        CollectionOwner saved = coll.findOneById("foo");
        List<Referenced> fetched = manager.fetch(saved.list);
        assertThat(fetched).hasSize(2);
        assertThat(fetched.get(0)._id).isEqualTo("hello");
        assertThat(fetched.get(0).i).isEqualTo(10);
        assertThat(fetched.get(1)._id).isEqualTo("world");
        assertThat(fetched.get(1).i).isEqualTo(20);
    }

    public static class CollectionOwner {
        public String _id;
        public List<DBRef<Referenced, String>> list;
    }

    @Test
    public void collectionOfObjectIdDbRefsShouldBeSavedAsObjectIdDbRefs() {
        JacksonMongoCollection<ObjectIdCollectionOwner> coll = getCollection(ObjectIdCollectionOwner.class);
        JacksonMongoCollection<ObjectIdReferenced> refColl = getCollection(ObjectIdReferenced.class, "referenced");

        byte[] refId1 = new org.bson.types.ObjectId().toByteArray();
        refColl.insert(new ObjectIdReferenced(refId1, 10));
        byte[] refId2 = new org.bson.types.ObjectId().toByteArray();
        refColl.insert(new ObjectIdReferenced(refId2, 20));

        ObjectIdCollectionOwner owner = new ObjectIdCollectionOwner();
        owner.list = Arrays
            .asList(new DBRef<>(refId1, ObjectIdReferenced.class, refColl
                .getName(), refColl.getDatabaseName()), new DBRef<>(
                refId2, ObjectIdReferenced.class, refColl.getName(), refColl.getDatabaseName()));
        owner._id = "foo";
        coll.insert(owner);

        ObjectIdCollectionOwner saved = coll.findOneById("foo");
        assertThat(saved.list).isNotNull();
        assertThat(saved.list).hasSize(2);
        assertThat(saved.list.get(0).getId()).isEqualTo(refId1);
        assertThat(saved.list.get(0).getCollectionName()).isEqualTo(refColl.getName());
        assertThat(saved.list.get(1).getId()).isEqualTo(refId2);
        assertThat(saved.list.get(1).getCollectionName()).isEqualTo(refColl.getName());

        // Try loading them
        ObjectIdReferenced ref = manager.fetch(saved.list.get(0));
        assertThat(ref).isNotNull();
        assertThat(ref._id).isEqualTo(refId1);
        assertThat(ref.i).isEqualTo(10);

        ref = manager.fetch(saved.list.get(1));
        assertThat(ref).isNotNull();
        assertThat(ref._id).isEqualTo(refId2);
        assertThat(ref.i).isEqualTo(20);
    }

    public static class ObjectIdCollectionOwner {
        public String _id;
        @ObjectId
        public List<DBRef<ObjectIdReferenced, byte[]>> list;
    }

}
