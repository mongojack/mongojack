package org.mongojack.aggregation;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Fluent builder for the mongo Limit aggregation pipeline operation
 * 
 * @see <a href="http://docs.mongodb.org/manual/reference/operator/aggregation/limit/">Mongo Aggregation - Limit</a>
 */
public class LimitAggregation extends AggregatePipelineOperation {

    private DBObject limit;

    public LimitAggregation(int limitNumber) {
        limit = new BasicDBObject("$limit", limitNumber);
    }

    @Override
    public DBObject apply() {
        return limit;
    }

    @Override
    public String toString() {
        return limit.toString();
    }
}
