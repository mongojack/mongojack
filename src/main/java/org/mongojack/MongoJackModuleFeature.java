package org.mongojack;

import com.fasterxml.jackson.databind.cfg.ConfigFeature;

public enum MongoJackModuleFeature implements ConfigFeature {

    /**
     * Register the java time module on configuration
     */
    REGISTER_JAVA_TIME(true),

    /**
     * Disable writing dates as timestamps {@code objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);},
     * because we get fewer runtime problems that way.
     */
    DISABLE_DATES_AS_TIMESTAMPS(true),

    /**
     * Sets the mapper's serialization inclusion to non-null
     */
    SET_SERIALIZATION_INCLUSION_NON_NULL(true),

    /**
     * Serialises {@link java.time.Instant}s as BSON dates when nanosecond precision is disabled.
     *
     * @see com.fasterxml.jackson.databind.SerializationFeature#WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS
     */
    WRITE_INSTANT_AS_BSON_DATE(false),

    /**
     * <p>Adds a set of serializers so that you can properly serialize {@link org.bson.BsonValue} values
     * and {@link org.bson.conversions.Bson} values in your input documents.  Disabled because it has the potential
     * to change expected behavior for users of the library.</p>
     *
     * <p>This feature is primarily meant so you can build aggregation pipelines or filters with embedded
     * bson stuff and have them correctly serialize.  It's theoretically possible that you could save this stuff
     * in a POJO, but it won't be read back out correctly because type information is lost.  That said, deserialization
     * will be <em>attempted</em>, but only to the point of producing a simple {@link org.bson.Document}</p>.
     */
    ENABLE_BSON_VALUE_SERIALIZATION(false),

    ;

    private final boolean _defaultState;
    private final int _mask;

    MongoJackModuleFeature(boolean defaultState) {
        _defaultState = defaultState;
        _mask = (1 << ordinal());
    }

    @Override
    public boolean enabledByDefault() { return _defaultState; }

    @Override
    public int getMask() { return _mask; }

    @Override
    public boolean enabledIn(int flags) { return (flags & _mask) != 0; }

}
