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
package net.vz.mongodb.jackson.internal;

import org.codehaus.jackson.type.JavaType;

/**
 * A key for uniquely referencing a Jackson Collection, for use in HashMaps
 */
public class JacksonCollectionKey {
    private final String name;
    private final JavaType type;
    private final JavaType keyType;

    public JacksonCollectionKey(String name, JavaType type, JavaType keyType) {
        this.name = name;
        this.type = type;
        this.keyType = keyType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JacksonCollectionKey that = (JacksonCollectionKey) o;

        if (keyType != null ? !keyType.equals(that.keyType) : that.keyType != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (keyType != null ? keyType.hashCode() : 0);
        return result;
    }

    public String getName() {
        return name;
    }

    public JavaType getType() {
        return type;
    }

    public JavaType getKeyType() {
        return keyType;
    }
}
