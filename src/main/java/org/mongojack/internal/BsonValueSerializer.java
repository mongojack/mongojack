package org.mongojack.internal;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonNull;
import org.bson.BsonValue;
import org.mongojack.internal.stream.JsonGeneratorAdapter;

import tools.jackson.core.JacksonException;

public class BsonValueSerializer extends ValueSerializer<BsonValue> {

    private final BsonMapSerializer bsonMapSerializer = new BsonMapSerializer();

    @Override
    public void serialize(BsonValue value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        if (value == null || value instanceof BsonNull) {
            gen.writeNull();
        } else if (value instanceof BsonDocument) {
            bsonMapSerializer.serializeSimpleBsonMap((BsonDocument) value, gen, serializers);
        } else if (value instanceof BsonArray) {
            gen.writeStartArray();
            ValueSerializer<Object> ser = serializers.findValueSerializer(BsonValue.class);
            for (BsonValue bsonValue : ((BsonArray) value).getValues()) {
                ser.serialize(bsonValue, gen, serializers);
            }
            gen.writeEndArray();
        } else if (gen instanceof JsonGeneratorAdapter) {
            ((JsonGeneratorAdapter)gen).writeBsonValue(value);
        }
    }
}
