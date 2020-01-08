package org.mongojack.internal.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mongojack.JacksonCodecRegistry;

public interface InitializationRequiredForTransformation {

    void initialize(
        ObjectMapper objectMapper,
        JavaType type,
        JacksonCodecRegistry codecRegistry
    );

}
