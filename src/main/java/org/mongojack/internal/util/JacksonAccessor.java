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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import java.io.IOException;
import java.util.Set;

/**
 * Accesses things in Jackson that usually aren't accessible. Here be dragons.
 */
public class JacksonAccessor {

    private static class LocalBeanSerializer extends BeanSerializerBase {

        protected LocalBeanSerializer(final BeanSerializerBase src) {
            super(src);
        }

        @Override
        public BeanSerializerBase withObjectIdWriter(final ObjectIdWriter objectIdWriter) {
            throw new IllegalStateException("LocalBeanSerializer should never escape confinement");
        }

        @Override
        protected BeanSerializerBase withIgnorals(final Set<String> toIgnore) {
            throw new IllegalStateException("LocalBeanSerializer should never escape confinement");
        }

        @Override
        protected BeanSerializerBase asArraySerializer() {
            throw new IllegalStateException("LocalBeanSerializer should never escape confinement");
        }

        @Override
        public BeanSerializerBase withFilterId(final Object filterId) {
            throw new IllegalStateException("LocalBeanSerializer should never escape confinement");
        }

        @Override
        public void serialize(final Object bean, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
            throw new IllegalStateException("LocalBeanSerializer should never escape confinement");
        }

        protected BeanPropertyWriter[] getProps() {
            return _props;
        }

    }

    public static JsonSerializer<?> findJsonSerializer(
        SerializerProvider serializerProvider,
        BeanSerializerBase serializer,
        String propertyName
    ) {
        BeanPropertyWriter writer = findPropertyWriterByName(propertyName, new LocalBeanSerializer(serializer).getProps());
        JsonSerializer<?> foundSerializer = null;
        if (writer != null) {
            foundSerializer = writer.getSerializer();
            if (foundSerializer == null) {
                foundSerializer = findValueSerializer(serializerProvider, writer.getType());
            }
        }
        return foundSerializer;
    }

    private static BeanPropertyWriter findPropertyWriterByName(final String propertyName, final BeanPropertyWriter[] props) {
        for (BeanPropertyWriter prop : props) {
            if (propertyName.equals(prop.getName())) {
                return prop;
            }
        }
        return null;
    }

    public static SerializerProvider getSerializerProvider(
        ObjectMapper objectMapper
    ) {
        DefaultSerializerProvider serializerProvider = (DefaultSerializerProvider) objectMapper
            .getSerializerProvider();
        return serializerProvider.createInstance(
            objectMapper.getSerializationConfig(),
            objectMapper.getSerializerFactory()
        );
    }

    public static JsonSerializer findValueSerializer(
        SerializerProvider serializerProvider, JavaType javaType
    ) {
        try {
            return serializerProvider.findValueSerializer(javaType, null);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonSerializer findValueSerializer(
        SerializerProvider serializerProvider, Class clazz
    ) {
        try {
            return serializerProvider.findValueSerializer(clazz, null);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
    }

}
