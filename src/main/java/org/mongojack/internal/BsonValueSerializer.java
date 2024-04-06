package org.mongojack.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.mongojack.internal.stream.JsonGeneratorAdapter;

import java.io.IOException;

public class BsonValueSerializer extends JsonSerializer<BsonValue> {

    private final BsonMapSerializer bsonMapSerializer = new BsonMapSerializer();

    @Override
    public void serialize(BsonValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else if (value instanceof BsonDocument) {
            bsonMapSerializer.serializeSimpleBsonMap((BsonDocument) value, gen, serializers);
        } else if (value instanceof BsonArray) {
            gen.writeStartArray();
            JsonSerializer<Object> ser = serializers.findValueSerializer(BsonValue.class);
            for (BsonValue bsonValue : ((BsonArray) value).getValues()) {
                ser.serialize(bsonValue, gen, serializers);
            }
            gen.writeEndArray();
        } else if (gen instanceof JsonGeneratorAdapter) {
            ((JsonGeneratorAdapter)gen).writeBsonValue(value);
        }
    }
}
