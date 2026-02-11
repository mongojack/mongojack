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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

public class TestJacksonMongoCollectionCustomObjectMapper extends MongoDBTestBase {

    private JacksonMongoCollection<MockObject> coll;

    @BeforeEach
    public void setUp() {
        coll = JacksonMongoCollection.<MockObject> builder()
                .withObjectMapper(createObjectMapper())
                .build(getMongoCollection("testJacksonMongoCollection", MockObject.class), MockObject.class, uuidRepresentation);
    }

    @Test
    public void customObjectMapperShouldWorkWhenSerialising() {
        MockObject obj = new MockObject();
        obj.custom = new Custom("hello", "world");
        coll.insert(obj);
        Document custom = getMongoCollection(coll.getName(), Document.class).find().first();
        assertNotNull(custom);
        assertThat(((Document) custom.get("custom")).get("v1")).isEqualTo("hello");
        assertThat(((Document) custom.get("custom")).get("v2")).isEqualTo("world");
    }

    @Test
    public void customObjectMapperShouldWorkWhenDeserialising() {
        MockObject obj = new MockObject();
        obj.custom = new Custom("hello", "world");
        coll.insert(obj);
        MockObject saved = coll.findOne();
        assertNotNull(saved);
        assertNotNull(saved.custom);
        assertThat(saved.custom.value1).isEqualTo("hello");
        assertThat(saved.custom.value2).isEqualTo("world");
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
        module.addDeserializer(Custom.class, new ValueDeserializer<Custom>() {
            @Override
            public Custom deserialize(JsonParser jp, DeserializationContext ctxt)
                    throws JacksonException {
                JsonNode node = jp.readValueAsTree();
                return new Custom(node.get("v1").asText(), node.get("v2")
                        .asText());
            }
        });
        module.addSerializer(Custom.class, new ValueSerializer<Custom>() {
            @Override
            public void serialize(Custom value, JsonGenerator jgen,
                    SerializationContext provider) throws JacksonException {
                jgen.writeStartObject();
                jgen.writeName("v1");
                jgen.writeString(value.value1);
                jgen.writeName("v2");
                jgen.writeString(value.value2);
                jgen.writeEndObject();
            }
        });

        ObjectMapper objectMapper = JsonMapper.builder().addModule(module).build();
        objectMapper = ObjectMapperConfigurer.configureObjectMapper(objectMapper);
        return objectMapper;
    }

}
