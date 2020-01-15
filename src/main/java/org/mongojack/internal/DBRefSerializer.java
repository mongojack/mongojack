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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.mongojack.DBRef;

import java.io.IOException;

/**
 * Serialises DBRef objects
 *
 * @author James Roper
 * @since 1.2
 */
@SuppressWarnings("rawtypes")
public class DBRefSerializer extends JsonSerializer<DBRef> {

    @Override
    public void serialize(final DBRef value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeStartObject();
            gen.writeFieldName("$ref");
            gen.writeString(value.getCollectionName());
            gen.writeFieldName("$id");
            gen.writeObject(value.getId());
            if (value.getDatabaseName() != null) {
                gen.writeFieldName("$db");
                gen.writeObject(value.getDatabaseName());
            }
            gen.writeEndObject();
        }
    }

    @Override
    public Class<DBRef> handledType() {
        return DBRef.class;
    }
}
