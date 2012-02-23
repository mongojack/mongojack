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
package net.vz.mongodb.jackson.internal.util;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.ser.BeanSerializer;

/**
 * Place that hacks (using reflection) exist for Jackson, until we get features into jackson
 *
 * @author James Roper
 * @since 1.0
 */
public class IdHandlerFactory {

    public static <K> IdHandler<K, ?> getIdHandlerForProperty(ObjectMapper objectMapper, JavaType type) throws JsonMappingException {
        JsonDeserializer deserializer = objectMapper.findDeserializer(type);
        JsonDeserializer idDeserializer = null;
        JsonSerializer idSerializer = null;
        
        if (deserializer instanceof BeanDeserializer) {
            idDeserializer = ((BeanDeserializer) deserializer).findProperty("_id").getValueDeserializer();
        }
        JsonSerializer serializer = objectMapper.findSerializer(type);
        if (serializer instanceof BeanSerializer) {
            idSerializer = ((BeanSerializer) serializer).getSerializerForProperty("_id");
        }

        if (idDeserializer != null && idSerializer != null) {
            return new IdHandler.JacksonIdHandler(idSerializer, idDeserializer);
        } else {
            return new IdHandler.NoopIdHandler<K>();
        }
    }
}
