package org.mongojack;

/**
 * Builder for {@link SerializationOptions}
 */
public class SerializationOptionsBuilder {

    private boolean simpleFilterSerialization = false;

    SerializationOptionsBuilder() {
        // nothing
    }

    /**
     * <p>Controls whether or not Bson filters passed to JacksonMongoCollection and the various iterable implementations are
     * serialized with specific knowledge of the document class and its specific bean fields or not.</p>
     *
     * <p>Enabling simple serialization might be desired if conversion to a BsonDocument and then back produces undesirable
     * side-effects.  For most purposes a simple toBsonDocument with the right codec is sufficient.</p>
     *
     * @return whether or not to just use regular codec serialization
     */
    public SerializationOptionsBuilder withSimpleFilterSerialization(final boolean simpleFilterSerialization) {
        this.simpleFilterSerialization = simpleFilterSerialization;
        return this;
    }


    @SuppressWarnings("Convert2Lambda")
    public SerializationOptions build() {
        return new SerializationOptions() {
            @Override
            public boolean isSimpleFilterSerialization() {
                return simpleFilterSerialization;
            }
        };
    }

}
