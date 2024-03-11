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
public class DocumentSerializationUtils {

    private static volatile DocumentSerializationUtilsApi instance = new DocumentSerializationUtilsImpl();

    /**
     * Serialize the fields of the given object using the given object mapper.
     * This will convert POJOs to Documents where necessary.
     *
     * @param object   The object to serialize the fields of
     * @param registry Codec registry
     * @return The Document, safe for serialization to MongoDB
     */
    public static Bson serializeFields(
        Bson object,
        CodecRegistry registry
    ) {
        return instance.serializeFields(object, registry);
    }

    public static boolean writeKnownType(
        Object value,
        BsonWriter writer
    ) {
        return instance.writeKnownType(value, writer);
    }

    @SuppressWarnings({"unused"})
    public static boolean isKnownType(
        Object value
    ) {
        return instance.isKnownType(value);
    }

    public static boolean isKnownClass(
        Class<?> value
    ) {
        return instance.isKnownClass(value);
    }

    public static Bson serializeFilter(
        ObjectMapper objectMapper,
        JavaType type,
        Bson query,
        CodecRegistry registry
    ) {
        return instance.serializeFilter(objectMapper, type, query, registry);
    }

    public static Bson serializeUpdates(
        Map<String, Map<String, UpdateOperationValue>> update,
        ObjectMapper objectMapper,
        JavaType javaType,
        CodecRegistry registry
    ) {
        return instance.serializeUpdates(update, objectMapper, javaType, registry);
    }

    @SuppressWarnings("unused")
    public static DocumentSerializationUtilsApi getInstance() {
        return instance;
    }

    @SuppressWarnings("unused")
    public static void setInstance(final DocumentSerializationUtilsApi instance) {
        DocumentSerializationUtils.instance = instance;
    }

}
