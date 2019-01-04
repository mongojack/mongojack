package org.mongojack.internal;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;

/**
 * Patched {@link java.time.Instant} deserializer. Works with bson4jackson-deserialized ISODate() fields
 *
 * @author Mikhail Surin
 */
public class MongoJackInstantDeserializer extends InstantDeserializer<Instant> {
    public MongoJackInstantDeserializer() {
        super(Instant.class, DateTimeFormatter.ISO_INSTANT,
                Instant::from,
                a -> Instant.ofEpochMilli(a.value),
                a -> Instant.ofEpochSecond(a.integer, a.fraction),
                null,
                true);
    }

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.getCurrentTokenId() == JsonTokenId.ID_EMBEDDED_OBJECT) {
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
