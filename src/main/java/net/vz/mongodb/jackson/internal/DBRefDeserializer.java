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
package net.vz.mongodb.jackson.internal;

import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.JacksonDBCollection;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.type.JavaType;

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

    public DBRefDeserializer(JavaType type, JavaType keyType) {
        this.type = type;
        this.keyType = keyType;
    }

    @Override
    public DBRef deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        // First of all, make sure that we can get a copy of the DBCollection
        if (jp instanceof JacksonDBCollectionProvider) {
            K id = null;
            String collectionName = null;
            JsonToken token = jp.getCurrentToken();
            if (token == JsonToken.VALUE_NULL) {
                return null;
            }
            if (token == JsonToken.VALUE_EMBEDDED_OBJECT) {
                // Someones already kindly decoded it for us
                Object object = jp.getEmbeddedObject();
                if (object instanceof com.mongodb.DBRef) {
                    id = (K) ((com.mongodb.DBRef) object).getId();
                    collectionName = ((com.mongodb.DBRef) object).getRef();
                } else {
                    throw ctxt.instantiationException(DBRef.class, "Don't know what to do with embedded object: " + object);
                }
            } else if (token == JsonToken.START_OBJECT) {
                token = jp.nextValue();
                while (token != JsonToken.END_OBJECT) {
                    if (jp.getCurrentName().equals("$id")) {
                        id = (K) jp.getEmbeddedObject();
                    } else if (jp.getCurrentName().equals("$ref")) {
                        collectionName = jp.getText();
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
                throw ctxt.instantiationException(DBRef.class, "DBRef contains no collection name");
            }

            JacksonDBCollection coll = ((JacksonDBCollectionProvider) jp).getDBCollection();
            JacksonDBCollection<T, K> refColl = coll.getReferenceCollection(collectionName, type, keyType);
            return new FetchableDBRef<T, K>(id, refColl);
        } else {
            throw ctxt.instantiationException(DBRef.class, "DBRef can only be deserialised by this deserializer if parser implements " + JacksonDBCollectionProvider.class.getName());
        }
    }
}
