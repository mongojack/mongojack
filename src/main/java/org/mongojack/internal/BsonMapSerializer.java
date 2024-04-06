package org.mongojack.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

public class BsonMapSerializer {

    public void serializeSimpleBsonMap(Map<String, ?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        for (Map.Entry<String, ?> entry : value.entrySet()) {
            gen.writeFieldName(entry.getKey());
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
