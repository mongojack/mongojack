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

import java.util.Set;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.bean.BeanSerializerBase;
import tools.jackson.databind.ser.impl.ObjectIdWriter;
import tools.jackson.databind.util.NameTransformer;

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

        protected BeanSerializerBase withByNameInclusion(Set<String> toIgnore, Set<String> toInclude) {
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

        protected BeanSerializerBase withProperties(BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
            throw new IllegalStateException("LocalBeanSerializer should never escape confinement");
        }

        @Override
        public void serialize(final Object bean, final JsonGenerator gen, final SerializationContext provider) throws JacksonException {
            throw new IllegalStateException("LocalBeanSerializer should never escape confinement");
        }

        @Override
        public ValueSerializer<Object> unwrappingSerializer(NameTransformer unwrapper) {
            throw new IllegalStateException("LocalBeanSerializer should never escape confinement");
        }

        protected BeanPropertyWriter[] getProps() {
            return _props;
        }

    }

    public static ValueSerializer<?> findValueSerializer(
            SerializationContext serializerProvider,
            BeanSerializerBase serializer,
            String propertyName) {
        BeanPropertyWriter writer = findPropertyWriterByName(propertyName, new LocalBeanSerializer(serializer).getProps());
        ValueSerializer<?> foundSerializer = null;
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

    public static ValueSerializer findValueSerializer(
            SerializationContext serializerProvider, JavaType javaType) {
        try {
            return serializerProvider.findValueSerializer(javaType);
        } catch (DatabindException e) {
            throw new RuntimeException(e);
        }
    }

    public static ValueSerializer findValueSerializer(
            SerializationContext serializerProvider, Class clazz) {
        try {
            return serializerProvider.findValueSerializer(clazz);
        } catch (DatabindException e) {
            throw new RuntimeException(e);
        }
    }

}
