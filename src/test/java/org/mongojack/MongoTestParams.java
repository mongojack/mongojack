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
package org.mongojack;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

/**
 * Configuration for tests
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MongoTestParams {

    /**
     * The type of deserialiser to run the test with, by default, both.
     */
    SerializationType deserializerType() default SerializationType.BOTH;

    /**
     * The type of serializer to run the test with, by default, both.
     */
    SerializationType serializerType() default SerializationType.BOTH;


    public enum SerializationType {
        STREAM(SerializationConfig.STREAM),
        OBJECT(SerializationConfig.OBJECT),
        BOTH(SerializationConfig.STREAM, SerializationConfig.OBJECT);

        private final Iterable<SerializationConfig> configs;

        SerializationType(SerializationConfig... configs) {
            this.configs = Arrays.asList(configs);
        }

        public Iterable<SerializationConfig> getConfigs() {
            return configs;
        }
    }

    public class SerializationConfig {
        static final SerializationConfig STREAM = new SerializationConfig(true, "stream");
        static final SerializationConfig OBJECT = new SerializationConfig(false, "object");
        private final boolean enabled;
        private final String name;

        private SerializationConfig(boolean enabled, String name) {
            this.enabled = enabled;
            this.name = name;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getName() {
            return name;
        }
    }
}
