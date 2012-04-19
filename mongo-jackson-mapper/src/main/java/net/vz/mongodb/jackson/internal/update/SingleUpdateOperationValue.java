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
 * A single update operation value
 */
public class SingleUpdateOperationValue implements UpdateOperationValue {
    private final boolean targetCollection;
    private final boolean requiresSerialization;
    private final Object value;

    public SingleUpdateOperationValue(boolean targetCollection, boolean requiresSerialization, Object value) {
        this.targetCollection = targetCollection;
        this.requiresSerialization = requiresSerialization;
        this.value = value;
    }

    public boolean requiresSerialization() {
        return requiresSerialization;
    }

    public boolean isTargetCollection() {
        return targetCollection;
    }

    public Object getValue() {
        return value;
    }
}
