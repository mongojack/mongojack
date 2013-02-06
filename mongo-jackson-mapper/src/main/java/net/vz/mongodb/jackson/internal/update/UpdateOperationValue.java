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
package net.vz.mongodb.jackson.internal.update;

/**
 * A DB update operation
 */
public interface UpdateOperationValue {
    /**
     * Whether the target field for these values is a collection.
     *
     * @return True if the target is a collection, false if otherwise
     */
    boolean isTargetCollection();

    /**
     * Whether the value requires serialization
     *
     * @return True if the value requires serialization, false if otherwise
     */
    boolean requiresSerialization();

    /**
     * Get the value
     *
     * @return The value
     */
    Object getValue();
}
