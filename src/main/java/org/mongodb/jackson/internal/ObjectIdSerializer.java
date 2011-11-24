package org.mongodb.jackson.internal;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Serializer for object ids, serialises strings or byte arrays to an ObjectId class
 */
public class ObjectIdSerializer extends JsonSerializer {
    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        if (value == null) {
            jgen.writeObject(null);
        } else if (value instanceof String) {
            jgen.writeObject(new org.bson.types.ObjectId((String) value));
        } else if (value instanceof byte[]) {
            jgen.writeObject(new org.bson.types.ObjectId((byte[]) value));
        } else {
            throw new JsonMappingException("Cannot deserialise object of type " + value.getClass() + " to ObjectId");
        }
    }
}
