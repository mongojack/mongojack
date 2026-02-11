package org.mongojack.internal;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;

import tools.jackson.core.JacksonException;
import java.util.UUID;

/**
 * A simple serializer for Java UUIDs which prevents the regular Java
 * data type from being converted to an inefficient string.
 *
 * @author Jared Tiala
 * @since 2.6.2
 */
public class UUIDSerializer extends ValueSerializer<UUID> {

    @Override
    public void serialize(UUID uuid, JsonGenerator jgen,
            SerializationContext provider) throws JacksonException {
        jgen.writePOJO(uuid);
    }
}
