package org.mongojack.internal.util;

import java.io.IOException;

@FunctionalInterface
public interface MappingConsumer<T> {

    void accept(T t) throws IOException;

}
