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
package org.mongojack.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Date;

/**
 * Deserializer for dates.  Can deserialise either from number types, or BSON dates.
 *
 * @author James Roper
 * @since 1.2
 */
public class DateDeserializer extends StdDeserializer<Date> {

    public DateDeserializer() {
        super(Date.class);
    }

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonToken token = jp.getCurrentToken();
        if (token == JsonToken.VALUE_EMBEDDED_OBJECT) {
            // See if it's a date
            Object date = jp.getEmbeddedObject();
            if (date instanceof Date) {
                return (Date) date;
            } else {
                throw ctxt.mappingException(Date.class);
            }
        } else {
            return _parseDate(jp, ctxt);
        }
    }
}
