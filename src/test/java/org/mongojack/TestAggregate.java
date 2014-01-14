/* 
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
