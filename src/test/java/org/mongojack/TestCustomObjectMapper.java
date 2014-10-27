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
package org.mongojack;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mongojack.internal.MongoJackModule;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.DBObject;

public class TestCustomObjectMapper extends MongoDBTestBase {

    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setUp() {
        coll = getCollection(MockObject.class, String.class,
                createObjectMapper());
    }

    @Test
    public void customObjectMapperShouldWorkWhenSerialising() {
        MockObject obj = new MockObject();
        obj.custom = new Custom("hello", "world");
        coll.insert(obj);
        DBObject custom = (DBObject) coll.getDbCollection().findOne()
                .get("custom");
        assertNotNull(custom);
        assertThat((String) custom.get("v1"), equalTo("hello"));
        assertThat((String) custom.get("v2"), equalTo("world"));
    }

    @Test
    public void customObjectMapperShouldWorkWhenDeserialising() {
        MockObject obj = new MockObject();
        obj.custom = new Custom("hello", "world");
        coll.insert(obj);
        MockObject saved = coll.findOne();
        assertNotNull(saved);
        assertNotNull(saved.custom);
        assertThat(saved.custom.value1, equalTo("hello"));
        assertThat(saved.custom.value2, equalTo("world"));
    }

    public static class MockObject {
        @Id
        @ObjectId
        public String id;
        public Custom custom;
    }

    public static class Custom {
        public Custom(String value1, String value2) {
            this.value1 = value1;
            this.value2 = value2;
        }

        public String value1;
        public String value2;
    }

    private ObjectMapper createObjectMapper() {
        SimpleModule module = new SimpleModule("MySimpleModule", new Version(1,
                0, 0, null, "", ""));
        module.addDeserializer(Custom.class, new JsonDeserializer<Custom>() {
            @Override
            public Custom deserialize(JsonParser jp, DeserializationContext ctxt)
                    throws IOException {
                JsonNode node = jp.readValueAsTree();
                return new Custom(node.get("v1").asText(), node.get("v2")
                        .asText());
            }
        });
        module.addSerializer(Custom.class, new JsonSerializer<Custom>() {
            @Override
            public void serialize(Custom value, JsonGenerator jgen,
                    SerializerProvider provider) throws IOException {
                jgen.writeStartObject();
                jgen.writeFieldName("v1");
                jgen.writeString(value.value1);
                jgen.writeFieldName("v2");
                jgen.writeString(value.value2);
                jgen.writeEndObject();
            }
        });

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        MongoJackModule.configure(objectMapper);
        return objectMapper;
    }

}
