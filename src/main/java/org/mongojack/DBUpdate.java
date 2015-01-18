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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongojack.internal.update.ComplexUpdateOperationValue;
import org.mongojack.internal.update.MultiUpdateOperationValue;
import org.mongojack.internal.update.SingleUpdateOperationValue;
import org.mongojack.internal.update.UpdateOperationValue;
import org.mongojack.internal.util.SerializationUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * A database update. This can be used to build queries using the MongoDB
 * modifier operations. It also will do serialisation of values, however it
 * won't honour any custom serialisers specified on the fields that those values
 * are being set.
 * 
 * @author James Roper
 * @since 1.1
 */
public class DBUpdate {

    /**
     * Increment the given field atomically by one
     * 
     * @param field
     *            The field to increment
     * @return this object
     */
    public static Builder inc(String field) {
        return inc(field, 1);
    }

    /**
     * Increment the given field atomically by the given value
     * 
     * @param field
     *            The field to increment
     * @param by
     *            The value to increment by
     * @return this object
     */
    public static Builder inc(String field, int by) {
        return new Builder().inc(field, by);
    }

    /**
     * Set the given field (can be multiple levels deep) to the given value
     * atomically
     * 
     * @param field
     *            The field to set
     * @param value
     *            The value to set it to
     * @return this object
     */
    public static Builder set(String field, Object value) {
        return new Builder().set(field, value);
    }

    /**
     * Unset the given field atomically
     * 
     * @param field
     *            The field to unset
     * @return this object
     */
    public static Builder unset(String field) {
        return new Builder().unset(field);
    }

    /**
     * Add the given value to the array value at the specified field atomically
     * 
     * @param field
     *            The field to add the value to
     * @param value
     *            The value to add
     * @return this object
     */
    public static Builder push(String field, Object value) {
        return new Builder().push(field, value);
    }

    /**
     * Add all of the given values to the array value at the specified field
     * atomically
     * 
     * @param field
     *            The field to add the values to
     * @param values
     *            The values to add
     * @return this object
     */
    public static Builder pushAll(String field, Object... values) {
        return new Builder().pushAll(field, values);
    }

    /**
     * Add all of the given values to the array value at the specified field
     * atomically
     * 
     * @param field
     *            The field to add the values to
     * @param values
     *            The values to add
     * @return this object
     */
    public static Builder pushAll(String field, List<?> values) {
        return new Builder().pushAll(field, values);
    }

    /**
     * Add the given value to the array value if it doesn't already exist in the
     * specified field atomically
     * 
     * @param field
     *            The field to add the value to
     * @param value
     *            The value to add
     * @return this object
     */
    public static Builder addToSet(String field, Object value) {
        return new Builder().addToSet(field, value);
    }

    /**
     * Add the given values to the array value if they don't already exist in
     * the specified field atomically
     * 
     * @param field
     *            The field to add the values to
     * @param values
     *            The values to add
     * @return this object
     */
    public static Builder addToSet(String field, Object... values) {
        return new Builder().addToSet(field, values);
    }

    /**
     * Add the given values to the array value if they don't already exist in
     * the specified field atomically
     * 
     * @param field
     *            The field to add the values to
     * @param values
     *            The values to add
     * @return this object
     */
    public static Builder addToSet(String field, List<?> values) {
        return new Builder().addToSet(field, values);
    }

    /**
     * Remove the first value from the array specified by field atomically
     * 
     * @param field
     *            The field to remove the value from
     * @return this object
     */
    public static Builder popFirst(String field) {
        return new Builder().popFirst(field);
    }

    /**
     * Remove the last value from the array specified by field atomically
     * 
     * @param field
     *            The field to remove the value from
     * @return this object
     */
    public static Builder popLast(String field) {
        return new Builder().popLast(field);
    }

    /**
     * Remove all occurances of value from the array at field
     * 
     * @param field
     *            The field to remove the value from
     * @param value
     *            The value to remove. This may be another query.
     * @return this object
     */
    public static Builder pull(String field, Object value) {
        return new Builder().pull(field, value);
    }

    /**
     * Remove all occurances of the values from the array at field
     * 
     * @param field
     *            The field to remove the values from
     * @param values
     *            The values to remove
     * @return this object
     */
    public static Builder pullAll(String field, Object... values) {
        return new Builder().pullAll(field, values);
    }

    /**
     * Remove all occurances of the values from the array at field
     * 
     * @param field
     *            The field to remove the values from
     * @param values
     *            The values to remove
     * @return this object
     */
    public static Builder pullAll(String field, List<?> values) {
        return new Builder().pullAll(field, values);
    }

    /**
     * Rename the given field to the new field name
     * 
     * @param oldFieldName
     *            The old field name
     * @param newFieldName
     *            The new field name
     * @return this object
     */
    public static Builder rename(String oldFieldName, String newFieldName) {
        return new Builder().rename(oldFieldName, newFieldName);
    }

    /**
     * Perform a bit operation on the given field
     * 
     * @param field
     *            The field to perform the operation on
     * @param operation
     *            The operation to perform
     * @param value
     *            The value
     * @return this object
     */
    public static Builder bit(String field, String operation, int value) {
        return new Builder().bit(field, operation, value);
    }

    /**
     * Perform two bit operations on the given field
     * 
     * @param field
     *            The field to perform the operations on
     * @param operation1
     *            The first operation to perform
     * @param value1
     *            The first value
     * @param operation2
     *            The second operation to perform
     * @param value2
     *            The second value
     * @return this object
     */
    public static Builder bit(String field, String operation1, int value1,
            String operation2, int value2) {
        return new Builder().bit(field, operation1, value1, operation2, value2);
    }

    /**
     * Perform a bitwise and on the given field
     * 
     * @param field
     *            The field to perform the and on
     * @param value
     *            The value
     * @return this object
     */
    public static Builder bitwiseAnd(String field, int value) {
        return new Builder().bitwiseAnd(field, value);
    }

    /**
     * Perform a bitwise or on the given field
     * 
     * @param field
     *            The field to perform the or on
     * @param value
     *            The value
     * @return this object
     */
    public static Builder bitwiseOr(String field, int value) {
        return new Builder().bitwiseOr(field, value);
    }

    /**
     * The builder
     */
    public static class Builder {
        private final Map<String, Map<String, UpdateOperationValue>> update = new HashMap<String, Map<String, UpdateOperationValue>>();

        /**
         * Increment the given field atomically by one
         * 
         * @param field
         *            The field to increment
         * @return this object
         */
        public Builder inc(String field) {
            return inc(field, 1);
        }

        /**
         * Increment the given field atomically by the given value
         * 
         * @param field
         *            The field to increment
         * @param by
         *            The value to increment by
         * @return this object
         */
        public Builder inc(String field, int by) {
            return addOperation("$inc", field, new SingleUpdateOperationValue(
                    false, false, by));
        }

        /**
         * Set the given field (can be multiple levels deep) to the given value
         * atomically
         * 
         * @param field
         *            The field to set
         * @param value
         *            The value to set it to
         * @return this object
         */
        public Builder set(String field, Object value) {
            return addOperation("$set", field, new SingleUpdateOperationValue(
                    false, true, value));
        }

        /**
         * Unset the given field atomically
         * 
         * @param field
         *            The field to unset
         * @return this object
         */
        public Builder unset(String field) {
            return addOperation("$unset", field,
                    new SingleUpdateOperationValue(false, false, 1));
        }

        /**
         * Add the given value to the array value at the specified field
         * atomically
         * 
         * @param field
         *            The field to add the value to
         * @param value
         *            The value to add
         * @return this object
         */
        public Builder push(String field, Object value) {
            return addOperation("$push", field, new SingleUpdateOperationValue(
                    true, true, value));
        }

        /**
         * Add all of the given values to the array value at the specified field
         * atomically
         * 
         * @param field
         *            The field to add the values to
         * @param values
         *            The values to add
         * @return this object
         */
        public Builder pushAll(String field, Object... values) {
            return addOperation("$pushAll", field,
                    new MultiUpdateOperationValue(true, true, values));
        }

        /**
         * Add all of the given values to the array value at the specified field
         * atomically
         * 
         * @param field
         *            The field to add the values to
         * @param values
         *            The values to add
         * @return this object
         */
        public Builder pushAll(String field, List<?> values) {
            return addOperation("$pushAll", field,
                    new MultiUpdateOperationValue(true, true, values));
        }

        /**
         * Add the given value to the array value if it doesn't already exist in
         * the specified field atomically
         * 
         * @param field
         *            The field to add the value to
         * @param value
         *            The value to add
         * @return this object
         */
        public Builder addToSet(String field, Object value) {
            return addOperation("$addToSet", field,
                    new SingleUpdateOperationValue(true, true, value));
        }

        /**
         * Add the given values to the array value if they don't already exist
         * in the specified field atomically
         * 
         * @param field
         *            The field to add the values to
         * @param values
         *            The values to add
         * @return this object
         */
        public Builder addToSet(String field, Object... values) {
            return addOperation("$addToSet", field,
                    new MultiUpdateOperationValue(true, true, values));
        }

        /**
         * Add the given values to the array value if they don't already exist
         * in the specified field atomically
         * 
         * @param field
         *            The field to add the values to
         * @param values
         *            The values to add
         * @return this object
         */
        public Builder addToSet(String field, List<?> values) {
            return addOperation("$addToSet", field,
                    new MultiUpdateOperationValue(true, true, values));
        }

        /**
         * Remove the first value from the array specified by field atomically
         * 
         * @param field
         *            The field to remove the value from
         * @return this object
         */
        public Builder popFirst(String field) {
            return addOperation("$pop", field, new SingleUpdateOperationValue(
                    true, false, -1));
        }

        /**
         * Remove the last value from the array specified by field atomically
         * 
         * @param field
         *            The field to remove the value from
         * @return this object
         */
        public Builder popLast(String field) {
            return addOperation("$pop", field, new SingleUpdateOperationValue(
                    true, false, 1));
        }

        /**
         * Remove all occurances of value from the array at field
         * 
         * @param field
         *            The field to remove the value from
         * @param value
         *            The value to remove. This may be another query.
         * @return this object
         */
        public Builder pull(String field, Object value) {
            return addOperation("$pull", field, new SingleUpdateOperationValue(
                    true, true, value));
        }

        /**
         * Remove all occurances of the values from the array at field
         * 
         * @param field
         *            The field to remove the values from
         * @param values
         *            The values to remove
         * @return this object
         */
        public Builder pullAll(String field, Object... values) {
            return addOperation("$pullAll", field,
                    new MultiUpdateOperationValue(true, true, values));
        }

        /**
         * Remove all occurances of the values from the array at field
         * 
         * @param field
         *            The field to remove the values from
         * @param values
         *            The values to remove
         * @return this object
         */
        public Builder pullAll(String field, List<?> values) {
            return addOperation("$pullAll", field,
                    new MultiUpdateOperationValue(true, true, values));
        }

        /**
         * Rename the given field to the new field name
         * 
         * @param oldFieldName
         *            The old field name
         * @param newFieldName
         *            The new field name
         * @return this object
         */
        public Builder rename(String oldFieldName, String newFieldName) {
            return addOperation("$rename", oldFieldName,
                    new SingleUpdateOperationValue(false, false, newFieldName));
        }

        /**
         * Perform a bit operation on the given field
         * 
         * @param field
         *            The field to perform the operation on
         * @param operation
         *            The operation to perform
         * @param value
         *            The value
         * @return this object
         */
        public Builder bit(String field, String operation, int value) {
            return addOperation("$bit", field, new ComplexUpdateOperationValue(
                    new BasicDBObject(operation, value)));
        }

        /**
         * Perform two bit operations on the given field
         * 
         * @param field
         *            The field to perform the operations on
         * @param operation1
         *            The first operation to perform
         * @param value1
         *            The first value
         * @param operation2
         *            The second operation to perform
         * @param value2
         *            The second value
         * @return this object
         */
        public Builder bit(String field, String operation1, int value1,
                String operation2, int value2) {
            return addOperation("$bit", field,
                    new ComplexUpdateOperationValue(new BasicDBObject(
                            operation1, value1).append(operation2, value2)));
        }

        /**
         * Perform a bitwise and on the given field
         * 
         * @param field
         *            The field to perform the and on
         * @param value
         *            The value
         * @return this object
         */
        public Builder bitwiseAnd(String field, int value) {
            return bit(field, "and", value);
        }

        /**
         * Perform a bitwise or on the given field
         * 
         * @param field
         *            The field to perform the or on
         * @param value
         *            The value
         * @return this object
         */
        public Builder bitwiseOr(String field, int value) {
            return bit(field, "or", value);
        }

        /**
         * Add a raw operation. This may be useful in case of MongoDB adding new
         * features that aren't yet available through this interface, or if
         * something has been left out. Note that no serialisation will be
         * attempted of the values.
         * 
         * @param op
         *            The operation
         * @param field
         *            The field to set the value on
         * @param value
         *            The value to set
         * @return this object
         */
        public Builder addRawOperation(String op, String field, Object value) {
            return addOperation(op, field, new SingleUpdateOperationValue(
                    false, false, value));
        }

        /**
         * Add an operation to the update
         * 
         * @param modifier
         *            The modifier of the operation
         * @param field
         *            The field to set
         * @param value
         *            The value to modify it with.
         * @return this object
         */
        public Builder addOperation(String modifier, String field,
                UpdateOperationValue value) {
            if (update.containsKey(modifier)) {
                Map<String, UpdateOperationValue> existing = update
                        .get(modifier);
                existing.put(field, value);
            } else {
                Map<String, UpdateOperationValue> newMap = new HashMap<String, UpdateOperationValue>();
                newMap.put(field, value);
                update.put(modifier, newMap);
            }
            return this;
        }

        /**
         * Serialise the values of the query and get them
         * 
         * @param objectMapper
         *            The object mapper to use to serialise values
         * @return The object
         */
        public DBObject serialiseAndGet(ObjectMapper objectMapper,
                JavaType javaType) {
            return SerializationUtils.serializeDBUpdate(update, objectMapper,
                    javaType);
        }

        /**
         * Checks if the update is empty
         *
         * @return true if the update is empty
         */
        public boolean isEmpty() {
            return update.isEmpty();
        }

    }
}
