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

import org.codehaus.jackson.annotate.JsonCreator;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class TestIdAnnotatedClass extends MongoDBTestBase {

    private <T, K> JacksonDBCollection<T, K> createCollFor(T object, Class<K> keyType) throws Exception {
        // Stupid generics...
        return (JacksonDBCollection) getCollection(object.getClass(), keyType);
    }

    @Test
    public void testIdFieldAnnotated() throws Exception {
        IdFieldAnnotated o = new IdFieldAnnotated();
        o.id = "blah";
        JacksonDBCollection<IdFieldAnnotated, String> coll = createCollFor(o, String.class);
        WriteResult<IdFieldAnnotated, String> writeResult = coll.insert(o);
        assertThat(writeResult.getSavedId(), equalTo("blah"));
        assertThat(writeResult.getDbObject().get("id"), nullValue());
        IdFieldAnnotated result = coll.findOneById("blah");
        assertThat(result, notNullValue());
        assertThat(result.id, equalTo("blah"));
    }

    public static class IdFieldAnnotated {
        @Id
        public String id;
    }

    @Test
    public void testJpaIdFieldAnnotated() throws Exception {
        JpaIdFieldAnnotated o = new JpaIdFieldAnnotated();
        o.id = "blah";
        JacksonDBCollection<JpaIdFieldAnnotated, String> coll = createCollFor(o, String.class);
        WriteResult<JpaIdFieldAnnotated, String> writeResult = coll.insert(o);
        assertThat(writeResult.getSavedId(), equalTo("blah"));
        assertThat(writeResult.getDbObject().get("id"), nullValue());
        JpaIdFieldAnnotated result = coll.findOneById("blah");
        assertThat(result, notNullValue());
        assertThat(result.id, equalTo("blah"));
    }

    public static class JpaIdFieldAnnotated {
        @javax.persistence.Id
        public String id;
    }

    @Test
    public void testGetterSetterAnnotated() throws Exception {
        GetterSetterAnnotated o = new GetterSetterAnnotated();
        o.setId("blah");
        JacksonDBCollection<GetterSetterAnnotated, String> coll = createCollFor(o, String.class);
        WriteResult<GetterSetterAnnotated, String> writeResult = coll.insert(o);
        assertThat(writeResult.getSavedId(), equalTo("blah"));
        assertThat(writeResult.getDbObject().get("id"), nullValue());
        GetterSetterAnnotated result = coll.findOneById("blah");
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo("blah"));
    }

    public static class GetterSetterAnnotated {
        private String id;

        @Id
        public String getId() {
            return id;
        }

        @Id
        public void setId(String id) {
            this.id = id;
        }
    }

    @Test
    public void testCreatorGetterAnnotated() throws Exception {
        CreatorGetterAnnotated o = new CreatorGetterAnnotated("blah");
        JacksonDBCollection<CreatorGetterAnnotated, String> coll = createCollFor(o, String.class);
        WriteResult<CreatorGetterAnnotated, String> writeResult = coll.insert(o);
        assertThat(writeResult.getSavedId(), equalTo("blah"));
        assertThat(writeResult.getDbObject().get("id"), nullValue());
        CreatorGetterAnnotated result = coll.findOneById("blah");
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo("blah"));
    }

    public static class CreatorGetterAnnotated {
        private final String id;

        @JsonCreator
        public CreatorGetterAnnotated(@Id String id) {
            this.id = id;
        }

        @Id
        public String getId() {
            return id;
        }
    }

    @Test
    public void testObjectIdFieldAnnotated() throws Exception {
        ObjectIdFieldAnnotated o = new ObjectIdFieldAnnotated();
        JacksonDBCollection<ObjectIdFieldAnnotated, String> coll = createCollFor(o, String.class);
        WriteResult<ObjectIdFieldAnnotated, String> writeResult = coll.insert(o);
        assertThat(writeResult.getDbObject().get("_id"), instanceOf(org.bson.types.ObjectId.class));
        assertThat(writeResult.getSavedId(), instanceOf(String.class));
        assertThat(writeResult.getDbObject().get("id"), nullValue());
        ObjectIdFieldAnnotated result = coll.findOneById(writeResult.getSavedId());
        assertThat(result, notNullValue());
        assertThat(result.id, equalTo(writeResult.getSavedId()));
    }

    public static class ObjectIdFieldAnnotated {
        @Id
        @ObjectId
        public String id;
    }

    @Test
    public void testCreatorGetterObjectIdAnnotated() throws Exception {
        CreatorGetterObjectIdAnnotated o = new CreatorGetterObjectIdAnnotated(null);
        JacksonDBCollection<CreatorGetterObjectIdAnnotated, String> coll = createCollFor(o, String.class);
        WriteResult<CreatorGetterObjectIdAnnotated, String> writeResult = coll.insert(o);
        assertThat(writeResult.getSavedId(), notNullValue());
        assertThat(writeResult.getSavedId(), instanceOf(String.class));
        assertThat(writeResult.getDbObject().get("id"), nullValue());
        assertThat(writeResult.getSavedId(), equalTo(writeResult.getDbObject().get("_id").toString()));
        CreatorGetterObjectIdAnnotated result = coll.findOneById(writeResult.getSavedId());
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(writeResult.getSavedId()));
    }

    public static class CreatorGetterObjectIdAnnotated {
        private final String id;

        @JsonCreator
        public CreatorGetterObjectIdAnnotated(@Id @ObjectId String id) {
            this.id = id;
        }

        @Id
        @ObjectId
        public String getId() {
            return id;
        }
    }


}
