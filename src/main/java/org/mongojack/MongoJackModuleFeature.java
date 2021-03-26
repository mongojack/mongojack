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
     * Serialises {@link Instant}s as BSON dates when nanosecond precision is disabled.
     *
     * @see SerializationFeature#WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS
     */
    WRITE_INSTANT_AS_BSON_DATE(false),

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
