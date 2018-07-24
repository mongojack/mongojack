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
package org.mongojack.internal;

import com.mongodb.MongoException;
import org.mongojack.PrePersist;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will track and later invoke @PrePersist annotated methods on the entity classes.
 *
 * @author dnebinger
 */
public class PrePersistEntityMethodsInvoker<T> {

    /**
     * This class tracks the individual methods and will invoke them.
     */
    private static class PrePersistMethodInvoker<T> {
        private final Method method;
        private final int parms;
        
        /**
         * Constructor for the instance.
         * @param method Method to track.
         */
        public PrePersistMethodInvoker(final Method method) {
            this.method = method;

            // force method to be accessible so we can invoke it later.
            this.method.setAccessible(true);
            
            this.parms = this.method.getParameterTypes().length;
        }

        /**
         * invoke: Invokes the method on the entity.
         * @param entity Entity to invoke the method on.
         * @param update If <code>true</code>, signals prepersist is being called as part of an update.
         * @throws InvocationTargetException
         * @throws IllegalAccessException
         */
        public void invoke(final T entity, final boolean update) throws InvocationTargetException, IllegalAccessException {
            if (parms == 0) {
                method.invoke(entity);
            } else {
                method.invoke(entity, update);
            }
        }
    }

    /**
     * List used to track the methods to invoke on pre-persist.
     */
    private final List<PrePersistMethodInvoker<T>> prePersistMethodInvokers = new ArrayList<>();

    /**
     * Constructor for the instance.
     * @param clazz Class to invoke PrePersist methods on.
     */
    public PrePersistEntityMethodsInvoker(final Class<?> clazz) {
        addPrePersistMethods(clazz);
    }

    /**
     * addPrePersistMethods: Adds all methods in the entity class and super class(es) that are annotated
     * with the @PrePersist annotation.
     *
     * Methods are added in inverse order, so all superclass methods come before subclass and finally
     * entity class methods.  This should allow an @PrePersist method in the entity to make final change(s)
     * that the super class(es) can't step on.
     *
     * @param clazz Class to find and add methods from.
     */
    private void addPrePersistMethods(final Class<?> clazz) {
        if ((clazz == null) || (clazz.equals(Object.class))) {
            // done with recursion
            return;
        }

        // add superclass methods
        addPrePersistMethods(clazz.getSuperclass());

        // now handle entity methods
        for (Method method : clazz.getDeclaredMethods()) {
            // ignore static methods
            if (!Modifier.isStatic(method.getModifiers())) {
                if (method.isAnnotationPresent(PrePersist.class)) {
                    // this is a method to invoke
                    prePersistMethodInvokers.add(new PrePersistMethodInvoker<T>(method));
                }
            }
        }
    }

    /**
     * Invokes all of the prepersist methods.
     * @param entity Entity to invoke prepersist methods on.
     * @param update If <code>true</code>, signals prepersist is being called as part of an update.
     */
    public void prePersist(final T entity, final boolean update) {
        try {
            for (PrePersistMethodInvoker<T> prePersistEntityMethodsInvoker : prePersistMethodInvokers) {
                prePersistEntityMethodsInvoker.invoke(entity, update);
            }
        } catch (IllegalAccessException e) {
            throw new MongoException("Failed invoking PrePersist update method: " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new MongoException("Failed invoking PrePersist update method: " + e.getMessage(), e);
        }
    }
}
