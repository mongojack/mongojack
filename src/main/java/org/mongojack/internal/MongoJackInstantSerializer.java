package org.mongojack.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import org.mongojack.TransformingEmbeddedObjectSerializer;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

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
    public void serialize(Instant value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (provider.isEnabled(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)) {
            defaultSerializer.serialize(value, jgen, provider);
        } else {
            super.serialize(value, jgen, provider);
        }
    }

}
