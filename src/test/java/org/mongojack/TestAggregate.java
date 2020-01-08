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
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.mock.MockObject;
import org.mongojack.mock.MockObjectAggregationResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TestAggregate extends MongoDBTestBase {
    
    private JacksonMongoCollection<MockObject> coll;

    @Before
    public void setup() {
        coll = getCollection(MockObject.class);
        coll.remove(new BasicDBObject());
    }

    @Test
    public void testAggregateSingleOpNothingInCollection() {
        Aggregation<MockObject> aggregation = new Aggregation<>(
            MockObject.class,
            new BasicDBObject("$match", new BasicDBObject("booleans", true))
        );

        final List<MockObject> resultList = StreamSupport.stream(coll.aggregate(aggregation.getAllOps(), MockObject.class).spliterator(), false).collect(Collectors.toList());

        Assert.assertEquals(0, resultList.size());
    }

    @Test
    public void testAggregateSingleOpItemsInCollection() {
        coll.insert(new MockObject("string1", 1));
        coll.insert(new MockObject("string2", 2));
        final List<MockObject> resultList =
            StreamSupport.stream(coll.aggregate(new Aggregation<>(
                MockObject.class,
                new BasicDBObject("$match", new BasicDBObject("string", Pattern.compile(".*")))
            ).getAllOps(), MockObject.class).spliterator(), false).collect(Collectors.toList());
        Assert.assertEquals(2, resultList.size());
    }

    @Test
    public void testAggregateMultipleOpsItemsInCollection() {
        coll.insert(new MockObject("string1", 1));
        coll.insert(new MockObject("string2", 2));
        List<MockObject> resultList =
            StreamSupport.stream(coll.aggregate(new Aggregation<>(MockObject.class,
                        new BasicDBObject("$match", new BasicDBObject("string", Pattern.compile("string1")))).getAllOps(), MockObject.class).spliterator(), false).collect(Collectors.toList());
        Assert.assertEquals(1, resultList.size());
        Assert.assertEquals(1, resultList.get(0).integer.intValue());
        resultList =
            StreamSupport.stream(coll.aggregate(new Aggregation<>(
                MockObject.class,
                new BasicDBObject("$match", new BasicDBObject("string", Pattern.compile(".*"))),
                new BasicDBObject("$match", new BasicDBObject("integer", new BasicDBObject("$gt", new Integer(1))))
            ).getAllOps(), MockObject.class).spliterator(), false).collect(Collectors.toList());
        Assert.assertEquals(1, resultList.size());
    }

    @SuppressWarnings("unchecked")
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
        projection2.put("distance", new BasicDBObject("$cond",
                Arrays.asList(new BasicDBObject("$lt", Arrays.asList("$distance", 0)),
                        new BasicDBObject("$multiply", Arrays.asList("$distance", -1)),
                        "$distance")));

        // only get values where the distance is greater than 2
        // {$match : {distance : {$gt : 2}}})
        Bson match = new BasicDBObject("$match", new BasicDBObject("distance", new BasicDBObject("$gt", 2)));

        // build the object that represents the pipeline
        Aggregation<MockObjectAggregationResult> aggregation = new Aggregation<>(
            MockObjectAggregationResult.class,
            new BasicDBObject("$project", projection),
            new BasicDBObject("$project", projection2),
            match
        );

        // verify that our pipeline returns the expected results
        List<MockObjectAggregationResult> results = StreamSupport.stream(coll.aggregate(aggregation.getAllOps(), aggregation.getResultType()).spliterator(), false).collect(Collectors.toList());
        Assert.assertEquals(4, results.size());
        HashMap<String, MockObjectAggregationResult> resultMap = new HashMap<>(results.size());
        for (MockObjectAggregationResult result : results)
        {
            Assert.assertTrue(result.distance > 2);
            resultMap.put(result.string, result);
        }

        Assert.assertTrue(resultMap.containsKey("string3"));
        Assert.assertEquals(new Integer(3), resultMap.get("string3").distance);

        Assert.assertTrue(resultMap.containsKey("string4"));
        Assert.assertEquals(new Integer(4), resultMap.get("string4").distance);

        Assert.assertTrue(resultMap.containsKey("string-3"));
        Assert.assertEquals(new Integer(3), resultMap.get("string-3").distance);

        Assert.assertTrue(resultMap.containsKey("string-4"));
        Assert.assertEquals(new Integer(4), resultMap.get("string-4").distance);
    }
}
