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

import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.ObjectId;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.introspect.*;
import org.codehaus.jackson.map.type.TypeBindings;
import org.codehaus.jackson.type.JavaType;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Annotation introspector that supports @ObjectId's
 *
 * @author James Roper
 * @since 1.0
 */
public class MongoAnnotationIntrospector extends NopAnnotationIntrospector {
    private final DeserializationConfig deserializationConfig;

    public MongoAnnotationIntrospector(DeserializationConfig deserializationConfig) {
        this.deserializationConfig = deserializationConfig;
    }

    @Override
    public boolean isHandled(Annotation ann) {
        return ann.annotationType() == ObjectId.class
                || ann.annotationType() == Id.class
                || ann.annotationType() == javax.persistence.Id.class;
    }

    // Handling of javax.persistence.Id
    @Override
    public String findGettablePropertyName(AnnotatedMethod am) {
        return findPropertyName(am);
    }

    @Override
    public String findSettablePropertyName(AnnotatedMethod am) {
        return findPropertyName(am);
    }

    @Override
    public String findDeserializablePropertyName(AnnotatedField af) {
        return findPropertyName(af);
    }

    @Override
    public String findSerializablePropertyName(AnnotatedField af) {
        return findPropertyName(af);
    }

    @Override
    public String findPropertyNameForParam(AnnotatedParameter param) {
        return findPropertyName(param);
    }

    private String findPropertyName(Annotated annotated) {

        if (annotated.hasAnnotation(Id.class) || annotated.hasAnnotation(javax.persistence.Id.class)) {
            return "_id";
        }
        return null;
    }


    // Handling of ObjectId annotated properties
    @Override
    public Object findSerializer(Annotated am) {
        if (am.hasAnnotation(ObjectId.class)) {
            return ObjectIdSerializer.class;
        }
        return null;
    }

    @Override
    public Object findDeserializer(Annotated am) {
        if (am.hasAnnotation(ObjectId.class)) {
            return findObjectIdDeserializer(deserializationConfig.getTypeFactory().constructType(am.getGenericType()));
        }
        return null;
    }

    @Override
    public Class findContentDeserializer(Annotated am) {
        if (am.hasAnnotation(ObjectId.class)) {
            JavaType type = deserializationConfig.getTypeFactory().constructType(am.getGenericType());
            if (type.isCollectionLikeType()) {
                return (Class) findObjectIdDeserializer(type.containedType(0));
            } else if (type.isMapLikeType()) {
                return (Class) findObjectIdDeserializer(type.containedType(1));
            }
        }
        return null;
    }

    private Object findObjectIdDeserializer(JavaType type) {
        if (type.getRawClass() == String.class) {
            return ObjectIdDeserializers.ToStringDeserializer.class;
        } else if (type.getRawClass() == byte[].class) {
            return ObjectIdDeserializers.ToByteArrayDeserializer.class;
        } else if (type.getRawClass() == DBRef.class) {
            Class<? extends JsonDeserializer> keyTypeDeserializer = (Class) findObjectIdDeserializer(type.containedType(1));
            if (keyTypeDeserializer != null) {
                JsonDeserializer keyDeserializer;
                try {
                    keyDeserializer = (JsonDeserializer) keyTypeDeserializer.newInstance();
                } catch (InstantiationException e) {
                    // We know this won't fail
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    // We know this won't fail
                    throw new RuntimeException(e);
                }
                return new DBRefDeserializer(type.containedType(0), type.containedType(1), keyDeserializer);
            }
        } else if (type.getRawClass() == org.bson.types.ObjectId.class) {
            // Don't know why someone would annotated an ObjectId with @ObjectId, but handle it
            return ObjectIdDeserializers.ToObjectIdDeserializer.class;
        }
        return null;
    }

}
