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

import org.mongojack.MongoJackModuleConfiguration;
import org.mongojack.MongoJackModuleFeature;

import com.fasterxml.jackson.annotation.JsonInclude;

import tools.jackson.core.Version;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.introspect.DefaultAccessorNamingStrategy;

/**
 * The ObjectID serialising module
 *
 * @author James Roper
 * @since 1.0
 */
public class MongoJackModule extends JacksonModule {

    public static final MongoJackModuleConfiguration DEFAULT_CONFIGURATION = new MongoJackModuleConfiguration();

    public static final JacksonModule DEFAULT_MODULE_INSTANCE = new MongoJackModule();

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
        var builder = objectMapper.rebuild();
        if (moduleConfiguration == DEFAULT_CONFIGURATION) {
            builder.addModule(DEFAULT_MODULE_INSTANCE);
        } else {
            builder.addModule(new MongoJackModule(moduleConfiguration));
        }

        // JacksonCodec.decode is called multiple times on the same stream in order to deserialize multiple objects out
        // of an array.
        // This means that the stream will have trailing tokens after the first object is deserialized, and we don't
        // want to fail on that.
        builder.disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

        // to allow setters like set_id() which have leading underscore, and are common when using MongoDB
        builder.accessorNaming(new DefaultAccessorNamingStrategy.Provider().withFirstCharAcceptance(true, true));

        // disable serialize dates as timestamps because we have fewer runtime errors that way
        if (moduleConfiguration.isEnabled(MongoJackModuleFeature.DISABLE_DATES_AS_TIMESTAMPS)) {
            builder.configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        }

        if (moduleConfiguration.isEnabled(MongoJackModuleFeature.SET_SERIALIZATION_INCLUSION_NON_NULL)) {
            builder
                    .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                    .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL));
        }
        return builder.build();
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
        MongoAnnotationIntrospector annotationIntrospector = new MongoAnnotationIntrospector(context.typeFactory());
        context.insertAnnotationIntrospector(annotationIntrospector);
        // Only include non null properties, this makes it possible to use
        // object templates for querying and
        // partial object retrieving
        context.addSerializers(new MongoJackSerializers(moduleConfiguration));
        context.addDeserializers(new MongoJackDeserializers(moduleConfiguration));
    }

}
