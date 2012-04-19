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
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;


/**
 * Creates an ID handler
 *
 * @author James Roper
 * @since 1.0
 */
public class IdHandlerFactory {

    public static <K> IdHandler<K, ?> getIdHandlerForProperty(ObjectMapper objectMapper, JavaType type) throws JsonMappingException {
        JsonDeserializer deserializer = JacksonAccessor.findDeserializer(objectMapper, type);
        JsonDeserializer idDeserializer = null;
        JsonSerializer idSerializer = null;
        
        if (deserializer instanceof BeanDeserializer) {
            idDeserializer = ((BeanDeserializer) deserializer).findProperty("_id").getValueDeserializer();
        }

        JsonSerializer serializer = JacksonAccessor.findValueSerializer(objectMapper, type);
        if (serializer instanceof BeanSerializerBase) {
            BeanPropertyWriter writer = JacksonAccessor.findPropertyWriter((BeanSerializerBase) serializer, "_id");
            if (writer != null) {
                idSerializer = writer.getSerializer();
            }
        }

        if (idDeserializer != null && idSerializer != null) {
            return new IdHandler.JacksonIdHandler(idSerializer, idDeserializer);
        } else {
            return new IdHandler.NoopIdHandler<K>();
        }
    }
}
