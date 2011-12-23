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
package net.vz.mongodb.jackson.internal.stream;

import com.mongodb.BasicDBObject;

/**
 * A Jackson DBObject.  Extends BasicDBObject so that it can hold error code fields and similar.
 *
 * @author James Roper
 * @since 1.1.2
 */
public class JacksonDBObject<T> extends BasicDBObject {
    public JacksonDBObject() {
    }

    public JacksonDBObject(T object) {
        this.object = object;
    }

    private T object;

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    @Override
    public Object get(String key) {
        if ("_id".equals(key)) {
            return "Generated/saved ID retrieval does not work when streaming serialization is enabled";
        }
        return super.get(key);
    }
}
