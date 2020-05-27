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
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Deserializes calendars. Can handle strings, ints or BSON dates
 * 
 * @author James Roper
 * @since 1.2
 */
public class CalendarDeserializer extends StdDeserializer<Calendar> {
    public CalendarDeserializer() {
        super(Calendar.class);
    }

    @Override
    public Calendar deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonToken token = jp.getCurrentToken();
        Date date;
        if (token == JsonToken.VALUE_EMBEDDED_OBJECT) {
            // See if it's a date
            Object object = jp.getEmbeddedObject();
            if (object instanceof Date) {
                date = (Date) object;
            } else {
                return (Calendar) ctxt.handleUnexpectedToken(Calendar.class, jp);
            }
        } else {
            date = _parseDate(jp, ctxt);
        }
        if (date == null) {
            return null;
        }
        return ctxt.constructCalendar(date);
    }
}
