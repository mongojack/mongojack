/*
 * Copyright 2011 VZ Netzwerke Ltd
 * Copyright 2014 devbliss GmbH
 * Copyright 2008-present MongoDB, Inc.
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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.BsonDocument;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.mongojack.internal.query.CollectionQueryCondition;
import org.mongojack.internal.query.CompoundQueryCondition;
import org.mongojack.internal.query.QueryCondition;
import org.mongojack.internal.query.SimpleQueryCondition;
import org.mongojack.internal.util.DocumentSerializationUtils;
import org.mongojack.internal.util.InitializationRequiredForTransformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Builder for MongoDB queries.
 * <p>
 * The primary way in which this case is used is by calling the below methods on {@link DBCursor}, which is an instance
 * if the Builder. The static methods on this class may be used when passing in expressions to and(), or() etc.
 * <p>
 * Any values that are not basic objects (numbers, Strings, booleans, collections etc) that are passed in are attempted
 * to be serialised using Jackson.
 * <p>
 * Caution needs to be taken when querying entries that are objectIds. The mapper is at this stage unaware whether a
 * field is stored as an ObjectId or not, so you must pass in any values that are stored as ObjectId as type
 * {@link org.bson.types.ObjectId}.
 *
 * @author James Roper
 * @since 1.2
 */
public class DBQuery {

    // this was pulled from the original com.mongodb.QueryOperators class, which has been removed
    // likely people should switch to using com.mongodb.client.model.Filters directly
    private static final String GT = "$gt";
    private static final String GTE = "$gte";
    private static final String LT = "$lt";
    private static final String LTE = "$lte";

    private static final String NE = "$ne";
    private static final String IN = "$in";
    private static final String NIN = "$nin";
    private static final String MOD = "$mod";
    private static final String ALL = "$all";
    private static final String SIZE = "$size";
    private static final String EXISTS = "$exists";

    /**
     * Create an empty query
     *
     * @return The empty query
     */
    public static Query empty() {
        return new Query();
    }

    /**
     * The field is equal to the given value
     *
     * @param field The field to compare
     * @param value The value to compare to
     * @return the query
     */
    public static Query is(String field, Object value) {
        return new Query().is(field, value);
    }

    /**
     * The field is less than the given value
     *
     * @param field The field to compare
     * @param value The value to compare to
     * @return the query
     */
    public static Query lessThan(String field, Object value) {
        return new Query().lessThan(field, value);
    }

    /**
     * The field is less than or equal to the given value
     *
     * @param field The field to compare
     * @param value The value to compare to
     * @return the query
     */
    public static Query lessThanEquals(String field, Object value) {
        return new Query().lessThanEquals(field, value);
    }

    /**
     * The field is greater than the given value
     *
     * @param field The field to compare
     * @param value The value to compare to
     * @return the query
     */
    public static Query greaterThan(String field, Object value) {
        return new Query().greaterThan(field, value);
    }

    /**
     * The field is greater than or equal to the given value
     *
     * @param field The field to compare
     * @param value The value to compare to
     * @return the query
     */
    public static Query greaterThanEquals(String field, Object value) {
        return new Query().greaterThanEquals(field, value);
    }

    /**
     * The field is not equal to the given value
     *
     * @param field The field to compare
     * @param value The value to compare to
     * @return the query
     */
    public static Query notEquals(String field, Object value) {
        return new Query().notEquals(field, value);
    }

    /**
     * The field is in the given set of values
     *
     * @param field  The field to compare
     * @param values The value to compare to
     * @return the query
     */
    public static Query in(String field, Object... values) {
        return new Query().in(field, values);
    }

    /**
     * The field is in the given set of values
     *
     * @param field  The field to compare
     * @param values The value to compare to
     * @return the query
     */
    public static Query in(String field, Collection<?> values) {
        return new Query().in(field, values);
    }

    /**
     * The field is not in the given set of values
     *
     * @param field  The field to compare
     * @param values The value to compare to
     * @return the query
     */
    public static Query notIn(String field, Object... values) {
        return new Query().notIn(field, values);
    }

    /**
     * The field is not in the given set of values
     *
     * @param field  The field to compare
     * @param values The value to compare to
     * @return the query
     */
    public static Query notIn(String field, Collection<?> values) {
        return new Query().notIn(field, values);
    }

    /**
     * The field, modulo the given mod argument, is equal to the value
     *
     * @param field The field to compare
     * @param mod   The modulo
     * @param value The value to compare to
     * @return the query
     */
    public static Query mod(String field, Number mod, Number value) {
        return new Query().mod(field, mod, value);
    }

    /**
     * The array field contains all of the given values
     *
     * @param field  The field to compare
     * @param values The values to compare to
     * @return the query
     */
    public static Query all(String field, Collection<?> values) {
        return new Query().all(field, values);
    }

    /**
     * The array field contains all of the given values
     *
     * @param field  The field to compare
     * @param values The values to compare to
     * @return the query
     */
    public static Query all(String field, Object... values) {
        return new Query().all(field, values);
    }

    /**
     * The array field is of the given size
     *
     * @param field The field to compare
     * @param size  The value to compare
     * @return the query
     */
    public static Query size(String field, int size) {
        return new Query().size(field, size);
    }

    /**
     * The given field exists
     *
     * @param field The field to check
     * @return the query
     */
    public static Query exists(String field) {
        return new Query().exists(field);
    }

    /**
     * The given field doesn't exist
     *
     * @param field The field to check
     * @return the query
     */
    public static Query notExists(String field) {
        return new Query().notExists(field);
    }

    /**
     * One of the given expressions matches
     *
     * @param expressions The expressions to test
     * @return the query
     */
    public static Query or(Query... expressions) {
        return new Query().or(expressions);
    }

    /**
     * All of the given expressions match
     *
     * @param expressions The expressions to test
     * @return the query
     */
    public static Query and(Query... expressions) {
        return new Query().and(expressions);
    }

    /**
     * None of the given expressions match
     *
     * @param expressions The expressions to test
     * @return the query
     */
    public static Query nor(Query... expressions) {
        return new Query().nor(expressions);
    }

    /**
     * The given field matches the regular expression
     *
     * @param field The field to comare
     * @param regex The regular expression to match with
     * @return the query
     */
    public static Query regex(String field, Pattern regex) {
        return new Query().regex(field, regex);
    }

    /**
     * An element in the given array field matches the given query
     *
     * @param field the array field
     * @param query The query to attempt to match against the elements of the
     *              array field
     * @return the query
     */
    public static Query elemMatch(String field, Query query) {
        return new Query().elemMatch(field, query);
    }

    /**
     * Execute the given JavaScript code as part of the query
     *
     * @param code the JavaScript code
     * @return the query
     */
    public static Query where(String code) {
        return new Query().where(code);
    }

    public static abstract class AbstractBuilder<Q extends AbstractBuilder> {

        /**
         * The field is equal to the given value
         *
         * @param field The field to compare
         * @param value The value to compare to
         * @return the query
         */
        public Q is(String field, Object value) {
            return put(field, new SimpleQueryCondition(value));
        }

        /**
         * The field is less than the given value
         *
         * @param field The field to compare
         * @param value The value to compare to
         * @return the query
         */
        public Q lessThan(String field, Object value) {
            return put(field, LT,
                new SimpleQueryCondition(value)
            );
        }

        /**
         * The field is less than or equal to the given value
         *
         * @param field The field to compare
         * @param value The value to compare to
         * @return the query
         */
        public Q lessThanEquals(String field, Object value) {
            return put(field, LTE, new SimpleQueryCondition(
                value));
        }

        /**
         * The field is greater than the given value
         *
         * @param field The field to compare
         * @param value The value to compare to
         * @return the query
         */
        public Q greaterThan(String field, Object value) {
            return put(field, GT,
                new SimpleQueryCondition(value)
            );
        }

        /**
         * The field is greater than or equal to the given value
         *
         * @param field The field to compare
         * @param value The value to compare to
         * @return the query
         */
        public Q greaterThanEquals(String field, Object value) {
            return put(field, GTE, new SimpleQueryCondition(
                value));
        }

        /**
         * The field is not equal to the given value
         *
         * @param field The field to compare
         * @param value The value to compare to
         * @return the query
         */
        public Q notEquals(String field, Object value) {
            return put(field, NE,
                new SimpleQueryCondition(value)
            );
        }

        /**
         * The field is in the given set of values
         *
         * @param field  The field to compare
         * @param values The value to compare to
         * @return the query
         */
        public Q in(String field, Object... values) {
            return put(field, IN, Arrays.asList(values));
        }

        /**
         * The field is in the given set of values
         *
         * @param field  The field to compare
         * @param values The value to compare to
         * @return the query
         */
        public Q in(String field, Collection<?> values) {
            return put(field, IN, values);
        }

        /**
         * The field is not in the given set of values
         *
         * @param field  The field to compare
         * @param values The value to compare to
         * @return the query
         */
        public Q notIn(String field, Object... values) {
            return put(field, NIN, Arrays.asList(values));
        }

        /**
         * The field is not in the given set of values
         *
         * @param field  The field to compare
         * @param values The value to compare to
         * @return the query
         */
        public Q notIn(String field, Collection<?> values) {
            return put(field, NIN, values);
        }

        /**
         * The field, modulo the given mod argument, is equal to the value
         * { field: { $mod: [ divisor, remainder ] } }
         *
         * @param field The field to compare
         * @param mod   The modulo
         * @param value The value to compare to
         * @return the query
         */
        public Q mod(String field, Number mod, Number value) {
            return put(
                field,
                MOD,
                new CollectionQueryCondition(
                    Arrays.<QueryCondition>asList(
                        new SimpleQueryCondition(mod, false),
                        new SimpleQueryCondition(value)
                    ),
                    false
                )
            );
        }

        /**
         * The array field contains all of the given values
         *
         * @param field  The field to compare
         * @param values The values to compare to
         * @return the query
         */
        public Q all(String field, Collection<?> values) {
            return put(field, ALL, values);
        }

        /**
         * The array field contains all of the given values
         *
         * @param field  The field to compare
         * @param values The values to compare to
         * @return the query
         */
        public Q all(String field, Object... values) {
            return all(field, Arrays.asList(values));
        }

        /**
         * The array field is of the given size
         *
         * @param field The field to compare
         * @param size  The value to compare
         * @return the query
         */
        public Q size(String field, int size) {
            return put(field, SIZE, new SimpleQueryCondition(
                size, false));
        }

        /**
         * The given field exists
         *
         * @param field The field to check
         * @return the query
         */
        public Q exists(String field) {
            return put(field, EXISTS, new SimpleQueryCondition(
                true, false));
        }

        /**
         * The given field doesn't exist
         *
         * @param field The field to check
         * @return the query
         */
        public Q notExists(String field) {
            return put(field, EXISTS, new SimpleQueryCondition(
                false, false));
        }

        /**
         * One of the given expressions matches
         *
         * @param expressions The expressions to test
         * @return the query
         */
        public Q or(Query... expressions) {
            return putGroup("$or", expressions);
        }

        /**
         * All of the given expressions matches
         *
         * @param expressions The expressions to test
         * @return the query
         */
        public Q and(Query... expressions) {
            return putGroup("$and", expressions);
        }

        /**
         * None of the given expressions matches
         *
         * @param expressions The expressions to test
         * @return the query
         */
        public Q nor(Query... expressions) {
            return putGroup("$nor", expressions);
        }

        /**
         * The given field matches the regular expression
         *
         * @param field The field to comare
         * @param regex The regular expression to match with
         * @return the query
         */
        public Q regex(String field, Pattern regex) {
            return put(field, new SimpleQueryCondition(regex, false));
        }

        /**
         * An element in the given array field matches the given query
         *
         * @param field the array field
         * @param query The query to attempt to match against the elements of the
         *              array field
         * @return the query
         */
        public Q elemMatch(String field, Query query) {
            return put(field, "$elemMatch", new CompoundQueryCondition(query, false));
        }

        /**
         * Execute the given JavaScript code as part of the query
         *
         * @param code the JavaScript code
         * @return the query
         */
        public Q where(String code) {
            return put("$where", new SimpleQueryCondition(code, false));
        }

        protected abstract Q put(String op, QueryCondition value);

        protected abstract Q put(String field, String op, QueryCondition value);

        protected Q put(String field, String op, Collection<?> values) {
            List<QueryCondition> conditions = new ArrayList<QueryCondition>();
            for (Object value : values) {
                conditions.add(new SimpleQueryCondition(value));
            }
            return put(field, op, new CollectionQueryCondition(conditions, true));
        }

        protected abstract Q putGroup(String op, Query... expressions);
    }

    /**
     * This is a query builder that is also a valid query that can be passed to
     * MongoDB
     */
    public static class Query extends AbstractBuilder<Query> implements Bson, InitializationRequiredForTransformation {

        protected final Map<String, QueryCondition> query = new LinkedHashMap<String, QueryCondition>();
        private ObjectMapper objectMapper;
        private JavaType type;

        private Query() {
        }

        public Set<Map.Entry<String, QueryCondition>> conditions() {
            return query.entrySet();
        }

        @Override
        protected Query put(String op, QueryCondition value) {
            query.put(op, value);
            return this;
        }

        @Override
        protected Query put(String field, String op, QueryCondition value) {
            Query subQuery;
            QueryCondition saved = query.get(field);
            if (!(saved instanceof CompoundQueryCondition)) {
                subQuery = new Query();
                boolean targetIsCollection = (value instanceof CollectionQueryCondition && ((CollectionQueryCondition) value).targetIsCollection())
                    || (value instanceof CompoundQueryCondition && ((CompoundQueryCondition) value).targetIsCollection());
                query.put(field, new CompoundQueryCondition(subQuery, targetIsCollection));
            } else {
                subQuery = ((CompoundQueryCondition) saved).getQuery();
            }
            subQuery.put(op, value);
            return this;
        }

        @Override
        protected Query putGroup(String op, Query... expressions) {
            CollectionQueryCondition condition;
            QueryCondition existing = query.get(op);
            if (existing == null) {
                condition = new CollectionQueryCondition();
                query.put(op, condition);
            } else if (existing instanceof CollectionQueryCondition) {
                condition = (CollectionQueryCondition) existing;
            } else {
                throw new IllegalStateException("Expecting collection for "
                    + op);
            }
            List<QueryCondition> conditions = new ArrayList<QueryCondition>();
            for (Query query : expressions) {
                conditions.add(new CompoundQueryCondition(query, false));
            }
            condition.addAll(conditions);
            return this;
        }

        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            return DocumentSerializationUtils.serializeQuery(objectMapper, type, this).toBsonDocument(tDocumentClass, codecRegistry);
        }

        @Override
        public void initialize(final ObjectMapper objectMapper, final JavaType type, final JacksonCodecRegistry codecRegistry) {
            this.objectMapper = objectMapper;
            this.type = type;
        }

    }
}
