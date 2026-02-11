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
package org.mongojack.internal;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import com.mongodb.DBRef;

import tools.jackson.core.JacksonException;


/**
 * Deserializer for DBRefs
 * 
 * @author James Roper
 * @since 1.2
 */
public class MongoDBRefDeserializer extends ValueDeserializer<DBRef> {

    @Override
    public DBRef deserialize(JsonParser jp, DeserializationContext ctxt) throws JacksonException {
        if (jp.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        if (jp.currentToken() == JsonToken.VALUE_EMBEDDED_OBJECT) {
            Object object = jp.getEmbeddedObject();
            if (object instanceof  DBRef) {
                return (DBRef)object;
            } else {
                throw ctxt.instantiationException(DBRef.class, "Don't know what to do with embedded object: " + object);
            }
        } else if (jp.currentToken() == JsonToken.START_OBJECT) {
            Object id = null;
            String collectionName = null;
            String databaseName = null;
            while (jp.nextValue() != JsonToken.END_OBJECT) {
                switch (jp.currentName()) {
                    case "$id":
                        switch (jp.currentToken()) {
                            case VALUE_EMBEDDED_OBJECT:
                                id = jp.getEmbeddedObject();
                                break;
                            case VALUE_STRING:
                                id = jp.getText();
                                break;
                            case VALUE_NUMBER_INT:
                            case VALUE_NUMBER_FLOAT:
                                id = jp.getNumberValue();
                                break;
                            case VALUE_TRUE:
                            case VALUE_FALSE:
                                id = jp.getBooleanValue();
                                break;
                            default:
                                throw ctxt.instantiationException(DBRef.class, "Don't know how to deserialize from current token " + jp.currentToken());
                        }
                        break;
                    case "$ref":
                        collectionName = jp.getText();
                        break;
                    case "$db":
                        databaseName = jp.getText();
                        break;
                }
            }
            if (collectionName == null) {
                throw ctxt.instantiationException(DBRef.class, "Couldn't extract collection name for dbref");
            }
            if (id == null) {
                throw ctxt.instantiationException(DBRef.class, "Couldn't extract object id for dbref");
            }
            return new DBRef(databaseName, collectionName, id);
        } else {
            throw ctxt.instantiationException(DBRef.class, "Don't know how to deserialize from current token " + jp.currentToken());
        }
    }

}
