package org.mongojack;

import org.junit.Before;
import org.junit.Test;
import org.mongojack.mock.MockObject;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestDBSort extends MongoDBTestBase {
    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setUp() {
        coll = getCollection(MockObject.class, String.class);
        coll.save(new MockObject("1", "b", 10));
        coll.save(new MockObject("2", "a", 30));
        coll.save(new MockObject("3", "a", 20));
    }

    private void assertOrder(Iterable<MockObject> results, String... order) {
        int i = 0;
        for (MockObject o : results) {
            assertThat("Item " + i + " out of order", o._id, equalTo(order[i]));
            i++;
        }
    }

    @Test
    public void testAsc() {
        assertOrder(coll.find().sort(DBSort.asc("string").asc("integer")), "3", "2", "1");
    }

    @Test
    public void testDesc() {
        assertOrder(coll.find().sort(DBSort.desc("string").desc("integer")), "1", "2", "3");
    }

    @Test
    public void testAscDesc() {
        assertOrder(coll.find().sort(DBSort.asc("string").desc("integer")), "2", "3", "1");
    }

    @Test
    public void testDescAsc() {
        assertOrder(coll.find().sort(DBSort.desc("string").asc("integer")), "1", "3", "2");
    }

}
