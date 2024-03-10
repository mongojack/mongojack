/*
 * Copyright 2014 Christopher Exell
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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongojack.mock.MockObject;
import org.mongojack.mock.MockObjectAggregationResult;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestAggregate extends MongoDBTestBase {

    private JacksonMongoCollection<MockObject> coll;

    @BeforeEach
    public void setup() {
        coll = getCollection(MockObject.class);
        coll.deleteMany(new BasicDBObject());
    }

    @Test
    public void testAggregateSingleOpNothingInCollection() {
        final List<MockObject> resultList =
            StreamSupport.stream(coll.aggregate(Collections.singletonList(Aggregates.match(new Document("booleans", true))), MockObject.class).spliterator(), false).collect(Collectors.toList());

        assertEquals(0, resultList.size());
    }

    @Test
    public void testAggregateSingleOpItemsInCollection() {
        coll.insert(new MockObject("string1", 1));
        coll.insert(new MockObject("string2", 2));
        final List<MockObject> resultList = StreamSupport.stream(
                coll.aggregate(
                        Collections.singletonList(
                            new BasicDBObject("$match", new BasicDBObject("string", Pattern.compile(".*")))
                        ),
                        MockObject.class
                    )
                    .spliterator(),
                false
            )
            .collect(Collectors.toList());
        assertEquals(2, resultList.size());
    }

    @Test
    public void testAggregateMultipleOpsItemsInCollection() {
        coll.insert(new MockObject("string1", 1));
        coll.insert(new MockObject("string2", 2));
        List<MockObject> resultList =
            StreamSupport.stream(coll.aggregate(Collections.singletonList(new BasicDBObject("$match", new BasicDBObject("string", Pattern.compile("string1")))
            ), MockObject.class).spliterator(), false).collect(Collectors.toList());
        assertEquals(1, resultList.size());
        assertEquals(1, resultList.get(0).integer.intValue());
        resultList =
            StreamSupport.stream(coll.aggregate(Arrays.asList(
                new BasicDBObject("$match", new BasicDBObject("string", Pattern.compile(".*"))),
                new BasicDBObject("$match", new BasicDBObject("integer", new BasicDBObject("$gt", 1)))
            ), MockObject.class).spliterator(), false).collect(Collectors.toList());
        assertEquals(1, resultList.size());
    }

    @Test
    public void testAggregateMultipleOpsItemsInCollection2() {
        coll.insert(new MockObject("string1", 1));
        coll.insert(new MockObject("string2", 2));
        List<MockObject> resultList = coll.aggregate(Collections.singletonList(Aggregates.match(Filters.eq("string", Pattern.compile("string1")))), MockObject.class).into(new ArrayList<>());
        assertEquals(1, resultList.size());
        assertEquals(1, resultList.get(0).integer.intValue());
        resultList = coll.aggregate(
            Arrays.asList(
                Aggregates.match(Filters.eq("string", Pattern.compile(".*"))),
                Aggregates.match(Filters.gt("integer", 1))
            ),
            MockObject.class
        ).into(new ArrayList<>());
        assertEquals(1, resultList.size());
    }

    @Test
    public void testAggregateAugmentedFieldSetReturnedInDifferentObject() {
        coll.insert(new MockObject("string4", 4));
        coll.insert(new MockObject("string3", 3));
        coll.insert(new MockObject("string2", 2));
        coll.insert(new MockObject("string1", 1));
        coll.insert(new MockObject("string0", 0));
        coll.insert(new MockObject("string-1", -1));
        coll.insert(new MockObject("string-2", -2));
        coll.insert(new MockObject("string-3", -3));
        coll.insert(new MockObject("string-4", -4));

        // build the steps in our aggregation pipeline

        // get the difference between 0 and the document's integer value
        // {$project : { string :1, integer : 1, distance : {$subtract : [0, "$integer"]}}}
        DBObject projection = DBProjection.include("string", "integer");
        projection.put("distance", new BasicDBObject("$subtract", Arrays.asList(0, "$integer")));

        // get the absolute value of the distance
        // {$project : { string :1, integer : 1, distance : {$cond : [ {$lt : ["$distance", 0]}, {$multiply :
        // ["$distance", -1]}, "$distance"]}}},
        DBObject projection2 = DBProjection.include("string", "integer");
        projection2.put("distance", new BasicDBObject(
            "$cond",
            Arrays.asList(
                new BasicDBObject("$lt", Arrays.asList("$distance", 0)),
                new BasicDBObject("$multiply", Arrays.asList("$distance", -1)),
                "$distance"
            )
        ));

        // only get values where the distance is greater than 2
        // {$match : {distance : {$gt : 2}}})
        Bson match = new BasicDBObject("$match", new BasicDBObject("distance", new BasicDBObject("$gt", 2)));

        // build the object that represents the pipeline
        List<Bson> aggregation = Arrays.asList(
            new BasicDBObject("$project", projection),
            new BasicDBObject("$project", projection2),
            match
        );

        // verify that our pipeline returns the expected results
        List<MockObjectAggregationResult> results = StreamSupport.stream(coll.aggregate(aggregation, MockObjectAggregationResult.class).spliterator(), false).collect(Collectors.toList());
        assertEquals(4, results.size());
        HashMap<String, MockObjectAggregationResult> resultMap = new HashMap<>(results.size());
        for (MockObjectAggregationResult result : results) {
            assertTrue(result.distance > 2);
            resultMap.put(result.string, result);
        }

        assertTrue(resultMap.containsKey("string3"));
        assertEquals(Integer.valueOf(3), resultMap.get("string3").distance);

        assertTrue(resultMap.containsKey("string4"));
        assertEquals(Integer.valueOf(4), resultMap.get("string4").distance);

        assertTrue(resultMap.containsKey("string-3"));
        assertEquals(Integer.valueOf(3), resultMap.get("string-3").distance);

        assertTrue(resultMap.containsKey("string-4"));
        assertEquals(Integer.valueOf(4), resultMap.get("string-4").distance);
    }

    @Test
    public void testBsonValueSerialization() {
        coll.insert(new MockObject("string4", 4));
        coll.insert(new MockObject("string3", 3));
        coll.insert(new MockObject("string2", 2));

        List<Bson> pipeline = Collections.singletonList(
            Aggregates.lookup(
                coll.getName(),
                Collections.singletonList(
                    Aggregates.project(
                        Projections.fields(
                            Projections.excludeId(),
                            Projections.computed("insert", Collections.singletonList("hello"))
                        )
                    )
                ),
                "inserts"
            ));
        coll.aggregate(pipeline, Document.class)
            .forEach(o -> {
                // driver 4.3 -> 4.5 changed this from a list of Documents to a list of Maps.
                assertThat(o.get("inserts")).isNotNull();
                assertThat(o.getList("inserts", Object.class)).isInstanceOf(List.class);
                assertThat(o.getList("inserts", Map.class).get(0)).isInstanceOf(Map.class);
                final List<String> insertList = (List<String>) o.getList("inserts", Map.class).get(0).get("insert");
                assertThat(insertList).isInstanceOf(List.class);
                assertThat(insertList.get(0)).isEqualTo("hello");
            });
    }

}
