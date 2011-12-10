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

import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.ObjectId;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.introspect.*;

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
    public Object findSerializer(Annotated am, BeanProperty property) {
        if (am.hasAnnotation(ObjectId.class)) {
            return ObjectIdSerializer.class;
        }
        return null;
    }

    @Override
    public Object findDeserializer(Annotated am, BeanProperty property) {
        if (am.hasAnnotation(ObjectId.class)) {
            return findObjectIdDeserializer(am.getRawType());
        }
        return null;
    }

    @Override
    public Class findContentDeserializer(Annotated am) {
        if (am.hasAnnotation(ObjectId.class)) {
            if (am.getGenericType() instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) am.getGenericType()).getActualTypeArguments();
                if (types.length > 0) {
                    if (types[0] instanceof Class) {
                        return findObjectIdDeserializer((Class) types[0]);
                    } else if (types[0] instanceof GenericArrayType) {
                        GenericArrayType arrayType = (GenericArrayType) types[0];
                        if (arrayType.getGenericComponentType() == byte.class) {
                            return ObjectIdDeserializers.ToByteArrayDeserializer.class;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Class<? extends JsonDeserializer<?>> findObjectIdDeserializer(Class<?> type) {
        if (type == String.class) {
            return ObjectIdDeserializers.ToStringDeserializer.class;
        } else if (type == byte[].class) {
            return ObjectIdDeserializers.ToByteArrayDeserializer.class;
        } else if (type == org.bson.types.ObjectId.class) {
            // Don't know why someone would annotated an ObjectId with @ObjectId, but handle it
            return ObjectIdDeserializers.ToObjectIdDeserializer.class;
        }
        return null;
    }

}
