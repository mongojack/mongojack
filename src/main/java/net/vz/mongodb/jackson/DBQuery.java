/*
 * Copyright 2011 VZ Netzwerke Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.vz.mongodb.jackson;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;
import org.bson.BSONObject;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Builder for MongoDB queries.
 * <p/>
 * The primary way in which this case is used is by calling the below methods on {@link DBCursor}, which is an instance
 * if the Builder.  The static methods on this class may be used when passing in expressions to and(), or() etc.
 * <p/>
 * Any values that are not basic objects (numbers, Strings, booleans, collections etc) that are passed in are attempted
 * to be serialised using Jackson.
 * <p/>
 * Caution needs to be taken when querying entries that are objectIds.  The mapper is at this stage unaware whether a
 * field is stored as an ObjectId or not, so you must pass in any values that are stored as ObjectId as type
 * {@link org.bson.types.ObjectId}.
 *
 * @author James Roper
 * @since 1.2
 */
public class DBQuery {

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
    public static Query in(String field, Collection<Object> values) {
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
    public static Query notIn(String field, Collection<Object> values) {
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
    public static Query all(String field, Collection<Object> values) {
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

    public static abstract class AbstractBuilder<Q extends AbstractBuilder> {
        protected final DBObject query;

        protected AbstractBuilder(DBObject query) {
            this.query = query;
        }

        /**
         * The field is equal to the given value
         *
         * @param field The field to compare
         * @param value The value to compare to
         * @return the query
         */
        public Q is(String field, Object value) {
            return put(field, null, value);
        }

        /**
         * The field is less than the given value
         *
         * @param field The field to compare
         * @param value The value to compare to
         * @return the query
         */
        public Q lessThan(String field, Object value) {
            return put(field, QueryOperators.LT, value);
        }

        /**
         * The field is less than or equal to the given value
         *
         * @param field The field to compare
         * @param value The value to compare to
         * @return the query
         */
        public Q lessThanEquals(String field, Object value) {
            return put(field, QueryOperators.LTE, value);
        }


        /**
         * The field is greater than the given value
         *
         * @param field The field to compare
         * @param value The value to compare to
         * @return the query
         */
        public Q greaterThan(String field, Object value) {
            return put(field, QueryOperators.GT, value);
        }

        /**
         * The field is greater than or equal to the given value
         *
         * @param field The field to compare
         * @param value The value to compare to
         * @return the query
         */
        public Q greaterThanEquals(String field, Object value) {
            return put(field, QueryOperators.GTE, value);
        }

        /**
         * The field is not equal to the given value
         *
         * @param field The field to compare
         * @param value The value to compare to
         * @return the query
         */
        public Q notEquals(String field, Object value) {
            return put(field, QueryOperators.NE, value);
        }

        /**
         * The field is in the given set of values
         *
         * @param field  The field to compare
         * @param values The value to compare to
         * @return the query
         */
        public Q in(String field, Object... values) {
            return put(field, QueryOperators.IN, values);
        }

        /**
         * The field is in the given set of values
         *
         * @param field  The field to compare
         * @param values The value to compare to
         * @return the query
         */
        public Q in(String field, Collection<Object> values) {
            return put(field, QueryOperators.IN, values);
        }

        /**
         * The field is not in the given set of values
         *
         * @param field  The field to compare
         * @param values The value to compare to
         * @return the query
         */
        public Q notIn(String field, Object... values) {
            return put(field, QueryOperators.NIN, values);
        }

        /**
         * The field is not in the given set of values
         *
         * @param field  The field to compare
         * @param values The value to compare to
         * @return the query
         */
        public Q notIn(String field, Collection<Object> values) {
            return put(field, QueryOperators.NIN, values);
        }

        /**
         * The field, modulo the given mod argument, is equal to the value
         *
         * @param field The field to compare
         * @param mod   The modulo
         * @param value The value to compare to
         * @return the query
         */
        public Q mod(String field, Number mod, Number value) {
            return put(field, QueryOperators.MOD, Arrays.asList(mod, value));
        }

        /**
         * The array field contains all of the given values
         *
         * @param field  The field to compare
         * @param values The values to compare to
         * @return the query
         */
        public Q all(String field, Collection<Object> values) {
            return put(field, QueryOperators.ALL, values);
        }

        /**
         * The array field contains all of the given values
         *
         * @param field  The field to compare
         * @param values The values to compare to
         * @return the query
         */
        public Q all(String field, Object... values) {
            return put(field, QueryOperators.ALL, values);
        }

        /**
         * The array field is of the given size
         *
         * @param field The field to compare
         * @param size  The value to compare
         * @return the query
         */
        public Q size(String field, int size) {
            return put(field, QueryOperators.SIZE, size);
        }

        /**
         * The given field exists
         *
         * @param field The field to check
         * @return the query
         */
        public Q exists(String field) {
            return put(field, QueryOperators.EXISTS, true);
        }

        /**
         * The given field doesn't exist
         *
         * @param field The field to check
         * @return the query
         */
        public Q notExists(String field) {
            return put(field, QueryOperators.EXISTS, false);
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
            return put(field, null, regex);
        }

        protected Q put(String field, String op, Object value) {
            if (op == null) {
                query.put(field, value);
            } else {
                DBObject operand;
                Object saved = query.get(field);
                if (!(saved instanceof DBObject)) {
                    operand = new BasicDBObject();
                    query.put(field, operand);
                } else {
                    operand = (DBObject) saved;
                }
                operand.put(op, value);
            }
            return (Q) this;
        }

        protected Q putGroup(String op, Object... expressions) {
            List<Object> saved = (List) query.get(op);
            if (saved == null) {
                saved = new ArrayList<Object>();
                query.put(op, saved);
            }
            saved.addAll(Arrays.asList(expressions));
            return (Q) this;
        }
    }

    /**
     * This is a query builder that is also a valid query that can be passed to MongoDB
     */
    public static class Query extends AbstractBuilder<Query> implements DBObject {

        private Query() {
            super(new BasicDBObject());
        }

        public void markAsPartialObject() {
            query.markAsPartialObject();
        }

        public boolean isPartialObject() {
            return query.isPartialObject();
        }

        public Object put(String key, Object v) {
            return query.put(key, v);
        }

        public void putAll(BSONObject o) {
            query.putAll(o);
        }

        public void putAll(Map m) {
            query.putAll(m);
        }

        public Object get(String key) {
            return query.get(key);
        }

        public Map toMap() {
            return query.toMap();
        }

        public Object removeField(String key) {
            return query.removeField(key);
        }

        public boolean containsKey(String s) {
            return query.containsKey(s);
        }

        public boolean containsField(String s) {
            return query.containsField(s);
        }

        public Set<String> keySet() {
            return query.keySet();
        }
    }
}