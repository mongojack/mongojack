package org.mongojack;

/**
 * Used to customize various aspects of serialization inside JacksonMongoCollection.  Pass this to the builder for the collection.
 */
public interface SerializationOptions {

    /**
     * <p>Controls whether or not Bson filters passed to JacksonMongoCollection and the various iterable implementations are
     * serialized with specific knowledge of the document class and its specific bean fields or not.</p>
     *
     * <p>Enabling simple serialization might be desired if conversion to a BsonDocument and then back produces undesirable
     * side-effects.  For most purposes a simple toBsonDocument with the right codec is sufficient.</p>
     *
     * @return whether or not to just use regular codec serialization
     */
    boolean isSimpleFilterSerialization();

    static SerializationOptionsBuilder builder() {
        return new SerializationOptionsBuilder();
    }

}
