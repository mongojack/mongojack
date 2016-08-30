package org.mongojack.internal;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * A simple serializer for Java UUIDs which prevents the regular Java
 * data type from being converted to an inefficient string.
 *
 * @author Jared Tiala
 * @since 2.6.2
 */
public class UUIDSerializer extends JsonSerializer<UUID> {

    @Override
    public void serialize(UUID uuid, JsonGenerator jgen,
            SerializerProvider provider) throws IOException {
        jgen.writeObject(uuid);
    }
}
