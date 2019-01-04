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

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mongojack.internal.MongoJackInstantDeserializer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link MongoJackInstantDeserializer}
 *
 * @author antimony
 */
@RunWith(MockitoJUnitRunner.class)
public class MongoJackInstantDeserializerTest {

    private static final long TIME_AS_LONG = 1065l;

    @Mock
    private Date date;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext deserializationContext;

    @Mock
    private JsonMappingException jsonMappingException;

    private MongoJackInstantDeserializer deserializer;

    private Instant deserializedDate;

    @Before
    public void setUp() {
        when(deserializationContext.mappingException(anyString())).thenReturn(jsonMappingException);
        deserializer = new MongoJackInstantDeserializer();
    }

    @Test
    public void testWithDateObject() throws IOException {
        when(jsonParser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_EMBEDDED_OBJECT);
        when(jsonParser.getEmbeddedObject()).thenReturn(date);

        deserializedDate = deserializer.deserialize(jsonParser, deserializationContext);
        assertEquals(date.toInstant(), deserializedDate);
    }

    @Test
    public void testWithInstantObject() throws IOException {
        Instant instant = date.toInstant();
        when(jsonParser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_EMBEDDED_OBJECT);
        when(jsonParser.getEmbeddedObject()).thenReturn(instant);

        deserializedDate = deserializer.deserialize(jsonParser, deserializationContext);
        assertEquals(instant, deserializedDate);
    }

    @Test(expected = ClassCastException.class)
    public void testWithoutDateObject() throws IOException {
        when(jsonParser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_EMBEDDED_OBJECT);
        when(jsonParser.getEmbeddedObject()).thenReturn(new Object());

        deserializedDate = deserializer.deserialize(jsonParser, deserializationContext);
    }

    @Test
    public void testNoEmbeddedObject() throws IOException {
        ObjectMapper mapper = createMapper();
        deserializedDate = mapper.readValue(Long.toString(TIME_AS_LONG), Instant.class);
        Instant instant = Instant.ofEpochSecond(TIME_AS_LONG);
        assertEquals(instant, deserializedDate);
    }

    @Test(expected = JsonMappingException.class)
    public void testDateIsNull() throws IOException {
        when(jsonParser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_NULL);
        deserializedDate = deserializer.deserialize(jsonParser, deserializationContext);
    }

    private ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule(MongoJackInstantDeserializerTest.class.getSimpleName() + "Module");
        module.addDeserializer(Instant.class, deserializer);
        mapper.registerModule(module);
        return mapper;
    }
}
