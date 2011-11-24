package org.mongodb.jackson.internal;

import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.NopAnnotationIntrospector;
import org.mongodb.jackson.ObjectId;

import java.lang.annotation.Annotation;

/**
 * Annotation introspector that supports @ObjectId's
 */
public class ObjectIdAnnotationIntrospector extends NopAnnotationIntrospector {
    @Override
    public boolean isHandled(Annotation ann) {
        return ann.annotationType() == ObjectId.class;
    }

    @Override
    public Object findSerializer(Annotated am, BeanProperty property) {
        if (am.getAnnotation(ObjectId.class) != null) {
            return ObjectIdSerializer.class;
        }
        return null;
    }

    @Override
    public Object findDeserializer(Annotated am, BeanProperty property) {
        if (am.getAnnotation(ObjectId.class) != null) {
            if (am.getRawType() == String.class) {
                return ObjectIdStringDeserializer.class;
            } else if (am.getRawType() == byte[].class) {
                return ObjectIdByteDeserializer.class;
            }
        }
        return null;
    }

}
