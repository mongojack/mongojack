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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestDBRefHandling extends MongoDBTestBase {
    private final boolean useStreamDeserialization;

    @Parameterized.Parameters
    public static List<Object[]> getParameters() {
        return Arrays.asList(new Object[]{true}, new Object[]{false});
    }

    public TestDBRefHandling(boolean useStreamDeserialization) {
        this.useStreamDeserialization = useStreamDeserialization;
    }

    @Test
    public void simpleDbRefShouldBeSavedAsDbRef() {
        JacksonDBCollection<Owner, String> coll = getCollection(Owner.class, String.class);
        JacksonDBCollection<Referenced, String> refColl = getCollection(Referenced.class, String.class);

        refColl.insert(new Referenced("hello", 10));
        String id = coll.insert(new Owner(new DBRef<Referenced, String>("hello", refColl.getName()))).getSavedId();

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
        JacksonDBCollection<ObjectIdReferenced, String> refColl = getCollection(ObjectIdReferenced.class, String.class);

        String refId = refColl.insert(new ObjectIdReferenced(10)).getSavedId();
        String id = coll.insert(new ObjectIdOwner(new DBRef<ObjectIdReferenced, String>(refId, refColl.getName()))).getSavedId();

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
        String id = coll.insert(new Owner(new DBRef<Referenced, String>("hello", Referenced.class))).getSavedId();

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
        public ObjectIdOwner(DBRef<ObjectIdReferenced, String> ref) {
            this.ref = ref;
        }

        public ObjectIdOwner() {
        }

        @ObjectId
        public String _id;
        @ObjectId
        public DBRef<ObjectIdReferenced, String> ref;
    }

    public static class ObjectIdReferenced {
        public ObjectIdReferenced(int i) {
            this.i = i;
        }

        public ObjectIdReferenced() {
        }

        @ObjectId
        public String _id;
        public int i;
    }

    private <T, K> JacksonDBCollection<T, K> getCollection(Class<T> type, Class<K> keyType) {
        JacksonDBCollection<T, K> coll = JacksonDBCollection.wrap(getCollection(), type, keyType);
        if (useStreamDeserialization) {
            coll.enable(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION);
        } else {
            coll.disable(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION);
        }
        return coll;
    }

    private <T, K> JacksonDBCollection<T, K> getCollection(Class<T> type, Class<K> keyType, String collectionName) {
        JacksonDBCollection<T, K> coll = JacksonDBCollection.wrap(getCollection(collectionName), type, keyType);
        if (useStreamDeserialization) {
            coll.enable(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION);
        } else {
            coll.disable(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION);
        }
        return coll;
    }

}
