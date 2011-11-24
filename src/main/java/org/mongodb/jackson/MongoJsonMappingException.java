package org.mongodb.jackson;

import com.mongodb.MongoException;
import org.codehaus.jackson.map.JsonMappingException;

/**
 * Exception used to indicate a problem occurred when converting the MongoDB objects to Jackson
 */
public class MongoJsonMappingException extends MongoException {
    public MongoJsonMappingException(JsonMappingException e) {
        super("Error mapping BSON to POJOs", e);
    }
}
