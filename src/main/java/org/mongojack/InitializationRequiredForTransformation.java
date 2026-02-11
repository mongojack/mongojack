package org.mongojack;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import org.mongojack.JacksonCodecRegistry;

public interface InitializationRequiredForTransformation {

    void initialize(
        ObjectMapper objectMapper,
        JavaType type,
        JacksonCodecRegistry codecRegistry
    );

}
