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

import com.mongodb.DBCollection;
import com.mongodb.DBEncoder;
import com.mongodb.DBEncoderFactory;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Encoder factory for Jackson encoders
 */
public class JacksonEncoderFactory implements DBEncoderFactory{

    private final DBEncoderFactory defaultEncoderFactory;
    private final ObjectMapper objectMapper;

    public JacksonEncoderFactory(DBEncoderFactory defaultEncoderFactory, ObjectMapper objectMapper) {
        this.defaultEncoderFactory = defaultEncoderFactory;
        this.objectMapper = objectMapper;
    }

    public DBEncoder create() {
        return new JacksonDBEncoder(defaultEncoderFactory.create(), objectMapper);
    }
}
