package org.mongodb.jackson.mock;

import org.mongodb.jackson.ObjectId;

/**
 * Mock object with object id annotated String
 */
public class MockObjectObjectIdAnnotated {
    @ObjectId
    public String _id;

    @ObjectId
    public byte[] someId;
}
