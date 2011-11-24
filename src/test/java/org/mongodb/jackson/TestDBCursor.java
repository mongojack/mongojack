package org.mongodb.jackson;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import org.junit.After;
import org.junit.Before;
import org.mongodb.jackson.mock.MockObject;

/**
 * Test the Json DB Cursor
 */
public class TestDBCursor {
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


}
