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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.mongojack.MongoJackModuleConfiguration;
import org.mongojack.MongoJackModuleFeature;

/**
 * The ObjectID serialising module
 *
 * @author James Roper
 * @since 1.0
 */
public class MongoJackModule extends Module {

    public static final MongoJackModuleConfiguration DEFAULT_CONFIGURATION = new MongoJackModuleConfiguration();

    public static final Module DEFAULT_MODULE_INSTANCE = new MongoJackModule();

    public static final Module DEFAULT_JAVA_TIME_MODULE = new JavaTimeModule();

    private final MongoJackModuleConfiguration moduleConfiguration;

    public MongoJackModule() {
        moduleConfiguration = DEFAULT_CONFIGURATION;
    }

    public MongoJackModule(final MongoJackModuleConfiguration moduleConfiguration) {
        this.moduleConfiguration = moduleConfiguration;
    }

    /**
     * Configure the given object mapper to be used with MongoJack. Please call
     * this method rather than calling
     * objectMapper.with(MongoJacksonMapperModule.INSTANCE), because Jacksons
     * module system doesn't allow MongoJack to do all the configuration it
     * needs to do. This method will do that configuration though.
     *
     * @param objectMapper The object mapper to configure
     * @return This object mapper (for chaining)
     */
    public static ObjectMapper configure(ObjectMapper objectMapper) {
        return configure(objectMapper, DEFAULT_CONFIGURATION);
    }

    /**
     * Configure the given object mapper to be used with MongoJack. Please call
     * this method rather than calling
     * objectMapper.with(MongoJacksonMapperModule.INSTANCE), because Jacksons
     * module system doesn't allow MongoJack to do all the configuration it
     * needs to do. This method will do that configuration though.
     *
     * @param objectMapper The object mapper to configure
     * @param moduleConfiguration The configuration of the module
     * @return This object mapper (for chaining)
     */
    public static ObjectMapper configure(ObjectMapper objectMapper, MongoJackModuleConfiguration moduleConfiguration) {
        // register java time module
        if (moduleConfiguration.isEnabled(MongoJackModuleFeature.REGISTER_JAVA_TIME)) {
            objectMapper.registerModule(DEFAULT_JAVA_TIME_MODULE);
        }

        if (moduleConfiguration == DEFAULT_CONFIGURATION) {
            objectMapper.registerModule(DEFAULT_MODULE_INSTANCE);
        } else {
            objectMapper.registerModule(new MongoJackModule(moduleConfiguration));
        }

        // disable serialize dates as timestamps because we have fewer runtime errors that way
        if (moduleConfiguration.isEnabled(MongoJackModuleFeature.DISABLE_DATES_AS_TIMESTAMPS)) {
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        }

        if (moduleConfiguration.isEnabled(MongoJackModuleFeature.SET_SERIALIZATION_INCLUSION_NON_NULL)) {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        return objectMapper;
    }

    @Override
    public String getModuleName() {
        return "Object ID Module";
    }

    @Override
    public Version version() {
        return new Version(1, 0, 0, null, "org.mongojack", "mongojack");
    }

    @Override
    public void setupModule(SetupContext context) {
        MongoAnnotationIntrospector annotationIntrospector = new MongoAnnotationIntrospector(context.getTypeFactory());
        context.insertAnnotationIntrospector(annotationIntrospector);
        // Only include non null properties, this makes it possible to use
        // object templates for querying and
        // partial object retrieving
        context.addSerializers(new MongoJackSerializers(moduleConfiguration));
        context.addDeserializers(new MongoJackDeserializers());
    }

}
