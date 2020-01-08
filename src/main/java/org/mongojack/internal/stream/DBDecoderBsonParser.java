/*
 * Copyright 2011 VZ Netzwerke Ltd
 * Copyright 2014 devbliss GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mongojack.internal.stream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonParser;
import de.undercouch.bson4jackson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;
import org.mongojack.internal.JacksonMongoCollectionProvider;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parser that wraps BSONParser to convert bson4jackson ObjectIds to org.bson
 * ObjectIds, and stores error codes
 *
 * @author James Roper
 * @since 1.1.2
 */
public class DBDecoderBsonParser extends BsonParser implements
    JacksonMongoCollectionProvider {

    private final JacksonDBObject<?> dbObject;
    private final JacksonMongoCollection dbCollection;

    public DBDecoderBsonParser(
        IOContext ctxt, int jsonFeatures,
        InputStream in, JacksonDBObject<?> dbObject,
        JacksonMongoCollection dbCollection, ObjectMapper objectMapper
    ) {
        // Honor document length must be true
        super(ctxt, jsonFeatures, Feature.HONOR_DOCUMENT_LENGTH.getMask(), in);
        this.dbObject = dbObject;
        this.dbCollection = dbCollection;
        setCodec(objectMapper);
    }

    @Override
    public String getText() throws IOException, JsonParseException {
        if (JsonToken.VALUE_EMBEDDED_OBJECT == getCurrentToken()) {
            return null;
        }
        return super.getText();
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

    public boolean handleUnknownProperty(
        DeserializationContext ctxt,
        JsonDeserializer<?> deserializer, Object beanOrClass,
        String propertyName
    ) throws IOException {
        if (propertyName.startsWith("$") || propertyName.equals("code")) {
            // It's a special server response
            JsonToken token = getCurrentToken();
            if (token == JsonToken.FIELD_NAME) {
                token = nextToken();
            }
            if (token == JsonToken.START_ARRAY
                || token == JsonToken.START_OBJECT) {
                // The server shouldn't be returning arrays or objects as the
                // response, skip all children
                skipChildren();
            }
            // Store the value, whatever type it happens to be
            dbObject.put(propertyName, getEmbeddedObject());
            return true;
        }
        return false;
    }

    @Override
    public JacksonMongoCollection getDBCollection() {
        return dbCollection;
    }

}
