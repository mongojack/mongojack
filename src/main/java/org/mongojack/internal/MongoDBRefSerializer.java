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

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import com.mongodb.DBRef;

import tools.jackson.core.JacksonException;

/**
 * Serialises DBRef objects
 *
 * @author James Roper
 * @since 1.2
 */
public class MongoDBRefSerializer extends ValueSerializer<DBRef> {

    @Override
    public void serialize(final DBRef value, final JsonGenerator gen, final SerializationContext serializers) throws JacksonException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeStartObject();
            gen.writeName("$ref");
            gen.writeString(value.getCollectionName());
            gen.writeName("$id");
            gen.writePOJO(value.getId());
            if (value.getDatabaseName() != null) {
                gen.writeName("$db");
                gen.writePOJO(value.getDatabaseName());
            }
            gen.writeEndObject();
        }
    }

    @Override
    public Class<DBRef> handledType() {
        return DBRef.class;
    }

}
