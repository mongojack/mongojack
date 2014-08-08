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

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.aggregation.AggregationBuilder;
import org.mongojack.aggregation.GroupAggregation;
import org.mongojack.aggregation.LimitAggregation;
import org.mongojack.aggregation.MatchAggregation;
import org.mongojack.aggregation.ProjectAggregation;
import org.mongojack.aggregation.SortAggregation;
import org.mongojack.aggregation.UnwindAggregation;
import org.mongojack.mock.MockObject;
import org.mongojack.mock.MockObjectAggregationResult;

public class TestAggregationBuilder extends MongoDBTestBase {
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

        AggregationBuilder<MockObjectAggregationResult> builder = new AggregationBuilder<MockObjectAggregationResult>();
        builder.group(new GroupAggregation("string").withSum("integer", "integer")).sort(new SortAggregation().descending("string"));

        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(builder.build(MockObjectAggregationResult.class));

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

        AggregationBuilder<MockObjectAggregationResult> builder = new AggregationBuilder<MockObjectAggregationResult>();
        builder.match(new MatchAggregation("string", "foo"))
                .group(new GroupAggregation("string").withMin("integer", "integer"));

        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(builder.build(MockObjectAggregationResult.class));

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

        AggregationBuilder<MockObjectAggregationResult> builder = new AggregationBuilder<MockObjectAggregationResult>();
        builder.group(new GroupAggregation("string").withSum("integer", "integer"))
                .project(new ProjectAggregation().set("integer", "string"));

        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(builder.build(MockObjectAggregationResult.class));

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

        AggregationBuilder<MockObjectAggregationResult> builder = new AggregationBuilder<MockObjectAggregationResult>();
        builder.unwind(new UnwindAggregation("simpleList"))
                .group(new GroupAggregation("string").withSum("integer", "integer"));

        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(builder.build(MockObjectAggregationResult.class));

        Assert.assertEquals(1, aggregationResult.results().size());
        Assert.assertEquals(15, aggregationResult.results().get(0).integer.intValue());

    }

    @Test
    public void testLimit() {

        coll.insert(new MockObject("foo", 1));
        coll.insert(new MockObject("bar", 1));
        coll.insert(new MockObject("baz", 1));
        coll.insert(new MockObject("qux", 1));

        AggregationBuilder<MockObjectAggregationResult> builder = new AggregationBuilder<MockObjectAggregationResult>();
        builder.group(new GroupAggregation("string"))
                .limit(new LimitAggregation(2));

        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(builder.build(MockObjectAggregationResult.class));

        Assert.assertEquals(2, aggregationResult.results().size());

    }
    
    @Test
    public void testMatchUnaryComparison() {

        coll.insert(new MockObject("foo", 1));
        coll.insert(new MockObject("bar", 2));
        coll.insert(new MockObject("baz", 3));
        coll.insert(new MockObject("qux", 4));

        AggregationBuilder<MockObjectAggregationResult> builder = new AggregationBuilder<MockObjectAggregationResult>();
        builder.match(new MatchAggregation().greaterThan("integer", 2));

        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(builder.build(MockObjectAggregationResult.class));

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

        AggregationBuilder<MockObjectAggregationResult> builder = new AggregationBuilder<MockObjectAggregationResult>();
        builder.match(new MatchAggregation().in("string", "foo", "baz"));

        AggregationResult<MockObjectAggregationResult> aggregationResult = coll.aggregate(builder.build(MockObjectAggregationResult.class));

        Assert.assertEquals(2, aggregationResult.results().size());
        for (MockObjectAggregationResult mockObjectAggregationResult : aggregationResult.results()) {
            Assert.assertTrue(mockObjectAggregationResult.string.equals("foo") ||  mockObjectAggregationResult.string.equals("baz"));
        }

    }
}
