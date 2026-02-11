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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.KeyDeserializer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

public class TestCustomObjectMapper extends MongoDBTestBase {

    private JacksonMongoCollection<MockObject> coll;

    @BeforeEach
    public void setUp() {
        coll = getCollection(MockObject.class,
                createObjectMapper());
    }

    @Test
    public void customObjectMapperShouldWorkWhenSerialising() {
        MockObject obj = new MockObject();
        obj.custom = new Custom("hello", "world");
        obj.uriStringMap = new HashMap<>();
        obj.uriStringMap.put(URI.create("foo.bar"), "001");
        obj.uriStringMap.put(URI.create("baz$qux"), "002");
        coll.insert(obj);
        final Document retrieved = getMongoCollection(coll.getName(), Document.class).find().first();
        Document custom = retrieved.get("custom", Document.class);
        assertNotNull(custom);
        assertThat(custom.getString("v1")).isEqualTo("hello");
        assertThat(custom.getString("v2")).isEqualTo("world");
        assertThat(retrieved.get("uriStringMap", Document.class).getString("foo%2Ebar")).isEqualTo("001");
        assertThat(retrieved.get("uriStringMap", Document.class).getString("baz%24qux")).isEqualTo("002");
    }

    @Test
    public void customObjectMapperShouldWorkWhenDeserialising() {
        MockObject obj = new MockObject();
        obj.custom = new Custom("hello", "world");
        obj.uriStringMap = new HashMap<>();
        obj.uriStringMap.put(URI.create("foo.bar"), "001");
        obj.uriStringMap.put(URI.create("baz$qux"), "002");
        coll.insert(obj);
        MockObject saved = coll.findOne();
        assertNotNull(saved);
        assertNotNull(saved.custom);
        assertThat(saved.custom.value1).isEqualTo("hello");
        assertThat(saved.custom.value2).isEqualTo("world");
        assertThat(saved.uriStringMap).isEqualTo(obj.uriStringMap);
    }

    @Test
    public void customObjectMapperShouldWorkForUpdate() {
        MockObject obj = new MockObject();
        obj.id = new org.bson.types.ObjectId().toHexString();
        obj.custom = new Custom("hello", "world");
        obj.uriStringMap = new HashMap<>();
        obj.uriStringMap.put(URI.create("foo.bar"), "001");
        obj.uriStringMap.put(URI.create("baz$qux"), "002");

        coll.updateOne(
                Filters.eq(obj.id),
                Updates.combine(
                        Updates.set("custom", obj.custom),
                        Updates.set("uriStringMap", obj.uriStringMap)),
                new UpdateOptions().upsert(true));

        MockObject saved = coll.findOne();
        assertNotNull(saved);
        assertNotNull(saved.custom);
        assertThat(saved.custom.value1).isEqualTo("hello");
        assertThat(saved.custom.value2).isEqualTo("world");
        assertThat(saved.uriStringMap).isEqualTo(obj.uriStringMap);
    }

    public static class MockObject {
        @Id
        @ObjectId
        public String id;
        public Custom custom;

        public Map<URI, String> uriStringMap;

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
        module.addKeySerializer(URI.class, new ValueSerializer<URI>() {
            @Override
            public void serialize(final URI value, final JsonGenerator gen, final SerializationContext serializers) throws JacksonException {
                if (value == null) {
                    gen.writeNull();
                } else {
                    gen.writeName(value.toString().replace(".", "%2E").replace("$", "%24"));
                }
            }
        });
        module.addKeyDeserializer(URI.class, new KeyDeserializer() {
            @Override
            public Object deserializeKey(final String key, final DeserializationContext ctxt) throws JacksonException {
                return URI.create(key.replace("%2E", ".").replace("%24", "$"));
            }
        });

        ObjectMapper objectMapper = JsonMapper.builder().addModule(module).build();
        objectMapper = ObjectMapperConfigurer.configureObjectMapper(objectMapper);
        return objectMapper;
    }

}
