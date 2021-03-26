package org.mongojack;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mongojack.internal.MongoJackModule;

/**
 * Can be used by OSGi containers (or anyone else) to configure a custom ObjectMapper instance.  This is necessary
 * because {@link org.mongojack.internal.MongoJackModule} is in the internal module, but it also can be used
 * by any caller to avoid using the "internal" implementations.
 */
public class ObjectMapperConfigurer {

    private ObjectMapperConfigurer() {
        // nothing
    }

    /**
     * Install the MongoJackModule into the object mapper with recommended settings.  Also installs JavaTimeModule.
     *
     * @param mapper
     * @return
     */
    public static ObjectMapper configureObjectMapper(ObjectMapper mapper) {
        return MongoJackModule.configure(mapper);
    }

    /**
     * Install the MongoJackModule into the object mapper with recommended settings.  Also installs JavaTimeModule.
     *
     * @param mapper
     * @return
     */
    public static ObjectMapper configureObjectMapper(ObjectMapper mapper, MongoJackModuleConfiguration moduleConfiguration) {
        return MongoJackModule.configure(mapper, moduleConfiguration);
    }

    /**
     * Installs <em>only</em> the MongoJackModule, without JavaTimeModule or other settings.
     *
     * @param mapper
     * @return
     */
    public static ObjectMapper addMongojackModuleOnly(ObjectMapper mapper) {
        mapper.registerModule(MongoJackModule.DEFAULT_MODULE_INSTANCE);
        return mapper;
    }

    /**
     * Installs <em>only</em> the MongoJackModule, without JavaTimeModule or other settings.
     *
     * @param mapper
     * @return
     */
    public static ObjectMapper addMongojackModuleOnly(ObjectMapper mapper, MongoJackModuleConfiguration moduleConfiguration) {
        mapper.registerModule(new MongoJackModule(moduleConfiguration));
        return mapper;
    }

}
