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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.*;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mongojack.mock.MockObject;
import org.mongojack.mock.MockObjectAggregationResult;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestAggregationBuilder extends MongoDBTestBase {
    private static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private JacksonMongoCollection<MockObject> coll;

    @BeforeEach
    public void setup() {
        coll = getCollection(MockObject.class);
    }

    @Test
    public void testGroupSumSort() {
        coll.insert(new MockObject("foo", 5));
        coll.insert(new MockObject("foo", 6));
        coll.insert(new MockObject("bar", 101));
        coll.insert(new MockObject("bar", 102));

        final AggregateIterable<MockObjectAggregationResult> aggregate = coll.aggregate(
            List.of(
                Aggregates.group("$string", Accumulators.sum("integer", "$integer")),
                Aggregates.sort(Sorts.ascending("_id"))
            ),
            MockObjectAggregationResult.class
        );
        final List<MockObjectAggregationResult> resultsList = StreamSupport.stream(aggregate.spliterator(), false)
            .collect(Collectors.toList());

        assertEquals(2, resultsList.size());

        assertEquals("bar", resultsList.get(0)._id);
        assertEquals(203, resultsList.get(0).integer.intValue());

        assertEquals("foo", resultsList.get(1)._id);
        assertEquals(11, resultsList.get(1).integer.intValue());
    }

    @Test
    public void testMatchGroupMin() {

        coll.insert(new MockObject("foo", 5));
        coll.insert(new MockObject("foo", 6));
        coll.insert(new MockObject("bar", 101));
        coll.insert(new MockObject("bar", 102));

        // TODO: Can I get the expected MqlValue stuff to work?  current().getString("string")
        List<Bson> pipeline = List.of(
            Aggregates.match(Filters.eq("string", "foo")),
            Aggregates.group("$string", Accumulators.min("integer", "$integer"))
        );

        final AggregateIterable<MockObjectAggregationResult> aggregate = coll.aggregate(pipeline, MockObjectAggregationResult.class);
        final List<MockObjectAggregationResult> resultsList = StreamSupport.stream(aggregate.spliterator(), false)
            .collect(Collectors.toList());

        assertEquals(1, resultsList.size());
        assertEquals("foo", resultsList.get(0)._id);
        assertEquals(5, resultsList.get(0).integer.intValue());
    }

    @Test
    public void testGroupProject() {

        coll.insert(new MockObject("foo", 5));
        coll.insert(new MockObject("foo", 6));
        coll.insert(new MockObject("bar", 101));
        coll.insert(new MockObject("bar", 102));

        List<Bson> pipeline = List.of(
            Aggregates.group(
                "$string",
                Accumulators.sum("integer", "$integer")
            ),
            Aggregates.project(Projections.computed("string", "$integer")),
            Aggregates.sort(Sorts.ascending("_id"))
        );

        final AggregateIterable<MockObjectAggregationResult> aggregate = coll.aggregate(pipeline, MockObjectAggregationResult.class);
        final List<MockObjectAggregationResult> resultsList = StreamSupport.stream(aggregate.spliterator(), false)
            .collect(Collectors.toList());

        assertEquals(2, resultsList.size());
        assertEquals("203", resultsList.get(0).string);
        assertEquals("11", resultsList.get(1).string);
    }

    @Test
    public void testUnwindGroup() {

        MockObject mock = new MockObject("foo", 5);
        mock.simpleList = new ArrayList<>();
        mock.simpleList.add("bar");
        mock.simpleList.add("baz");
        mock.simpleList.add("qux");

        coll.insert(mock);

        List<Bson> pipeline = List.of(
            Aggregates.unwind("$simpleList"),
            Aggregates.group("$string", Accumulators.sum("integer", "$integer"))
        );

        final AggregateIterable<MockObjectAggregationResult> aggregate = coll.aggregate(pipeline, MockObjectAggregationResult.class);
        final List<MockObjectAggregationResult> resultsList = StreamSupport.stream(aggregate.spliterator(), false)
            .collect(Collectors.toList());

        assertEquals(1, resultsList.size());
        assertEquals(15, resultsList.get(0).integer.intValue());

    }

    @Test
    public void testLimit() {

        coll.insert(new MockObject("foo", 1));
        coll.insert(new MockObject("bar", 1));
        coll.insert(new MockObject("baz", 1));
        coll.insert(new MockObject("qux", 1));

        List<Bson> pipeline = List.of(
            Aggregates.group("$string"),
            Aggregates.limit(2)
        );

        final AggregateIterable<MockObjectAggregationResult> aggregate = coll.aggregate(pipeline, MockObjectAggregationResult.class);
        final List<MockObjectAggregationResult> resultsList = StreamSupport.stream(aggregate.spliterator(), false)
            .collect(Collectors.toList());

        assertEquals(2, resultsList.size());
    }

    @Test
    public void testMatchUnaryComparison() {

        coll.insert(new MockObject("foo", 1));
        coll.insert(new MockObject("bar", 2));
        coll.insert(new MockObject("baz", 3));
        coll.insert(new MockObject("qux", 4));

        List<Bson> pipeline = List.of(Aggregates.match(Filters.gt("integer", 2)));

        final AggregateIterable<MockObjectAggregationResult> aggregate = coll.aggregate(pipeline, MockObjectAggregationResult.class);
        final List<MockObjectAggregationResult> resultsList = StreamSupport.stream(aggregate.spliterator(), false)
            .collect(Collectors.toList());

        assertEquals(2, resultsList.size());
        for (MockObjectAggregationResult mockObjectAggregationResult : resultsList) {
            assertTrue(mockObjectAggregationResult.integer > 2);
        }

    }

    @Test
    public void testMatchIn() {

        coll.insert(new MockObject("foo", 1));
        coll.insert(new MockObject("bar", 2));
        coll.insert(new MockObject("baz", 3));
        coll.insert(new MockObject("qux", 4));

        List<Bson> pipeline = List.of(Aggregates.match(Filters.in("string", "foo", "baz")));

        final AggregateIterable<MockObjectAggregationResult> aggregate = coll.aggregate(pipeline, MockObjectAggregationResult.class);
        final List<MockObjectAggregationResult> resultsList = StreamSupport.stream(aggregate.spliterator(), false)
            .collect(Collectors.toList());

        assertEquals(2, resultsList.size());
        for (MockObjectAggregationResult mockObjectAggregationResult : resultsList) {
            assertTrue(mockObjectAggregationResult.string.equals("foo") || mockObjectAggregationResult.string.equals("baz"));
        }

    }

    @Test
    public void testOut() {
        coll.insert(new MockObject("foo", 1));
        coll.insert(new MockObject("bar", 2));
        coll.insert(new MockObject("baz", 3));
        coll.insert(new MockObject("qux", 4));

        List<Bson> pipeline = List.of(Aggregates.match(Filters.gt("integer", 2)), Aggregates.out("testOut"));

        coll.aggregate(pipeline, MockObject.class).toCollection();

        JacksonMongoCollection<MockObject> outCollection = getCollection(MockObject.class, "testOut");
        assertEquals(2, outCollection.countDocuments());
        for (final MockObject object : outCollection.find()) {
            assertTrue(object.string.equals("baz") || object.string.equals("qux"));
        }
    }

    @Test
    public void testProjectArrayElemAt() {
        MockObject object = new MockObject();
        object.simpleList = new ArrayList<>();
        object.simpleList.add("foo");
        object.simpleList.add("bar");
        coll.insert(object);

        List<Bson> pipeline = List.of(
            Aggregates.project(Projections.computed("string", new Document("$arrayElemAt", List.of("$simpleList", 1))))
        );

        final AggregateIterable<MockObjectAggregationResult> aggregate = coll.aggregate(pipeline, MockObjectAggregationResult.class);
        final List<MockObjectAggregationResult> resultsList = StreamSupport.stream(aggregate.spliterator(), false)
            .collect(Collectors.toList());

        assertEquals("bar", resultsList.get(0).string);
    }

    static class User {
        @Id
        String name;
        @JsonProperty
        Date joined;
        @JsonProperty
        List<String> likes;

        User(String name, Date joined, List<String> likes) {
            this.name = name;
            this.joined = joined;
            this.likes = likes;
        }
    }

    @Test
    public void testSize() {
        MockObject foo = new MockObject("foo", 1);
        foo.simpleList = List.of("one", "two");
        coll.insert(foo);

        MockObject bar = new MockObject("bar", 2);
        bar.simpleList = List.of("uno", "dos", "tres");
        coll.insert(bar);

        MockObject baz = new MockObject("baz", 3);
        baz.simpleList = java.util.Collections.emptyList();
        coll.insert(baz);

        List<Bson> pipeline = List.of(Aggregates.project(Projections.computed("integer", new Document("$size", "$simpleList"))));
        final AggregateIterable<MockObjectAggregationResult> aggregate = coll.aggregate(pipeline, MockObjectAggregationResult.class);
        final List<MockObjectAggregationResult> resultsList = StreamSupport.stream(aggregate.spliterator(), false)
            .collect(Collectors.toList());

        assertEquals(3, resultsList.size());
        assertEquals(2, resultsList.get(0).integer.intValue());
        assertEquals(3, resultsList.get(1).integer.intValue());
        assertEquals(0, resultsList.get(2).integer.intValue());

        coll.insert(new MockObject("bat", 4)); // simpleList does not exist
        pipeline = List.of(Aggregates.project(Projections.computed("integer", new Document("$size", new Document("$ifNull", List.of("$simpleList", List.of()))))));
        final AggregateIterable<MockObjectAggregationResult> aggregate2 = coll.aggregate(pipeline, MockObjectAggregationResult.class);
        final List<MockObjectAggregationResult> resultsList2 = StreamSupport.stream(aggregate2.spliterator(), false)
            .collect(Collectors.toList());
        assertEquals(4, resultsList2.size());
        assertEquals(0, resultsList2.get(3).integer.intValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOperatorExpressions() throws MongoException, ParseException {
        // based on http://docs.mongodb.org/manual/tutorial/aggregation-with-user-preference-data/
        JacksonMongoCollection<User> users = getCollection(User.class);
        users.insert(new User("jane", ISO_DATE_FORMAT.parse("2011-03-02"), List.of("golf", "racquetball")));
        users.insert(new User("joe", ISO_DATE_FORMAT.parse("2012-07-02"), List.of("tennis", "golf", "swimming")));

        List<Bson> pipeline = List.of(
            Aggregates.project(
                Projections.fields(
                    Projections.computed("month_joined", new Document("$month", "$joined")),
                    Projections.computed("name", "$_id"),
                    Projections.excludeId()
                )
            ),
            Aggregates.sort(Sorts.ascending("month_joined"))
        );
        List<Object> results = StreamSupport.stream(users.aggregate(pipeline, Object.class).spliterator(), false).collect(Collectors.toList());
        assertEquals(2, results.size());
        Map<String, Object> firstResult = (Map<String, Object>) results.get(0);
        assertEquals(3, firstResult.get("month_joined"));
        assertEquals("jane", firstResult.get("name"));
        assertEquals(2, firstResult.keySet().size());
        Map<String, Object> secondResult = (Map<String, Object>) results.get(1);
        assertEquals(7, secondResult.get("month_joined"));
        assertEquals("joe", secondResult.get("name"));
        assertEquals(2, secondResult.keySet().size());
    }

    @Test
    public void testOperatorExpressions2() throws MongoException, ParseException {
        // based on http://docs.mongodb.org/manual/tutorial/aggregation-with-user-preference-data/
        JacksonMongoCollection<User> users = getCollection(User.class);
        users.insert(new User("jane", ISO_DATE_FORMAT.parse("2011-03-02"), List.of("golf", "racquetball")));
        users.insert(new User("joe", ISO_DATE_FORMAT.parse("2012-07-02"), List.of("tennis", "golf", "swimming")));

        final BsonDocument bsonDocument = Aggregates.project(
            Projections.fields(
                Projections.computed(
                    "month_joined",
                    new Document("$month", "$joined")
                ),
                new Document("name", "$_id"),
                Projections.excludeId()
            )
        ).toBsonDocument(User.class, users.getCodecRegistry());

        List<Object> results = users.aggregate(
                List.of(
                    Aggregates.project(
                        Projections.fields(
                            Projections.computed(
                                "month_joined",
                                new Document("$month", "$joined")
                            ),
                            new Document("name", "$_id"),
                            Projections.excludeId()
                        )
                    ),
                    Aggregates.sort(Sorts.ascending("month_joined"))
                ),
                Object.class
            )
            .into(new ArrayList<>());
        assertEquals(2, results.size());
        Map<String, Object> firstResult = (Map<String, Object>) results.get(0);
        assertEquals(3, firstResult.get("month_joined"));
        assertEquals("jane", firstResult.get("name"));
        assertEquals(2, firstResult.keySet().size());
        Map<String, Object> secondResult = (Map<String, Object>) results.get(1);
        assertEquals(7, secondResult.get("month_joined"));
        assertEquals("joe", secondResult.get("name"));
        assertEquals(2, secondResult.keySet().size());
    }
}
