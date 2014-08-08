package org.mongojack.aggregation;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Fluent builder for the mongo Unwind aggregation pipeline operation
 * 
 * @see <a href="http://docs.mongodb.org/manual/reference/operator/aggregation/unwind/">Mongo Aggregation - Unwind</a>
 */
public class UnwindAggregation extends AggregatePipelineOperation {

    private DBObject unwind;

    /**
     * Create a new unwind operation
     * 
     * @param arrayFieldName the array field to unwind
     */
    public UnwindAggregation(String arrayFieldName) {
        unwind = new BasicDBObject("$unwind", dollar(arrayFieldName));
    }

    @Override
    public DBObject apply() {
        return unwind;
    }

    @Override
    public String toString() {
        return unwind.toString();
    }
}
