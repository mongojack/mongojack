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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.Aggregation.Expression;
import org.mongojack.Aggregation.Group;
import org.mongojack.Aggregation.Pipeline;
import org.mongojack.Aggregation.Project;
import org.mongojack.mock.MockObject;
import org.mongojack.mock.MockObjectAggregationResult;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.MongoException;

import de.flapdoodle.embed.process.collections.Collections;

public class TestAggregationBuilder extends MongoDBTestBase {
    private static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setup() throws Exception {
        coll = getCollection(MockObject.class, String.class);
    }

    @Test
    public void testGroupSumSort() {
        coll.insert(new MockObject("foo", 5));
        coll.insert(new MockObject("foo", 6));
        coll.insert(new MockObject("bar", 101));
        coll.insert(new MockObject("bar", 102));

        Pipeline<?> pipeline = Aggregation.group("string").set("integer", Group.sum("integer")).sort(DBSort.desc("string"));

        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(pipeline, MockObjectAggregationResult.class);

        Assert.assertEquals(2, aggregationResult.results().size());

        Assert.assertEquals("bar", aggregationResult.results().get(0)._id);
        Assert.assertEquals(203, aggregationResult.results().get(0).integer.intValue());

        Assert.assertEquals("foo", aggregationResult.results().get(1)._id);
        Assert.assertEquals(11, aggregationResult.results().get(1).integer.intValue());
    }

    @Test
    public void testMatchGroupMin() {

        coll.insert(new MockObject("foo", 5));
        coll.insert(new MockObject("foo", 6));
        coll.insert(new MockObject("bar", 101));
        coll.insert(new MockObject("bar", 102));

        Pipeline<?> pipeline = Aggregation.match(DBQuery.is("string", "foo")).group("string").set("integer", Group.min("integer"));

        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(pipeline, MockObjectAggregationResult.class);

        Assert.assertEquals(1, aggregationResult.results().size());
        Assert.assertEquals("foo", aggregationResult.results().get(0)._id);
        Assert.assertEquals(5, aggregationResult.results().get(0).integer.intValue());
    }

    @Test
    public void testGroupProject() {

        coll.insert(new MockObject("foo", 5));
        coll.insert(new MockObject("foo", 6));
        coll.insert(new MockObject("bar", 101));
        coll.insert(new MockObject("bar", 102));

        Pipeline<?> pipeline = Aggregation.group("string").set("integer", Group.sum("integer")).project("string", Expression.path("integer"));

        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(pipeline, MockObjectAggregationResult.class);

        Assert.assertEquals(2, aggregationResult.results().size());
        Assert.assertEquals("203", aggregationResult.results().get(0).string);
        Assert.assertEquals("11", aggregationResult.results().get(1).string);
    }

    @Test
    public void testUnwindGroup() {

        MockObject mock = new MockObject("foo", 5);
        mock.simpleList = new ArrayList<String>();
        mock.simpleList.add("bar");
        mock.simpleList.add("baz");
        mock.simpleList.add("qux");

        coll.insert(mock);

        Pipeline<?> pipeline = Aggregation.unwind("simpleList").group("string").set("integer", Group.sum("integer"));

        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(pipeline, MockObjectAggregationResult.class);

        Assert.assertEquals(1, aggregationResult.results().size());
        Assert.assertEquals(15, aggregationResult.results().get(0).integer.intValue());

    }

    @Test
    public void testLimit() {

        coll.insert(new MockObject("foo", 1));
        coll.insert(new MockObject("bar", 1));
        coll.insert(new MockObject("baz", 1));
        coll.insert(new MockObject("qux", 1));

        Pipeline<?> pipeline = Aggregation.group("string").limit(2);

        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(pipeline, MockObjectAggregationResult.class);

        Assert.assertEquals(2, aggregationResult.results().size());

    }
    
    @Test
    public void testMatchUnaryComparison() {

        coll.insert(new MockObject("foo", 1));
        coll.insert(new MockObject("bar", 2));
        coll.insert(new MockObject("baz", 3));
        coll.insert(new MockObject("qux", 4));

        Pipeline<?> pipeline = Aggregation.match(DBQuery.greaterThan("integer", 2));

        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(pipeline, MockObjectAggregationResult.class);

        Assert.assertEquals(2, aggregationResult.results().size());
        for (MockObjectAggregationResult mockObjectAggregationResult : aggregationResult.results()) {
            Assert.assertTrue(mockObjectAggregationResult.integer > 2);
        }

    }
    
    @Test
    public void testMatchIn() {

        coll.insert(new MockObject("foo", 1));
        coll.insert(new MockObject("bar", 2));
        coll.insert(new MockObject("baz", 3));
        coll.insert(new MockObject("qux", 4));

        Pipeline<?> pipeline = Aggregation.match(DBQuery.in("string", "foo", "baz"));

        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(pipeline, MockObjectAggregationResult.class);

        Assert.assertEquals(2, aggregationResult.results().size());
        for (MockObjectAggregationResult mockObjectAggregationResult : aggregationResult.results()) {
            Assert.assertTrue(mockObjectAggregationResult.string.equals("foo") ||  mockObjectAggregationResult.string.equals("baz"));
        }

    }

    static class User {
        @Id String name;
        @JsonProperty Date joined;
        @JsonProperty List<String> likes;

        User(String name, Date joined, List<String> likes) {
            this.name = name;
            this.joined = joined;
            this.likes = likes;
        }
    }

    @Test
    public void testSize() {
        MockObject foo = new MockObject("foo", 1);
        foo.simpleList = Arrays.asList(new String[] {"one", "two"});
        coll.insert(foo);

        MockObject bar = new MockObject("bar", 2);
        bar.simpleList = Arrays.asList(new String[] {"uno", "dos", "tres"});
        coll.insert(bar);

        MockObject baz = new MockObject("baz", 3);
        baz.simpleList = Arrays.asList(new String[] {});
        coll.insert(baz);

        Pipeline<?> pipeline = Aggregation.project("integer", Expression.size(Expression.list("simpleList")));
        System.err.println("pipeline: " + coll.serializePipeline(pipeline));
        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(pipeline, MockObjectAggregationResult.class);

        Assert.assertEquals(3, aggregationResult.results().size());
        Assert.assertEquals(2, aggregationResult.results().get(0).integer.intValue());
        Assert.assertEquals(3, aggregationResult.results().get(1).integer.intValue());
        Assert.assertEquals(0, aggregationResult.results().get(2).integer.intValue());

        coll.insert(new MockObject("bat", 4)); // simpleList does not exist
        pipeline = Aggregation.project("integer",
                Expression.size(Expression.ifNull(Expression.list("simpleList"),
                        Expression.literal(Collections.newArrayList()))));
        aggregationResult = coll.aggregate(pipeline, MockObjectAggregationResult.class);
        Assert.assertEquals(4, aggregationResult.results().size());
        Assert.assertEquals(0, aggregationResult.results().get(3).integer.intValue());
    }

    @Test
    public void testOperatorExpressions() throws MongoException, ParseException {
        // based on http://docs.mongodb.org/manual/tutorial/aggregation-with-user-preference-data/
        JacksonDBCollection<User, String> users = getCollection(User.class, String.class);
        users.insert(new User("jane", ISO_DATE_FORMAT.parse("2011-03-02"), Collections.newArrayList("golf", "racquetball")));
        users.insert(new User("joe", ISO_DATE_FORMAT.parse("2012-07-02"), Collections.newArrayList("tennis", "golf", "swimming")));

        Pipeline pipeline = new Pipeline<Expression<?>>(Project
                .field("month_joined", Expression.month(Expression.date("joined")))
                .set("name", Expression.path("_id"))
                .excludeId())
                .sort(DBSort.asc("month_joined"));
        List<Object> results = users.aggregate(pipeline, Object.class).results();
        Assert.assertEquals(2, results.size());
        Map<String, Object> firstResult = (Map<String, Object>) results.get(0);
        Assert.assertEquals(3, firstResult.get("month_joined"));
        Assert.assertEquals("jane", firstResult.get("name"));
        Assert.assertEquals(2, firstResult.keySet().size());
        Map<String, Object> secondResult = (Map<String, Object>) results.get(1);
        Assert.assertEquals(7, secondResult.get("month_joined"));
        Assert.assertEquals("joe", secondResult.get("name"));
        Assert.assertEquals(2, secondResult.keySet().size());
    }
}
