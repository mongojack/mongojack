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
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The ObjectID serialising module
 *
 * @author James Roper
 * @since 1.0
 */
public class MongoJacksonMapperModule extends Module {
    public static final Module INSTANCE = new MongoJacksonMapperModule();

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
