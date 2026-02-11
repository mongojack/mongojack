package org.mongojack.internal;

import java.time.Instant;
import java.util.Date;

import org.mongojack.TransformingEmbeddedObjectSerializer;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.ext.javatime.ser.InstantSerializer;

/**
 * Serialises {@link Instant}s as BSON dates when nanosecond precision is disabled.
 *
 * @author Vladimir Petrakovich
 *
 * @see SerializationFeature#WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS
 */
public class MongoJackInstantSerializer extends TransformingEmbeddedObjectSerializer<Instant, Date> {

    private final InstantSerializer defaultSerializer = InstantSerializer.INSTANCE;

    @Override
    protected Date transform(final Instant value) {
        if (value != null) {
            return Date.from(value);
        }
        return null;
    }

    @Override
    public void serialize(Instant value, JsonGenerator jgen, SerializationContext provider) throws JacksonException {
        if (provider.isEnabled(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)) {
            defaultSerializer.serialize(value, jgen, provider);
        } else {
            super.serialize(value, jgen, provider);
        }
    }

}
