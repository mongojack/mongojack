package org.mongojack.internal.util;

import tools.jackson.core.JacksonException;

@FunctionalInterface
public interface MappingConsumer<T> {

    void accept(T t) throws JacksonException;

}
