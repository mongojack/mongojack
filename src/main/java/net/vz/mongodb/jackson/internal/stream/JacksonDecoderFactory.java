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

import com.mongodb.DBDecoder;
import com.mongodb.DBDecoderFactory;
import net.vz.mongodb.jackson.JacksonDBCollection;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

/**
 * DBDecoder factory for jackson
 *
 * @author James Roper
 * @since 1.1.2
 */
public class JacksonDecoderFactory<T> implements DBDecoderFactory {
    private final JacksonDBCollection<T, ?> dbCollection;
    private final ObjectMapper objectMapper;
    private final JavaType type;

    public JacksonDecoderFactory(JacksonDBCollection<T, ?> dbCollection, ObjectMapper objectMapper, JavaType type) {
        this.dbCollection = dbCollection;
        this.objectMapper = objectMapper;
        this.type = type;
    }

    public DBDecoder create() {
        return new JacksonDBDecoder(dbCollection, objectMapper, type);
    }
}
