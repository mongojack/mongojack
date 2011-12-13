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

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.HandlerInstantiator;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;

/**
 * This works around JACKSON-732
 */
public class MongoJacksonHandlerInstantiator extends HandlerInstantiator {
    private final MongoAnnotationIntrospector introspector;

    public MongoJacksonHandlerInstantiator(MongoAnnotationIntrospector introspector) {
        this.introspector = introspector;
    }

    @Override
    public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<? extends JsonDeserializer<?>> deserClass) {
        if (deserClass.equals(DBRefDeserializer.class)) {
            JavaType type = config.getTypeFactory().constructType(annotated.getGenericType());
            JavaType dbRefType;
            if (type.isContainerType()) {
                if (type.isCollectionLikeType()) {
                    dbRefType = type.containedType(0);
                } else if (type.isMapLikeType()) {
                    dbRefType = type.containedType(1);
                } else {
                    return null;
                }
            } else {
                dbRefType = type;
            }
            Class<? extends JsonDeserializer> keyDeserializer = introspector.findObjectIdDeserializer(dbRefType.containedType(1));
            return new DBRefDeserializer(dbRefType.containedType(0), dbRefType.containedType(1), ClassUtil.createInstance(keyDeserializer, false));
        }
        return null;
    }

    @Override
    public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<? extends KeyDeserializer> keyDeserClass) {
        return null;
    }

    @Override
    public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<? extends JsonSerializer<?>> serClass) {
        return null;
    }

    @Override
    public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<? extends TypeResolverBuilder<?>> builderClass) {
        return null;
    }

    @Override
    public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<? extends TypeIdResolver> resolverClass) {
        return null;
    }
}
