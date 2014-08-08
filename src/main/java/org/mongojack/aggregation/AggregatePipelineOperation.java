package org.mongojack.aggregation;

import com.mongodb.DBObject;

/**
 * Root class of all aggregation pipeline operations
 */
public abstract class AggregatePipelineOperation {

    protected String dollar(String input) {
        return "$" + input;
    }

    public abstract DBObject apply();
}
