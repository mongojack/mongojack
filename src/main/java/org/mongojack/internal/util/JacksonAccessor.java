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
package org.mongojack.internal.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Accesses things in Jackson that usually aren't accessible.  Here be dragons.
 */
public class JacksonAccessor {

    public static JsonDeserializer findDeserializer(ObjectMapper objectMapper, JavaType type) {
        return invoke(objectMapper, objectMapperFindRootDeserializer, JsonDeserializer.class,
                createDeserializationContext(objectMapper), type);
    }

    public static DeserializationContext createDeserializationContext(ObjectMapper objectMapper) {
        return invoke(objectMapper, objectMapperCreateDeserializationContext, DeserializationContext.class,
                null, objectMapper.getDeserializationConfig());
    }

    public static BeanPropertyWriter findPropertyWriter(BeanSerializerBase serializer, String propertyName) {
        BeanPropertyWriter[] props = get(serializer, beanSerializerBaseProps, BeanPropertyWriter[].class);
        for (BeanPropertyWriter prop : props) {
            if (propertyName.equals(prop.getName())) {
                return prop;
            }
        }
        return null;
    }

    public static SerializerFactory getSerializerFactory(ObjectMapper objectMapper) {
        return get(objectMapper, objectMapperSerializerFactory, SerializerFactory.class);
    }

    public static SerializerProvider getSerializerProvider(ObjectMapper objectMapper) {
        DefaultSerializerProvider serializerProvider = (DefaultSerializerProvider) objectMapper.getSerializerProvider();
        return serializerProvider.createInstance(objectMapper.getSerializationConfig(), getSerializerFactory(objectMapper));
    }

    public static JsonSerializer findValueSerializer(ObjectMapper objectMapper, SerializerProvider serializerProvider, JavaType javaType) {
        try {
            return serializerProvider.findValueSerializer(javaType, null);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Method objectMapperCreateDeserializationContext;
    private static final Method objectMapperFindRootDeserializer;
    private static final Field beanSerializerBaseProps;
    private static final Field objectMapperSerializerFactory;

    static {
        objectMapperCreateDeserializationContext = findMethod(ObjectMapper.class, "createDeserializationContext",
                new Class[] {JsonParser.class, DeserializationConfig.class});
        objectMapperFindRootDeserializer = findMethod(ObjectMapper.class, "_findRootDeserializer",
                new Class[] {DeserializationContext.class, JavaType.class});
        beanSerializerBaseProps = findField(BeanSerializerBase.class, "_props");
        objectMapperSerializerFactory = findField(ObjectMapper.class, "_serializerFactory");
    }

    private static Method findMethod(Class clazz, String name, Class[] argTypes) {
        try {
            Method method = clazz.getDeclaredMethod(name, argTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field findField(Class clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T get(Object object, Field field, Class<T> type) {
        try {
            return (T) field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T invoke(Object object, Method method, Class<T> returnType, Object... args) {
        try {
            return (T) method.invoke(object, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
