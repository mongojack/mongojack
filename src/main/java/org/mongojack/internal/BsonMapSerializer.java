package org.mongojack.internal;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

import tools.jackson.core.JacksonException;
import java.util.Map;

public class BsonMapSerializer {

    public void serializeSimpleBsonMap(Map<String, ?> value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        gen.writeStartObject();
        for (Map.Entry<String, ?> entry : value.entrySet()) {
            gen.writeName(entry.getKey());
            Object entryValue = entry.getValue();
            if (entryValue == null) {
                gen.writeNull();
            } else {
                serializers.findValueSerializer(entryValue.getClass()).serialize(entryValue, gen, serializers);
            }
        }
        gen.writeEndObject();
    }
}
