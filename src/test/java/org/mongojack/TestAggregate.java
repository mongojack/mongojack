package org.mongojack;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.mock.MockObject;

import com.mongodb.BasicDBObject;

public class TestAggregate extends MongoDBTestBase {
    private JacksonDBCollection<MockObject, String> coll;

    @Before
    public void setup() throws Exception {
        coll = getCollection(MockObject.class, String.class);
        coll.remove(new BasicDBObject());
    }
    
    @Test
    public void testAggregateSingleOpNothingInCollection() {
        AggregationResult<MockObject> result = 
                coll.aggregate(new Aggregation<MockObject>(MockObject.class, 
                new BasicDBObject("$match", new BasicDBObject("booleans", true))));
        Assert.assertEquals(0, result.results().size());
    }
    
    @Test
    public void testAggregateSingleOpItemsInCollection() {
        coll.insert(new MockObject("string1", 1));
        coll.insert(new MockObject("string2", 2));
        AggregationResult<MockObject> result = 
                coll.aggregate(new Aggregation<MockObject>(MockObject.class, 
                new BasicDBObject("$match", new BasicDBObject("string", Pattern.compile(".*")))));
        Assert.assertEquals(2, result.results().size());
    }

    
    
    @Test
    public void testAggregateMultipleOpsItemsInCollection() {
        coll.insert(new MockObject("string1", 1));
        coll.insert(new MockObject("string2", 2));
        AggregationResult<MockObject> result = 
                coll.aggregate(new Aggregation<MockObject>(MockObject.class, 
                new BasicDBObject("$match", new BasicDBObject("string", Pattern.compile("string1")))));
        Assert.assertEquals(1, result.results().size());
        Assert.assertEquals(1, result.results().get(0).integer.intValue());
        result = 
                coll.aggregate(new Aggregation<MockObject>(MockObject.class, 
                new BasicDBObject("$match", new BasicDBObject("string", Pattern.compile(".*"))),
                new BasicDBObject("$match", new BasicDBObject("integer", new BasicDBObject("$gt", new Integer(1))))
                ));
        Assert.assertEquals(1, result.results().size());
    }
    
}
