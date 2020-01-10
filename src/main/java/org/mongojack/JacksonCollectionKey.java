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

import java.util.Objects;

/**
 * A key for uniquely referencing a Jackson Collection, for use in HashMaps
 */
public class JacksonCollectionKey<CT> {
    private final String collectionName;
    private final String databaseName;
    private final Class<CT> valueType;

    public JacksonCollectionKey(final String databaseName, String collectionName, Class<CT> valueType) {
        assert collectionName != null;
        assert valueType != null;
        this.databaseName = databaseName;
        this.collectionName = collectionName;
        this.valueType = valueType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JacksonCollectionKey<?> that = (JacksonCollectionKey<?>) o;
        return Objects.equals(getCollectionName(), that.getCollectionName()) &&
            Objects.equals(getDatabaseName(), that.getDatabaseName()) &&
            Objects.equals(getValueType(), that.getValueType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCollectionName(), getDatabaseName(), getValueType());
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public Class<CT> getValueType() {
        return valueType;
    }

}
