package org.mongodb.jackson;

/**
 * Mock object with object id annotated String
 */
public class MockObjectObjectIdAnnotated {
    @ObjectId
    public String _id;

    @ObjectId
    public byte[] someId;
}
