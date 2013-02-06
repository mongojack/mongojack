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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBEncoder;
import com.mongodb.DBEncoderFactory;
import com.mongodb.DefaultDBEncoder;
import net.vz.mongodb.jackson.JacksonDBCollection;

/**
 * Encoder factory for Jackson encoders
 */
public class JacksonEncoderFactory implements DBEncoderFactory{

    private final ObjectMapper objectMapper;
    private final JacksonDBCollection<?, ?> collection;

    public JacksonEncoderFactory(ObjectMapper objectMapper, JacksonDBCollection<?, ?> collection) {
        this.objectMapper = objectMapper;
        this.collection = collection;
    }

    public DBEncoder create() {
        if (collection.isEnabled(JacksonDBCollection.Feature.USE_STREAM_SERIALIZATION)) {
           return new JacksonDBEncoder(objectMapper, DefaultDBEncoder.FACTORY.create());
        } else {
            return DefaultDBEncoder.FACTORY.create();
        }
    }
}
