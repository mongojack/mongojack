package org.mongojack.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.BasicBSONObject;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;

public class BsonSerializer extends JsonSerializer<Bson> {

    private final BsonMapSerializer bsonMapSerializer = new BsonMapSerializer();

    @Override
    public void serialize(Bson value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else if (value instanceof BsonDocument) {
            bsonMapSerializer.serializeSimpleBsonMap((BsonDocument) value, gen, serializers);
        } else if (value instanceof Document) {
            bsonMapSerializer.serializeSimpleBsonMap((Document) value, gen, serializers);
        } else if (value instanceof BasicBSONObject) {
            bsonMapSerializer.serializeSimpleBsonMap((BasicBSONObject) value, gen, serializers);
        } else {
            bsonMapSerializer.serializeSimpleBsonMap(value.toBsonDocument(), gen, serializers);
        }
    }
}
