package org.mongodb.jackson;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.Mongo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mongodb.jackson.mock.MockObject;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test the Json DB Cursor
 */
public class TestDBCursor {
    private Mongo mongo;
    private DB db;
    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setup() throws Exception {
        mongo = new Mongo();
        db = mongo.getDB("test");
        coll = JacksonDBCollection.wrap(db.createCollection("mockObject", new BasicDBObject()),
                MockObject.class, String.class);
    }

    @After
    public void tearDown() throws Exception {
        coll.getDbCollection().drop();
        mongo.close();
    }

    @Test
    public void testIterator() {
        MockObject o1 = new MockObject("id1", "blah1", 10);
        MockObject o2 = new MockObject("id2", "blah2", 20);
        MockObject o3 = new MockObject("id3", "blah3", 30);
        coll.insert(o1, o2, o3);
        DBCursor<MockObject> cursor = coll.find().sort(new BasicDBObjectBuilder().add("integer", 1).get());
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(o1));
        assertThat(cursor.curr(), equalTo(o1));
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(o2));
        assertThat(cursor.curr(), equalTo(o2));
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo(o3));
        assertThat(cursor.curr(), equalTo(o3));
        assertThat(cursor.hasNext(), equalTo(false));
    }

    @Test
    public void testArray() {
        MockObject o1 = new MockObject("id1", "blah1", 10);
        MockObject o2 = new MockObject("id2", "blah2", 20);
        MockObject o3 = new MockObject("id3", "blah3", 30);
        coll.insert(o1, o2, o3);
        List<MockObject> results = coll.find().sort(new BasicDBObjectBuilder().add("integer", 1).get()).toArray();
        assertThat(results, contains(o1, o2, o3));
        assertThat(results, hasSize(3));
    }
}
