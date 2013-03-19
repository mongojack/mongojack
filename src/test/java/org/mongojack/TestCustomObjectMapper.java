package org.mongojack;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.DBObject;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.internal.MongoJacksonMapperModule;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestCustomObjectMapper extends MongoDBTestBase {

    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setUp() {
        coll = getCollection(MockObject.class, String.class, createObjectMapper());
    }

    @Test
    public void customObjectMapperShouldWorkWhenSerialising() {
        MockObject obj = new MockObject();
        obj.custom = new Custom("hello", "world");
        coll.insert(obj);
        DBObject custom = (DBObject) coll.getDbCollection().findOne().get("custom");
        assertNotNull(custom);
        assertThat((String) custom.get("v1") , equalTo("hello"));
        assertThat((String) custom.get("v2") , equalTo("world"));
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
        SimpleModule module = new SimpleModule("MySimpleModule", new Version(1,0,0,null, "", ""));
        module.addDeserializer(Custom.class, new JsonDeserializer<Custom>() {
            @Override
            public Custom deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                JsonNode node = jp.readValueAsTree();
                return new Custom(node.get("v1").asText(), node.get("v2").asText());
            }
        });
        module.addSerializer(Custom.class, new JsonSerializer<Custom>() {
            @Override
            public void serialize(Custom value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
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
        MongoJacksonMapperModule.configure(objectMapper);
        return objectMapper;
    }

}
