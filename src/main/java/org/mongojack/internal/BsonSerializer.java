package org.mongojack.internal;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import org.bson.BasicBSONObject;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import tools.jackson.core.JacksonException;

public class BsonSerializer extends ValueSerializer<Bson> {

    private final BsonMapSerializer bsonMapSerializer = new BsonMapSerializer();

    @Override
    public void serialize(Bson value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
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
