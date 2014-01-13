package org.mongojack.internal.util;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mongojack.internal.CalendarSerializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Unit tests for the {@link CalendarSerializer}.
 * 
 * @author bmary
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarSerializerTest {

    @Mock
    private SerializerProvider serializerProvider;
    @Mock
    private JsonGenerator jsonGenerator;
    @Mock
    private Calendar calendar;

    private CalendarSerializer serializer;

    @Before
    public void setUp() {
        serializer = new CalendarSerializer();
    }

    @Test
    public void test() throws IOException {
        serializer.serialize(calendar, jsonGenerator, serializerProvider);

        verify(jsonGenerator).writeObject(calendar);
    }
}
