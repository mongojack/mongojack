package org.mongojack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Filters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongojack.internal.MongoJackModule;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Test parsing and generating polymorphic types.
 * <p>Related issues:
 *   <ul>
 *     <li><a href="https://github.com/mongojack/mongojack/issues/236">mongojack/mongojack#236</a></li>
 *   </ul>
 * </p>
 */
public class TestParsingAndGeneratingPolymorphicTypes extends MongoDBTestBase {
    private JacksonMongoCollection<Metric> coll;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws Exception {
        objectMapper = MongoJackModule.configure(new ObjectMapper());
        coll = getCollection(Metric.class, objectMapper);
    }

    @Test
    public void testSubtypes() throws Exception {
        final Metric longMetric = new LongMetric(236);
        final Metric doubleMetric = new DoubleMetric(0.8);

        // Validate that the serialize/deserialize cycle works when using plain Jackson
        assertThat(objectMapper.readValue(objectMapper.writeValueAsString(longMetric), Metric.class)).isInstanceOf(LongMetric.class);
        assertThat(objectMapper.readValue(objectMapper.writeValueAsString(doubleMetric), Metric.class)).isInstanceOf(DoubleMetric.class);

        coll.insertOne(longMetric);
        coll.insertOne(doubleMetric);

        final Metric longMetricRecord = coll.findOne(Filters.eq("type", "long"));
        final Metric doubleMetricRecord = coll.findOne(Filters.eq("type", "double"));

        assertThat(longMetricRecord).isInstanceOf(LongMetric.class);
        assertThat(((LongMetric) longMetricRecord).getValue()).isEqualTo(236L);
        assertThat(doubleMetricRecord).isInstanceOf(DoubleMetric.class);
        assertThat(((DoubleMetric) doubleMetricRecord).getValue()).isEqualTo(0.8d);
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = LongMetric.class, name = "long"),
        @JsonSubTypes.Type(value = DoubleMetric.class, name = "double"),
    })
    @JsonIgnoreProperties("_id")
    public static abstract class Metric {
        @JsonProperty("type")
        public abstract String getType();
    }

    public static class LongMetric extends Metric {
        private final long value;

        public LongMetric(@JsonProperty("value") long value) {
            this.value = value;
        }

        @JsonProperty("value")
        public long getValue() {
            return value;
        }

        @Override
        public String getType() {
            return "long";
        }
    }

    public static class DoubleMetric extends Metric {
        private final double value;

        public DoubleMetric(@JsonProperty("value") double value) {
            this.value = value;
        }

        @JsonProperty("value")
        public double getValue() {
            return value;
        }

        @Override
        public String getType() {
            return "double";
        }
    }
}
