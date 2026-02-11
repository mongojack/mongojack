package org.mongojack.internal;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonTokenId;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.ext.javatime.deser.InstantDeserializer;

/**
 * Patched {@link java.time.Instant} deserializer. Works with bson4jackson-deserialized ISODate() fields
 *
 * @author Mikhail Surin
 */
public class MongoJackInstantDeserializer extends InstantDeserializer<Instant> {
    private final static boolean DEFAULT_NORMALIZE_ZONE_ID = DateTimeFeature.NORMALIZE_DESERIALIZED_ZONE_ID.enabledByDefault();
    private final static boolean DEFAULT_ALWAYS_ALLOW_STRINGIFIED_DATE_TIMESTAMPS = DateTimeFeature.ALWAYS_ALLOW_STRINGIFIED_DATE_TIMESTAMPS
            .enabledByDefault();

    public MongoJackInstantDeserializer() {
        super(Instant.class, DateTimeFormatter.ISO_INSTANT,
                Instant::from,
                a -> Instant.ofEpochMilli(a.value),
                a -> Instant.ofEpochSecond(a.integer, a.fraction),
                null,
                true, // yes, replace zero offset with Z
                DEFAULT_NORMALIZE_ZONE_ID,
                DEFAULT_ALWAYS_ALLOW_STRINGIFIED_DATE_TIMESTAMPS);
    }

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        if (parser.currentTokenId() == JsonTokenId.ID_EMBEDDED_OBJECT) {
            Object embeddedObject = parser.getEmbeddedObject();
            if (embeddedObject instanceof Instant) {
                return (Instant) embeddedObject;
            }
            if (embeddedObject instanceof Date) {
                return ((Date) embeddedObject).toInstant();
            }
        }
        return super.deserialize(parser, context);
    }
}
