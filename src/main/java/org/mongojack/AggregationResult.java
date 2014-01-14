package org.mongojack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mongodb.AggregationOutput;
import com.mongodb.DBObject;

/**
 * A Generic AggregationResult class that wraps a mongodb AggregationOutput to deserialize
 * the DBObjects coming from mongo into the desired type T.
 *
 * @param <T> The return type of the aggregation.
 */
public class AggregationResult<T> {
    
    private AggregationOutput output;
    private List<T> results;
    private JacksonDBCollection<?, ?> collection;
    
    public AggregationResult(JacksonDBCollection<?, ?> collection, AggregationOutput output) {
        this.output = output; 
        this.collection = collection;
    }
    
    /**
     * Return the underlying AggregationOutput Object received from mongo.
     * 
     * @return The AggregationOutput from mongo.
     */
    public AggregationOutput getAggregationOutput()
    {
        return output;
    }
    
    /**
     * returns an iterator to the results of the aggregation converted 
     * back to the type of the generic using the Jackson ObjectMapper
     * from the JacksonDBCollection from which this AggregationResult
     * was created.
     *  
     * @return A List Containing the result of the aggregation.
     */
    @SuppressWarnings("unchecked")
    public List<T> results() {
        // only generate our results list once
        if (results == null) {
            results = new ArrayList<T>();
            Iterable<DBObject> iterable = output.results();
            Iterator<DBObject> iter = iterable.iterator();
            while (iter.hasNext())
            {
                results.add((T) collection.convertFromDbObject(iter.next()));                
            }
        }
        return results;
    }        
}
