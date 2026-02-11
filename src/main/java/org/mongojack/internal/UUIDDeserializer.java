package org.mongojack.internal;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import org.bson.BsonBinary;

import tools.jackson.core.JacksonException;
import java.util.UUID;

/**
 * A simple deserializer for Java UUIDs which prevents the regular Java
 * data type from being converted to an inefficient string.
 *
 * @author Jared Tiala
 * @since 2.6.2
 */
public class UUIDDeserializer extends ValueDeserializer<UUID> {

    @Override
    public UUID deserialize(JsonParser jp, DeserializationContext ctxt)
        throws JacksonException {
        JsonToken token = jp.currentToken();

        if (token == JsonToken.VALUE_EMBEDDED_OBJECT) {
            Object object = jp.getEmbeddedObject();

            if (object instanceof UUID) {
                return (UUID) object;
            } else if (object instanceof BsonBinary) {
                return ((BsonBinary) object).asUuid();
            }
        }

        return (UUID) ctxt.handleUnexpectedToken(UUID.class, jp);
    }
}
