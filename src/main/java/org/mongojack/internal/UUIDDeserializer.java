package org.mongojack.internal;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * A simple deserializer for Java UUIDs which prevents the regular Java
 * data type from being converted to an inefficient string.
 *
 * @author Jared Tiala
 * @since 2.6.2
 */
public class UUIDDeserializer extends JsonDeserializer<UUID> {

    @Override
    public UUID deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonToken token = jp.getCurrentToken();

        if (token == JsonToken.VALUE_EMBEDDED_OBJECT) {
            Object object = jp.getEmbeddedObject();

            if (object instanceof UUID) {
                return (UUID) object;
            }
        }

        throw ctxt.mappingException(UUID.class);
    }
}
