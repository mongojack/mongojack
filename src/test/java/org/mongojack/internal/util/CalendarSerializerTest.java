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

import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mongojack.internal.CalendarSerializer;
import org.mongojack.internal.stream.DBEncoderBsonGenerator;

import java.io.IOException;
import java.util.Calendar;

import static org.mockito.Mockito.*;

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
    private DBEncoderBsonGenerator jsonGenerator;
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
