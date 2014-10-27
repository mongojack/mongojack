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
 * Helper class for building projections.
 * 
 * @since 2.0.0
 */
public class DBProjection {
    /**
     * Include the given fields in the results.
     * 
     * @param fields The fields to include.
     * @return The projection.
     */
    public static ProjectionBuilder include(String... fields) {
        return new ProjectionBuilder().include(fields);
    }

    /**
     * Exclude the given fields from the results.
     * 
     * @param fields The fields to exclude.
     * @return The projection.
     */
    public static ProjectionBuilder exclude(String... fields) {
        return new ProjectionBuilder().exclude(fields);
    }

    public static class ProjectionBuilder extends BasicDBObject {
        private ProjectionBuilder() {
        }

        /**
         * Include the given fields in the results.
         * 
         * @param fields The fields to include.
         * @return The projection.
         */
        public ProjectionBuilder include(String... fields) {
            for (String field : fields) {
                put(field, 1);
            }
            return this;
        }

        /**
         * Exclude the given fields from the results.
         * 
         * @param fields The fields to exclude.
         * @return The projection.
         */
        public ProjectionBuilder exclude(String... fields) {
            for (String field : fields) {
                put(field, 0);
            }
            return this;
        }
    }
}
