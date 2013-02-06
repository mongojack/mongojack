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
package org.mongojack.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.mongojack.internal.object.BsonObjectGenerator;
import org.mongojack.internal.stream.DBEncoderBsonGenerator;

import java.io.IOException;

/**
 * Serializer that detects whether we are using a BSON serializer or Object serializer
 */
public abstract class MongoSerializer<T> extends JsonSerializer<T> {
    @Override
    public final void serialize(T value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        if (jgen instanceof DBEncoderBsonGenerator) {
            serialize(value, (DBEncoderBsonGenerator) jgen, provider);
        } else if (jgen instanceof BsonObjectGenerator) {
            serialize(value, (BsonObjectGenerator) jgen, provider);
        } else {
            throw new IllegalArgumentException("Mongo serializer may only be used for generating BSON");
        }
    }

    protected abstract void serialize(T value, DBEncoderBsonGenerator bgen, SerializerProvider provider) throws IOException, JsonProcessingException;

    protected abstract void serialize(T value, BsonObjectGenerator bgen, SerializerProvider provider) throws IOException, JsonProcessingException;
}
