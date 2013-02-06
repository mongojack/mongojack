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

import com.mongodb.DBObject;
import com.mongodb.ReadPreference;

import java.util.Map;

/**
 * Map reduce command builder
 */
public class MapReduce {

    public static enum OutputType {
        /**
         * Save the job output to a collection, replacing its previous content
         */
        REPLACE(com.mongodb.MapReduceCommand.OutputType.REPLACE),

        /**
         * Merge the job output with the existing contents of outputTarget collection
         */
        MERGE(com.mongodb.MapReduceCommand.OutputType.MERGE),

        /**
         * Reduce the job output with the existing contents of outputTarget collection
         */
        REDUCE(com.mongodb.MapReduceCommand.OutputType.REDUCE),

        /**
         * Return results inline, no result is written to the DB server
         */
        INLINE(com.mongodb.MapReduceCommand.OutputType.INLINE);

        private final com.mongodb.MapReduceCommand.OutputType driverType;

        OutputType(com.mongodb.MapReduceCommand.OutputType driverType) {
            this.driverType = driverType;
        }

        com.mongodb.MapReduceCommand.OutputType getDriverType() {
            return driverType;
        }
    }


    /**
     * Build a map reduce command
     *
     * @param map        The map function
     * @param reduce     The reduce function
     * @param outputType The outputType
     * @param collection The collection name, may be null if output type is INLINE
     * @param resultType The type to deserialise the result to
     * @param keyType    The type of the keys that are being reduced on
     * @return The command
     */
    public static <T, K> MapReduceCommand<T, K> build(String map, String reduce, OutputType outputType, String collection,
                                                      Class<T> resultType, Class<K> keyType) {
        return new MapReduceCommand<T, K>(map, reduce, outputType, collection, resultType, keyType);
    }

    public static class MapReduceCommand<T, K> {
        private final String map;
        private final String reduce;
        private final OutputType outputType;
        private final String collection;
        private final Class<T> resultType;
        private final Class<K> keyType;

        private ReadPreference readPreference;
        private String outputDB;
        private DBObject query;
        private String finalize;
        private DBObject sort;
        private int limit;
        private Map<String, Object> scope;
        private boolean verbose = true;
        private DBObject extra;

        private MapReduceCommand(String map, String reduce, OutputType outputType, String collection, Class<T> resultType, Class<K> keyType) {
            this.map = map;
            this.reduce = reduce;
            this.outputType = outputType;
            this.collection = collection;
            this.resultType = resultType;
            this.keyType = keyType;
        }

        /**
         * Set the read preference for reading the results
         *
         * @param readPreference The read preference
         * @return this command
         */
        public MapReduceCommand<T, K> setReadPreference(ReadPreference readPreference) {
            this.readPreference = readPreference;
            return this;
        }

        /**
         * Set the db to output to if it's not this db
         *
         * @param outputDB the db to output to
         * @return this command
         */
        public MapReduceCommand<T, K> setOutputDB(String outputDB) {
            this.outputDB = outputDB;
            return this;
        }

        /**
         * Set the query to limit the items that are mapped
         *
         * @param query The query
         * @return this command
         */
        public MapReduceCommand<T, K> setQuery(DBObject query) {
            this.query = query;
            return this;
        }

        /**
         * Set the finalize function
         *
         * @param finalize The finalize function
         * @return this command
         */
        public MapReduceCommand<T, K> setFinalize(String finalize) {
            this.finalize = finalize;
            return this;
        }

        /**
         * Sort the input objects by this key
         *
         * @param sort The sort
         * @return this command
         */
        public MapReduceCommand<T, K> setSort(DBObject sort) {
            this.sort = sort;
            return this;
        }

        /**
         * Set the limit for the result collection to return
         *
         * @param limit The limit
         * @return this command
         */
        public MapReduceCommand<T, K> setLimit(int limit) {
            this.limit = limit;
            return this;
        }

        /**
         * Set the scope in which the javascript code for map, reduce and finalise is executed
         *
         * @param scope The scope
         * @return this command
         */
        public MapReduceCommand<T, K> setScope(Map<String, Object> scope) {
            this.scope = scope;
            return this;
        }

        /**
         * Set whether statistics on job execution time should be provided
         *
         * @param verbose True if stats should be provided
         * @return this command
         */
        public MapReduceCommand<T, K> setVerbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        /**
         * Set extra arguments to the map reduce command
         *
         * @param extra The extra arguments
         * @return this command
         */
        public MapReduceCommand<T, K> setExtra(DBObject extra) {
            this.extra = extra;
            return this;
        }

        com.mongodb.MapReduceCommand build(JacksonDBCollection<?, ?> collection) {
            DBObject query = null;
            if (this.query != null) {
                query = collection.serializeFields(this.query);
            }
            com.mongodb.MapReduceCommand command = new com.mongodb.MapReduceCommand(collection.getDbCollection(), map,
                    reduce, this.collection, outputType.getDriverType(), query);
            if (finalize != null) {
                command.setFinalize(finalize);
            }
            if (readPreference != null) {
                command.setReadPreference(readPreference);
            }
            if (outputDB != null) {
                command.setOutputDB(outputDB);
            }
            if (sort != null) {
                command.setSort(sort);
            }
            command.setLimit(limit);
            if (scope != null) {
                command.setScope(scope);
            }
            command.setVerbose(verbose);
            if (extra != null) {
                for (String key : extra.keySet()) {
                    command.addExtraOption(key, extra.get(key));
                }
            }
            return command;
        }

        Class<T> getResultType() {
            return resultType;
        }

        Class<K> getKeyType() {
            return keyType;
        }
    }
}
