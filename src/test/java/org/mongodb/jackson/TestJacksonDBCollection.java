package org.mongodb.jackson;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class TestJacksonDBCollection {
    private Mongo mongo;
    private DB db;
    private JacksonDBCollection<MockObject> coll;

    @Before
    public void setup() throws Exception {
        mongo = new Mongo();
        db = mongo.getDB("test");
        coll = JacksonDBCollection.wrap(db.createCollection("mockObject", new BasicDBObject()),
                MockObject.class);
    }

    @After
    public void tearDown() throws Exception {
        coll.getDbCollection().drop();
        mongo.close();
    }

    @Test
    public void testQuery() {
        MockObject o1 = coll.insert(new MockObject("ten", 10)).getSavedObject();
        MockObject o2 = coll.insert(new MockObject("ten", 100)).getSavedObject();
        coll.insert(new MockObject("twenty", 20));

        List<MockObject> results = coll.find(new BasicDBObjectBuilder().add("string", "ten").get()).toArray();
        assertThat(results, hasSize(2));
        assertThat(results, contains(o1, o2));
    }

    @Test
    public void testQueryWithJavaObject() {
        MockObject o1 = coll.insert(new MockObject("ten", 10)).getSavedObject();
        MockObject o2 = coll.insert(new MockObject("ten", 100)).getSavedObject();
        coll.insert(new MockObject("twenty", 20));

        List<MockObject> results = coll.find(new MockObject("ten", null)).toArray();
        assertThat(results, hasSize(2));
        assertThat(results, contains(o1, o2));
    }

    @Test
    public void testQueryWithLimitedKeys() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        coll.insert(new MockObject("twenty", 20));

        List<MockObject> results = coll.find(new BasicDBObjectBuilder().add("string", "ten").get(),
                new BasicDBObjectBuilder().add("string", "something not null").get()).toArray();
        assertThat(results, hasSize(2));
        assertThat(results.get(0).integer, nullValue());
        assertThat(results.get(0).string, equalTo("ten"));
        assertThat(results.get(1).integer, nullValue());
        assertThat(results.get(1).string, equalTo("ten"));
    }

    @Test
    public void testQueryWithLimitedKeysFromJavaObject() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        coll.insert(new MockObject("twenty", 20));

        List<MockObject> results = coll.find(new MockObject("ten", null),
                new MockObject("something not null", null)).toArray();
        assertThat(results, hasSize(2));
        assertThat(results.get(0).integer, nullValue());
        assertThat(results.get(0).string, equalTo("ten"));
        assertThat(results.get(1).integer, nullValue());
        assertThat(results.get(1).string, equalTo("ten"));
    }

    @Test
    public void testRemove() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        MockObject object = coll.insert(new MockObject("twenty", 20)).getSavedObject();

        coll.remove(new BasicDBObjectBuilder().add("string", "ten").get());

        List<MockObject> remaining = coll.find().toArray();
        assertThat(remaining, Matchers.hasSize(1));
        assertThat(remaining, contains(object));
    }

    @Test
    public void testRemoveByJavaObject() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        MockObject object = coll.insert(new MockObject("twenty", 20)).getSavedObject();

        coll.remove(new MockObject("ten", null));

        List<MockObject> remaining = coll.find().toArray();
        assertThat(remaining, Matchers.hasSize(1));
        assertThat(remaining, contains(object));
    }

    @Test
    public void testRemoveByJavaObjectWithId() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        MockObject object = coll.insert(new MockObject("twenty", 20)).getSavedObject();

        MockObject toRemove = new MockObject();
        toRemove._id = object._id;

        coll.remove(toRemove);

        List<MockObject> remaining = coll.find().toArray();
        assertThat(remaining, Matchers.hasSize(2));
        assertThat(remaining, not(contains(object)));
    }

    @Test
    public void testRemoveById() {
        coll.insert(new MockObject("ten", 10));
        coll.insert(new MockObject("ten", 100));
        MockObject object = coll.insert(new MockObject("twenty", 20)).getSavedObject();


        coll.removeById(object._id);

        List<MockObject> remaining = coll.find().toArray();
        assertThat(remaining, Matchers.hasSize(2));
        assertThat(remaining, not(contains(object)));
    }
}
