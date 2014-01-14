package org.mongojack;

import com.mongodb.DBObject;

/**
 * A Generic Aggregation object that allows the aggregation operations,
 * and the return type of the AggregationResult to be specified.
 *
 * @param <T> The type of results to be produced by the aggregation results.
 */
public class Aggregation<T> {
    private Class<T> resultType;
    private DBObject initialOp;
    private DBObject[] additionalOps;
    
    public Aggregation(Class<T> resultType, DBObject initialOp, DBObject...additionalOps)
    {
        this.resultType = resultType;
        this.initialOp = initialOp;
        this.additionalOps = additionalOps;        
    }
    
    public Class<T> getResultType() {
        return resultType;
    }
    
    public DBObject getInitialOp() {
        return initialOp;
    }
    
    public DBObject[] getAdditionalOps() {
        return additionalOps;
    }
}
