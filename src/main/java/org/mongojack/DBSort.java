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
package org.mongojack;

import com.mongodb.BasicDBObject;

/**
 * Helper class for building sort specifications.
 * 
 * @since 2.0.0
 * @deprecated Prefer com.mongodb.client.model.Sorts
 */
@Deprecated
public class DBSort {

    /**
     * Sort ascending by the given field.
     * 
     * @param field The field to sort by.
     * @return The sort specification.
     */
    public static SortBuilder asc(String field) {
        return new SortBuilder().asc(field);
    }

    /**
     * Sort descending by the given field.
     * 
     * @param field The field to sort by.
     * @return The sort specification.
     */
    public static SortBuilder desc(String field) {
        return new SortBuilder().desc(field);
    }

    public static class SortBuilder extends BasicDBObject {
        private SortBuilder() {
        }

        /**
         * Sort ascending by the given field.
         * 
         * @param field The field to sort by.
         * @return The sort specification.
         */
        public SortBuilder asc(String field) {
            put(field, 1);
            return this;
        }

        /**
         * Sort descending by the given field.
         * 
         * @param field The field to sort by.
         * @return The sort specification.
         */
        public SortBuilder desc(String field) {
            put(field, -1);
            return this;
        }
    }
}
