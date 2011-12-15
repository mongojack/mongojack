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
package net.vz.mongodb.jackson.internal;

import org.bson.types.ObjectId;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.type.*;
import org.codehaus.jackson.type.JavaType;

/**
 * Serializer for MongoDB
 */
public class MongoJacksonSerializers implements Serializers {
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc, BeanProperty property) {
        if (type.getRawClass() == ObjectId.class) {
            return new ObjectIdSerializer();
        }
        return null;
    }

    public JsonSerializer<?> findArraySerializer(SerializationConfig config, ArrayType type, BeanDescription beanDesc, BeanProperty property, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
        return null;
    }

    public JsonSerializer<?> findCollectionSerializer(SerializationConfig config, CollectionType type, BeanDescription beanDesc, BeanProperty property, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
        return null;
    }

    public JsonSerializer<?> findCollectionLikeSerializer(SerializationConfig config, CollectionLikeType type, BeanDescription beanDesc, BeanProperty property, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
        return null;
    }

    public JsonSerializer<?> findMapSerializer(SerializationConfig config, MapType type, BeanDescription beanDesc, BeanProperty property, JsonSerializer<Object> keySerializer, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
        return null;
    }

    public JsonSerializer<?> findMapLikeSerializer(SerializationConfig config, MapLikeType type, BeanDescription beanDesc, BeanProperty property, JsonSerializer<Object> keySerializer, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
        return null;
    }
}
