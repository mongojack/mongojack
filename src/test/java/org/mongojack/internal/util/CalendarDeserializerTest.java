/*
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
package org.mongojack.internal.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mongojack.internal.CalendarDeserializer;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link CalendarDeserializer}
 * 
 * @author bmary
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarDeserializerTest {

    private static final long TIME_AS_LONG = 1065l;
    @Mock
    private Date date;
    @Mock
    private JsonParser jsonParser;
    @Mock
    private DeserializationContext deserializationContext;
    @Mock
    private Calendar calendar;
    @Mock
    private JsonMappingException jsonMappingException;

    private CalendarDeserializer deserializer;
    private Calendar deserializedDate;

    @Before
    public void setUp() throws IOException {
        final MismatchedInputException exception = MismatchedInputException.from(jsonParser, Calendar.class, "foo");
        when(deserializationContext.constructCalendar(date)).thenReturn(calendar);
        when(deserializationContext.handleUnexpectedToken(eq(Calendar.class), anyObject())).thenThrow(exception);
        when(deserializationContext.constructCalendar(new Date(TIME_AS_LONG))).thenReturn(calendar);

        deserializer = new CalendarDeserializer();
    }

    @Test
    public void testWithDateObject() throws IOException {
        when(jsonParser.getCurrentToken()).thenReturn(JsonToken.VALUE_EMBEDDED_OBJECT);
        when(jsonParser.getEmbeddedObject()).thenReturn(date);

        deserializedDate = deserializer.deserialize(jsonParser, deserializationContext);

        assertEquals(calendar, deserializedDate);
    }

    @Test(expected = JsonMappingException.class)
    public void testWithoutDateObject() throws IOException {
        when(jsonParser.getCurrentToken()).thenReturn(JsonToken.VALUE_EMBEDDED_OBJECT);
        when(jsonParser.getEmbeddedObject()).thenReturn(new Object());

        deserializedDate = deserializer.deserialize(jsonParser, deserializationContext);
    }

    @Test
    public void testNoEmbeddedObject() throws IOException {
        ObjectMapper mapper = createMapper();
        deserializedDate = mapper.readValue(Long.toString(TIME_AS_LONG), Calendar.class);

        Calendar expectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expectedCalendar.setTimeInMillis(TIME_AS_LONG);

        assertEquals(expectedCalendar, deserializedDate);
    }

    @Test
    public void testDateIsNull() throws IOException {
        when(jsonParser.getCurrentToken()).thenReturn(JsonToken.VALUE_NULL);
        when(jsonParser.currentTokenId()).thenReturn(JsonTokenId.ID_NULL);
        when(jsonParser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_NULL);

        deserializedDate = deserializer.deserialize(jsonParser, deserializationContext);
        assertNull(deserializedDate);
    }
    
    private ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule(CalendarDeserializerTest.class.getSimpleName() + "Module");
        module.addDeserializer(Calendar.class, deserializer);
        mapper.registerModule(module);
        return mapper;
    }
}
