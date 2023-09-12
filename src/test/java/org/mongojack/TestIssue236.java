package org.mongojack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Filters;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class TestIssue236 extends MongoDBTestBase {
    private JacksonMongoCollection<Person> personColl;
    private JacksonMongoCollection<Metric> metricColl;

    @Before
    public void setup() {
        personColl = getCollection(Person.class);
        metricColl = getCollection(Metric.class);
    }

    @Test
    public void testSimple() throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();

        final Metric metric = new Metric("metric-1", 123, 0.8);

        assertThat(objectMapper.readValue(objectMapper.writeValueAsString(metric), Metric.class), instanceOf(Metric.class));

        metricColl.insertOne(metric);

        final Metric metricRecord = metricColl.find(Filters.eq("name", "metric-1")).first();

        assertThat(metricRecord, instanceOf(Metric.class));
        assertThat(metricRecord, notNullValue());
        assertThat(metricRecord.getCount(), equalTo(123));
        assertThat(metricRecord.getPercentage(), equalTo(0.8));
    }

    @Test
    public void testSubtypes() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        final PolitePerson politePerson = new PolitePerson(1); // Person with an "int" politeness
        final GrumpyPerson grumpyPerson = new GrumpyPerson(0.1); // Person with a "double" politeness

        // The serialize/deserialize cycle works when just plain Jackson
        assertThat(objectMapper.readValue(objectMapper.writeValueAsString(politePerson), Person.class), instanceOf(PolitePerson.class));
        assertThat(objectMapper.readValue(objectMapper.writeValueAsString(grumpyPerson), Person.class), instanceOf(GrumpyPerson.class));

        personColl.insertOne(politePerson);
        personColl.insertOne(grumpyPerson);

        final PolitePerson politePersonRecord = (PolitePerson) personColl.find(Filters.eq("type", "POLITE")).first();
        final GrumpyPerson grumpyPersonRecord = (GrumpyPerson) personColl.find(Filters.eq("type", "GRUMPY")).first();

        assertThat(politePersonRecord, instanceOf(PolitePerson.class));
        assertThat(grumpyPersonRecord, instanceOf(GrumpyPerson.class));
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = GrumpyPerson.class, name = "GRUMPY"),
        @JsonSubTypes.Type(value = PolitePerson.class, name = "POLITE"),
    })
    @JsonIgnoreProperties("_id")
    public static abstract class Person {

        @JsonProperty("type")
        public abstract String getType();
    }

    public static class GrumpyPerson extends Person {
        private final double politeness;

        public GrumpyPerson(@JsonProperty("politeness") double politeness) {
            this.politeness = politeness;
        }

        @JsonProperty("politeness")
        public double getPoliteness() {
            return politeness;
        }

        @Override
        public String getType() {
            return "GRUMPY";
        }
    }

    public static class PolitePerson extends Person {
        private final int politeness;

        public PolitePerson(@JsonProperty("politeness") int politeness) {
            this.politeness = politeness;
        }

        @JsonProperty("politeness")
        public int getPoliteness() {
            return politeness;
        }

        @Override
        public String getType() {
            return "POLITE";
        }
    }

    @JsonIgnoreProperties("_id")
    public static class Metric {
        private final String name;
        private final int count;
        private final double percentage;

        public Metric(@JsonProperty("name") String name,
                      @JsonProperty("count") int count,
                      @JsonProperty("percentage") double percentage) {
            this.name = name;
            this.count = count;
            this.percentage = percentage;
        }

        @JsonProperty("name")
        public String getName() {
            return name;
        }

        @JsonProperty("count")
        public int getCount() {
            return count;
        }

        @JsonProperty("percentage")
        public double getPercentage() {
            return percentage;
        }
    }
}
