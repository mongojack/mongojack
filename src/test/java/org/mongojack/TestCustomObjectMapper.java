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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.internal.MongoJackModule;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class TestCustomObjectMapper extends MongoDBTestBase {

    private JacksonMongoCollection<MockObject> coll;

    @Before
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
        assertThat(custom.getString("v1"), equalTo("hello"));
        assertThat(custom.getString("v2"), equalTo("world"));
        assertThat(retrieved.get("uriStringMap", Document.class).getString("foo%2Ebar"), equalTo("001"));
        assertThat(retrieved.get("uriStringMap", Document.class).getString("baz%24qux"), equalTo("002"));
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
        assertThat(saved.custom.value1, equalTo("hello"));
        assertThat(saved.custom.value2, equalTo("world"));
        assertThat(saved.uriStringMap, equalTo(obj.uriStringMap));
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
                Updates.set("uriStringMap", obj.uriStringMap)
            ),
            new UpdateOptions().upsert(true)
        );

        MockObject saved = coll.findOne();
        assertNotNull(saved);
        assertNotNull(saved.custom);
        assertThat(saved.custom.value1, equalTo("hello"));
        assertThat(saved.custom.value2, equalTo("world"));
        assertThat(saved.uriStringMap, equalTo(obj.uriStringMap));
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
        module.addKeySerializer(URI.class, new JsonSerializer<URI>() {
            @Override
            public void serialize(final URI value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                } else {
                    gen.writeFieldName(value.toString().replace(".", "%2E").replace("$","%24"));
                }
            }
        });
        module.addKeyDeserializer(URI.class, new KeyDeserializer() {
            @Override
            public Object deserializeKey(final String key, final DeserializationContext ctxt) throws IOException {
                return URI.create(key.replace("%2E",".").replace("%24","$"));
            }
        });

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        MongoJackModule.configure(objectMapper);
        return objectMapper;
    }

}
