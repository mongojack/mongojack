/*
 * Copyright 2021 Stéphane Démurget
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
package org.mongojack.internal;

import org.mongojack.Id;

import com.fasterxml.jackson.databind.introspect.Annotated;

/**
 * Helper to deal with annotations.
 *
 * @author Stéphane Démurget
 */
public class AnnotationHelper {

    private static final Class<?> JAVAX_PERSIST_ID_CLASS = initPersistIdClass("javax.persistence.Id");
    private static final Class<?> JAKARTA_PERSIST_ID_CLASS = initPersistIdClass("jakarta.persistence.Id");
    private static final Class<?> BSON_PERSIST_ID_CLASS = initPersistIdClass("org.bson.codecs.pojo.annotations.BsonId");

    private AnnotationHelper() {
        super();
    }

    public static boolean hasIdAnnotation(Annotated annotated) {
        return annotated.hasAnnotation(Id.class) ||
            (JAVAX_PERSIST_ID_CLASS != null && annotated.hasAnnotation(JAVAX_PERSIST_ID_CLASS)) ||
            (JAKARTA_PERSIST_ID_CLASS != null && annotated.hasAnnotation(JAKARTA_PERSIST_ID_CLASS))||
            (BSON_PERSIST_ID_CLASS != null && annotated.hasAnnotation(BSON_PERSIST_ID_CLASS));
    }

    private static Class<?> initPersistIdClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null; // javax or jakarta persist @Id will not be supported
        }
    }
}
