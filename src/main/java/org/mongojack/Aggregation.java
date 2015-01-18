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

import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Generic Aggregation object that allows the aggregation operations,
 * and the return type of the AggregationResult to be specified.
 * 
 * @param <T> The type of results to be produced by the aggregation results.
 * 
 * @author Christopher Exell
 * @since 2.1.0
 */
public class Aggregation<T> {
    private Class<T> resultType;
    private DBObject initialOp;
    private DBObject[] additionalOps;

    public Aggregation(Class<T> resultType, DBObject initialOp, DBObject... additionalOps)
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

    public List<DBObject> getAllOps() {
        List<DBObject> allOps = new ArrayList<DBObject>();
        allOps.add(initialOp);
        allOps.addAll(Arrays.asList(additionalOps));
        return allOps;
    }
}
