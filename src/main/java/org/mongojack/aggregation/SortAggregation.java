package org.mongojack.aggregation;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Fluent builder for the mongo Sort aggregation pipeline operation
 * 
 * @see <a href="http://docs.mongodb.org/manual/reference/operator/aggregation/sort/">Mongo Aggregation - Sort</a>
 */
public class SortAggregation extends AggregatePipelineOperation {

    private DBObject sort;

    public SortAggregation() {
        sort = new BasicDBObject("$sort", new BasicDBObject());
    }

    /**
     * Sort ascending
     * 
     * @param field the field to sort by
     * @return the builder
     */
    public SortAggregation ascending(String field) {
        return sortOrder(field, 1);
    }

    /**
     * Sort descending
     * 
     * @param field the field to sort by
     * @return the builder
     */
    public SortAggregation descending(String field) {
        return sortOrder(field, -1);
    }

    /**
     * @param field
     * @param order -1 for descening and 1 for asending
     */
    private SortAggregation sortOrder(String field, int order) {
        ((BasicDBObject) sort.get("$sort")).append(field, order);
        return this;
    }

    @Override
    public DBObject apply() {
        return sort;
    }

    @Override
    public String toString() {
        return sort.toString();
    }
}
