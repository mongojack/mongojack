package net.vz.mongodb.jackson;

import junit.framework.Assert;
import net.vz.mongodb.jackson.mock.MockByteArray;

import org.junit.Before;
import org.junit.Test;

public class TestGetSavedObjectWithBinaryData extends MongoDBTestBase {

    private JacksonDBCollection<MockByteArray, String> coll;

    @Before
    public void setup() throws Exception {

        coll = getCollection(MockByteArray.class, String.class);
    }

    @Test
    public void testSaveAndReturn() throws Exception {

        final MockByteArray mockObject = new MockByteArray(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        final MockByteArray result = coll.insert(mockObject).getSavedObject();

        Assert.assertNotNull(result);
    }
}
