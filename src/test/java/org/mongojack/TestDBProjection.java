package org.mongojack;

import org.junit.Before;
import org.junit.Test;
import org.mongojack.mock.MockObject;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TestDBProjection extends MongoDBTestBase {
    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setUp() {
        coll = getCollection(MockObject.class, String.class);
    }

    @Test
    public void testIncludes() {
        MockObject o = new MockObject("string", 10);
        o.longs = 20l;
        coll.save(o);
        MockObject result = coll.findOne(DBQuery.empty(), DBProjection.include("string", "integer"));
        assertThat(result.string, equalTo("string"));
        assertThat(result.integer, equalTo(10));
        assertNull(result.longs);
    }

    @Test
    public void testExcludes() {
        MockObject o = new MockObject("string", 10);
        o.longs = 20l;
        coll.save(o);
        MockObject result = coll.findOne(DBQuery.empty(), DBProjection.exclude("string", "integer"));
        assertNull(result.string);
        assertNull(result.integer);
        assertThat(result.longs, equalTo(20l));
    }
}
