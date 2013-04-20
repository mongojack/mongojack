package org.mongojack;

import com.mongodb.BasicDBObject;

/**
 * Helper class for building sort specifications.
 *
 * @since 2.0.0
 */
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
