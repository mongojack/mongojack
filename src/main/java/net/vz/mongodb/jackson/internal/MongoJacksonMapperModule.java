/*
 * Copyright 2011 VZ Netzwerke Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.vz.mongodb.jackson.internal;

import net.vz.mongodb.jackson.internal.stream.ServerErrorProblemHandler;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The ObjectID serialising module
 *
 * @author James Roper
 * @since 1.0
 */
public class MongoJacksonMapperModule extends Module {
    public static final Module INSTANCE = new MongoJacksonMapperModule();

    /**
     * Configure the given object mapper to be used with the Mongo Jackson Mapper.  Please call this method rather than
     * calling objectMapper.with(MongoJacksonMapperModule.INSTANCE), because Jacksons module system doesn't allow the
     * mongo jackson mapper to do all the configuration it needs to do.  This method will do that configuration though.
     *
     * @param objectMapper The object mapper to configure
     * @return This object mapper (for chaining)
     */
    public static ObjectMapper configure(ObjectMapper objectMapper) {
        objectMapper.registerModule(INSTANCE);
        objectMapper.setHandlerInstantiator(new MongoJacksonHandlerInstantiator(
                new MongoAnnotationIntrospector(objectMapper.getDeserializationConfig())));
        return objectMapper;
    }

    @Override
    public String getModuleName() {
        return "Object ID Module";
    }

    @Override
    public Version version() {
        return new Version(1, 0, 0, null);
    }

    @Override
    public void setupModule(SetupContext context) {
        MongoAnnotationIntrospector annotationIntrospector = new MongoAnnotationIntrospector(context.getDeserializationConfig());
        context.insertAnnotationIntrospector(annotationIntrospector);
        // Only include non null properties, this makes it possible to use object templates for querying and
        // partial object retrieving
        context.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        context.getDeserializationConfig().addHandler(new ServerErrorProblemHandler());
        context.addSerializers(new MongoJacksonSerializers());
        context.addDeserializers(new MongoJacksonDeserializers());
    }
}
