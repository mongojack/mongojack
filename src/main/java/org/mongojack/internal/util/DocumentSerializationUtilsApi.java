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
package org.mongojack.internal.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.BsonWriter;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.mongojack.UpdateOperationValue;

import java.util.Map;

/**
 * Utilities for helping with serialisation
 */
interface DocumentSerializationUtilsApi {

    /**
     * Serialize the fields of the given object using the given object mapper.
     * This will convert POJOs to Documents where necessary.
     *
     * @param object   The object to serialize the fields of
     * @param registry Codec registry
     * @return The Document, safe for serialization to MongoDB
     */
    Bson serializeFields(
        Bson object,
        CodecRegistry registry
    );

    boolean writeKnownType(
        Object value,
        BsonWriter writer
    );

    boolean isKnownType(
        Object value
    );

    boolean isKnownClass(
        Class<?> value
    );

    Bson serializeFilter(
        ObjectMapper objectMapper,
        JavaType type,
        Bson query,
        CodecRegistry registry
    );

    Bson serializeUpdates(
        Map<String, Map<String, UpdateOperationValue>> update,
        ObjectMapper objectMapper,
        JavaType javaType,
        CodecRegistry registry
    );
}
