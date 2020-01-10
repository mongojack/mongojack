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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.mongojack.DBRef;

import java.io.IOException;

/**
 * Deserializer for DBRefs
 * 
 * @author James Roper
 * @since 1.2
 */
public class DBRefDeserializer<T, K> extends JsonDeserializer<DBRef> {

    private final JavaType type;
    private final JavaType keyType;
    private final JsonDeserializer<K> keyDeserializer;

    public DBRefDeserializer(JavaType type, JavaType keyType) {
        this(type, keyType, null);
    }

    public DBRefDeserializer(JavaType type, JavaType keyType,
            JsonDeserializer<K> keyDeserializer) {
        this.type = type;
        this.keyType = keyType;
        this.keyDeserializer = keyDeserializer;
    }

    @Override
    public DBRef deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        K id = null;
        String collectionName = null;
        String databaseName = null;
        JsonToken token = jp.getCurrentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        if (token == JsonToken.VALUE_EMBEDDED_OBJECT) {
            // Someones already kindly decoded it for us
            Object object = jp.getEmbeddedObject();
            if (object instanceof com.mongodb.DBRef) {
                if (keyDeserializer != null) {
                    id = keyDeserializer.deserialize(jp, ctxt);
                } else {
                    id = (K) ((com.mongodb.DBRef) object).getId();
                }
                collectionName = ((com.mongodb.DBRef) object).getCollectionName();
                databaseName = ((com.mongodb.DBRef) object).getDatabaseName();
            } else {
                throw ctxt.instantiationException(DBRef.class,
                        "Don't know what to do with embedded object: "
                                + object);
            }
        } else if (token == JsonToken.START_OBJECT) {
            token = jp.nextValue();
            while (token != JsonToken.END_OBJECT) {
                if (jp.getCurrentName().equals("$id")) {
                    if (keyDeserializer != null) {
                        id = keyDeserializer.deserialize(jp, ctxt);
                    } else {
                        id = (K) jp.getEmbeddedObject();
                    }
                } else if (jp.getCurrentName().equals("$ref")) {
                    collectionName = jp.getText();
                } else if (jp.getCurrentName().equals("$db")) {
                    databaseName = jp.getText();
                } else {
                    // Ignore the rest
                }
                token = jp.nextValue();
            }
        }
        if (id == null) {
            return null;
        }
        if (collectionName == null) {
            throw ctxt.instantiationException(DBRef.class,
                    "DBRef contains no collection name");
        }

        return new DBRef<>(id, type.getRawClass(), collectionName, databaseName);
    }
}
