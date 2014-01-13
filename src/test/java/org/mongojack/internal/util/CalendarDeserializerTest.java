package org.mongojack.internal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mongojack.internal.CalendarDeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;

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
    public void setUp() {
        when(deserializationContext.constructCalendar(date)).thenReturn(calendar);
        when(deserializationContext.mappingException(Calendar.class)).thenReturn(jsonMappingException);
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
        when(jsonParser.getCurrentToken()).thenReturn(JsonToken.VALUE_NUMBER_INT);
        when(jsonParser.getLongValue()).thenReturn(TIME_AS_LONG);

        deserializedDate = deserializer.deserialize(jsonParser, deserializationContext);
        assertEquals(calendar, deserializedDate);
    }

    @Test
    public void testDateIsNull() throws IOException {
        when(jsonParser.getCurrentToken()).thenReturn(JsonToken.VALUE_NULL);

        deserializedDate = deserializer.deserialize(jsonParser, deserializationContext);
        assertNull(deserializedDate);
    }
}
