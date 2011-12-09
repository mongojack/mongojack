/*
 * Copyright 2011 VZ Netzwerke Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.vz.mongodb.jackson.internal.stream;

import de.undercouch.bson4jackson.BsonParser;
import de.undercouch.bson4jackson.types.ObjectId;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parser that wraps BSONParser to convert bson4jackson ObjectIds to org.bson ObjectIds, and stores error codes
 *
 * @author James Roper
 * @since 1.1.2
 */
public class DBDecoderBsonParser extends BsonParser {

    private final JacksonDBObject<?> dbObject;

    public DBDecoderBsonParser(int jsonFeatures, InputStream in, JacksonDBObject<?> dbObject) {
        super(jsonFeatures, in);
        this.dbObject = dbObject;
    }

    @Override
    public Object getEmbeddedObject() throws IOException, JsonParseException {
        Object object = super.getEmbeddedObject();
        if (object instanceof ObjectId) {
            return ObjectIdConvertor.convert((ObjectId) object);
        } else {
            return object;
        }
    }

    public boolean handleUnknownProperty(DeserializationContext ctxt, JsonDeserializer<?> deserializer,
                                         Object beanOrClass, String propertyName) throws IOException {
        if (propertyName.startsWith("$")) {
            // It's a special server response
            JsonToken token = nextToken();
            if (token == JsonToken.START_ARRAY || token == JsonToken.START_OBJECT) {
                // The server shouldn't be returning arrays or objects as the response, skip all children
                skipChildren();
            }
            // Store the value, whatever type it happens to be
            dbObject.put(propertyName, getEmbeddedObject());
            return true;
        }
        return false;
    }
}
