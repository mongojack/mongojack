package org.mongodb.jackson;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import org.codehaus.jackson.annotate.JsonCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class TestIdAnnotatedClass {
    private Mongo mongo;
    private DB db;

    @Before
    public void setup() throws Exception {
        mongo = new Mongo();
        db = mongo.getDB("test");
    }

    @After
    public void tearDown() throws Exception {
        db.getCollection("mockObject").drop();
        mongo.close();
    }

    private <T> JacksonDBCollection<T> createCollFor(T object) throws Exception {
        // Stupid generics...
        return (JacksonDBCollection) JacksonDBCollection.wrap(db.createCollection("mockObject", new BasicDBObject()),
                object.getClass());
    }

    @Test
    public void testIdFieldAnnotated() throws Exception {
        IdFieldAnnotated o = new IdFieldAnnotated();
        o.id = "blah";
        JacksonDBCollection<IdFieldAnnotated> coll = createCollFor(o);
        WriteResult<IdFieldAnnotated> writeResult = coll.insert(o);
        assertThat((String) writeResult.getSavedId(), equalTo("blah"));
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
        JacksonDBCollection<JpaIdFieldAnnotated> coll = createCollFor(o);
        WriteResult<JpaIdFieldAnnotated> writeResult = coll.insert(o);
        assertThat((String) writeResult.getSavedId(), equalTo("blah"));
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
        JacksonDBCollection<GetterSetterAnnotated> coll = createCollFor(o);
        WriteResult<GetterSetterAnnotated> writeResult = coll.insert(o);
        assertThat((String) writeResult.getSavedId(), equalTo("blah"));
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
        JacksonDBCollection<CreatorGetterAnnotated> coll = createCollFor(o);
        WriteResult<CreatorGetterAnnotated> writeResult = coll.insert(o);
        assertThat((String) writeResult.getSavedId(), equalTo("blah"));
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
        JacksonDBCollection<ObjectIdFieldAnnotated> coll = createCollFor(o);
        WriteResult<ObjectIdFieldAnnotated> writeResult = coll.insert(o);
        assertThat(writeResult.getDbObject().get("_id"), instanceOf(org.bson.types.ObjectId.class));
        assertThat(writeResult.getSavedId(), instanceOf(String.class));
        assertThat(writeResult.getDbObject().get("id"), nullValue());
        ObjectIdFieldAnnotated result = coll.findOneById(writeResult.getSavedId());
        assertThat(result, notNullValue());
        assertThat(result.id, equalTo((String) writeResult.getSavedId()));
    }

    public static class ObjectIdFieldAnnotated {
        @Id
        @ObjectId
        public String id;
    }

}
