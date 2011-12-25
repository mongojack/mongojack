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
package net.vz.mongodb.jackson.internal.util;

import com.fasterxml.jackson.core.JsonNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.*;

import java.util.HashMap;

/**
 * Temporarily here while this class gets added back to Jackson 2
 *
 * Simple implementation {@link Deserializers} which allows registration of
 * deserializers based on raw (type erased class).
 * It can work well for basic bean and scalar type deserializers, but is not
 * a good fit for handling generic types (like {@link Map}s and {@link Collection}s
 * or array types).
 *<p>
 * Unlike {@link SimpleSerializers}, this class does not currently support generic mappings;
 * all mappings must be to exact declared deserialization type.
 *
 * @since 1.7
 */
public class SimpleDeserializers implements Deserializers
{
    protected HashMap<ClassKey,JsonDeserializer<?>> _classMappings = null;

    /*
    /**********************************************************
    /* Life-cycle, construction and configuring
    /**********************************************************
     */

    public SimpleDeserializers() { }

    public <T> void addDeserializer(Class<T> forClass, JsonDeserializer<? extends T> deser)
    {
        ClassKey key = new ClassKey(forClass);
        if (_classMappings == null) {
            _classMappings = new HashMap<ClassKey,JsonDeserializer<?>>();
        }
        _classMappings.put(key, deser);
    }

    /*
    /**********************************************************
    /* Serializers implementation
    /**********************************************************
     */

    public JsonDeserializer<?> findArrayDeserializer(ArrayType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanProperty property,
            TypeDeserializer elementTypeDeserializer,
            JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(type.getRawClass()));
    }

    public JsonDeserializer<?> findBeanDeserializer(JavaType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc, BeanProperty property)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(type.getRawClass()));
    }

    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc, BeanProperty property,
            TypeDeserializer elementTypeDeserializer,
            JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(type.getRawClass()));
    }

    public JsonDeserializer<?> findCollectionLikeDeserializer(CollectionLikeType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc, BeanProperty property,
            TypeDeserializer elementTypeDeserializer,
            JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(type.getRawClass()));
    }

    public JsonDeserializer<?> findEnumDeserializer(Class<?> type,
            DeserializationConfig config, BeanDescription beanDesc, BeanProperty property)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(type));
    }

    public JsonDeserializer<?> findMapDeserializer(MapType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc, BeanProperty property,
            KeyDeserializer keyDeserializer,
            TypeDeserializer elementTypeDeserializer,
            JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(type.getRawClass()));
    }

    public JsonDeserializer<?> findMapLikeDeserializer(MapLikeType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc, BeanProperty property,
            KeyDeserializer keyDeserializer,
            TypeDeserializer elementTypeDeserializer,
            JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(type.getRawClass()));
    }

    public JsonDeserializer<?> findTreeNodeDeserializer(Class<? extends JsonNode> nodeType,
            DeserializationConfig config, BeanProperty property)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(nodeType));
    }
}