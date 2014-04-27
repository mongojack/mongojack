package org.mongojack;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mongojack.mock.MockBaseObject;
import org.mongojack.mock.MockDerivedObject;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestPolymorphic extends MongoDBTestBase {
    private JacksonDBCollection<MockBaseObject, String> coll;

    @Before
    public void setup() throws Exception {
        coll = getCollection(MockBaseObject.class, String.class);
    }

    String id;

    @Test
    public void testDerived() {
        MockDerivedObject obj = new MockDerivedObject();
        obj.baseField = 1;
        obj.derivedField = 2;
        coll.insert(obj);
        MockBaseObject foundObj = coll.findOne();
        assertEquals(foundObj.baseField, obj.baseField);
        assertEquals(((MockDerivedObject) foundObj).derivedField, obj.derivedField);
        id = foundObj._id;
        assertNotNull(id);
    }

    @Ignore("This is broken, checked in as a repro")
    @Test
    public void testFindId() {
        assertNotNull(coll.findOne(DBQuery.is("_id", id)));
        assertNotNull(coll.findOneById(id));
    }
}
