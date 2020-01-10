/*
 * Copyright 2011 VZ Netzwerke Ltd
 * Copyright 2014 devbliss GmbH
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
package org.mongojack.internal.query;

import org.mongojack.DBQuery;
import org.mongojack.QueryCondition;

public class CompoundQueryCondition implements QueryCondition {

    private final DBQuery.Query query;
    private final boolean targetIsCollection;

    public CompoundQueryCondition(DBQuery.Query query, boolean targetIsCollection) {
        this.query = query;
        this.targetIsCollection = targetIsCollection;
    }

    public DBQuery.Query getQuery() {
        return query;
    }
    
    public boolean targetIsCollection() {
        return targetIsCollection;
    }
}
