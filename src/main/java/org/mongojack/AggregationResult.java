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
import java.util.Iterator;
import java.util.List;

import com.mongodb.AggregationOutput;
import com.mongodb.DBObject;

/**
 * A Generic AggregationResult class that wraps a mongodb AggregationOutput to deserialize
 * the DBObjects coming from mongo into the desired type T.
 * 
 * @param <T> The return type of the aggregation.
 * 
 * @author Christopher Exell
 * @since 2.1.0
 */
public class AggregationResult<T> {

    private AggregationOutput output;
    private List<T> results;
    private JacksonDBCollection<?, ?> collection;
    private Class<T> resultType;

    public AggregationResult(JacksonDBCollection<?, ?> collection, AggregationOutput output, Class<T> resultType) {
        this.output = output;
        this.collection = collection;
        this.resultType = resultType;
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
    public List<T> results() {
        // only generate our results list once
        if (results == null) {
            results = new ArrayList<T>();
            Iterable<DBObject> iterable = output.results();
            Iterator<DBObject> iter = iterable.iterator();
            while (iter.hasNext())
            {
                results.add((T) collection.convertFromDbObject(iter.next(), resultType));
            }
        }
        return results;
    }
}
