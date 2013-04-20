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
            for (String field: fields) {
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
            for (String field: fields) {
                put(field, 0);
            }
            return this;
        }
    }
}
