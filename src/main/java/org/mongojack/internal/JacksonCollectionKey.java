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
package org.mongojack.internal;

import com.fasterxml.jackson.databind.JavaType;

import java.util.Objects;

/**
 * A key for uniquely referencing a Jackson Collection, for use in HashMaps
 */
public class JacksonCollectionKey {
    private final String name;
    private final String dbName;
    private final JavaType type;

    public JacksonCollectionKey(String name, final String dbName, JavaType type) {
        this.name = name;
        this.type = type;
        this.dbName = dbName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JacksonCollectionKey that = (JacksonCollectionKey) o;
        return Objects.equals(getName(), that.getName()) &&
            Objects.equals(getDbName(), that.getDbName()) &&
            Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDbName(), getType());
    }

    public String getName() {
        return name;
    }

    public JavaType getType() {
        return type;
    }

    public String getDbName() {
        return dbName;
    }
}
