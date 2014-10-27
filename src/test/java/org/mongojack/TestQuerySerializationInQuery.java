package org.mongojack;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class TestQuerySerialization extends MongoDBTestBase {

    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setUp() {
        coll = getCollection(MockObject.class, String.class);
    }

    @Test
    public void testSimpleEquals() {
        coll.save(new MockObject());
        String id = coll.findOne().id;
        assertNotNull(coll.findOne(DBQuery.is("_id", id)));
    }

    @Test
    public void testIn() {
        coll.save(new MockObject());
        String id = coll.findOne().id;
        assertThat(coll.find().in("_id", id, new org.bson.types.ObjectId().toString()).toArray(), hasSize(1));
    }

    @Test
    public void testLessThan() {
        MockObject o = new MockObject();
        o.i = 5;
        coll.save(o);
        // Ensure that the serializer actually worked
        assertThat((Integer) coll.getDbCollection().findOne().get("i"), equalTo(15));
        assertThat(coll.find().lessThan("i", 12).toArray(), hasSize(1));
    }

    @Test
    public void testAnd() {
        MockObject o = new MockObject();
        o.i = 5;
        coll.save(o);
        // Ensure that the serializer actually worked
        assertThat(coll.find().and(DBQuery.lessThan("i", 12), DBQuery.greaterThan("i", 4)).toArray(), hasSize(1));
        assertThat(coll.find().and(DBQuery.lessThan("i", 12), DBQuery.greaterThan("i", 9)).toArray(), hasSize(0));
    }
    
    @Test
    public void testAll() {
        MockObject o = new MockObject();
        MockObject o1 = new MockObject();
        o1.id = new org.bson.types.ObjectId().toString();
        o.items = Arrays.asList(o1);
        coll.save(o);
        
        // Ensure that the serializer actually worked
        assertThat(coll.find().all("items", o1).toArray(), hasSize(1));
    }

    @Test
    public void testList() {
        MockObject o = new MockObject();
        MockObject o1 = new MockObject();
        o1.id = new org.bson.types.ObjectId().toString();
        o.items = Arrays.asList(o1);
        coll.save(o);

        assertThat(coll.find().is("items._id", o1.id).toArray(), hasSize(1));
    }

    @Test
    public void testArrayEquals() {
        MockObject o = new MockObject();
        MockObject o1 = new MockObject();
        o1.id = new org.bson.types.ObjectId().toString();
        o.items = Arrays.asList(o1);
        coll.save(o);

        assertThat(coll.find().is("items", Arrays.asList(o1)).toArray(), hasSize(1));
    }

    public static class MockObject {
        @ObjectId
        @Id
        public String id;

        @JsonSerialize(using = PlusTenSerializer.class)
        @JsonDeserialize(using = MinusTenDeserializer.class)
        public int i;

        public List<MockObject> items;
    }

    public static class PlusTenSerializer extends JsonSerializer<Integer> {
        @Override
        public void serialize(Integer value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeNumber(value + 10);
        }
    }

    public static class MinusTenDeserializer extends JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return jp.getValueAsInt() - 10;
        }
    }

}
