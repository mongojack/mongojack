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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A multi update operation value
 */
public class MultiUpdateOperationValue implements UpdateOperationValue {
    private final boolean targetCollection;
    private final boolean requiresSerialization;
    private final List<Object> values;

    public MultiUpdateOperationValue(boolean targetCollection, boolean requiresSerialization, List<?> values) {
        this.targetCollection = targetCollection;
        this.requiresSerialization = requiresSerialization;
        this.values = (List) values;
    }

    public MultiUpdateOperationValue(boolean targetCollection, boolean requiresSerialization, Object... values) {
        this.targetCollection = targetCollection;
        this.requiresSerialization = requiresSerialization;
        this.values = new ArrayList<Object>();
        this.values.addAll(Arrays.asList(values));
    }

    public boolean isTargetCollection() {
        return targetCollection;
    }

    public boolean requiresSerialization() {
        return requiresSerialization;
    }

    public Object getValue() {
        return values;
    }

    public Collection<?> getValues() {
        return values;
    }

    public void addValues(List<?> values) {
        this.values.addAll(values);
    }
}
