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

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import org.mongojack.DBRef;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Deserializers for MongoJack
 * 
 * @author James Roper
 * @since 1.2
 */
public class MongoJackDeserializers extends SimpleDeserializers {
    public MongoJackDeserializers() {
        addDeserializer(Date.class, new DateDeserializer());
        addDeserializer(Instant.class, new MongoJackInstantDeserializer());
        addDeserializer(Calendar.class, new CalendarDeserializer());
        addDeserializer(UUID.class, new UUIDDeserializer());
        addDeserializer(com.mongodb.DBRef.class, new MongoDBRefDeserializer());
    }

    @Override
    public JsonDeserializer<?> findBeanDeserializer(JavaType type,
            DeserializationConfig config, BeanDescription beanDesc)
            throws JsonMappingException {
        if (type.getRawClass() == DBRef.class) {
            if (type.containedTypeCount() != 2) {
                throw new JsonMappingException(null, "Property doesn't declare object and key type");
            }
            JavaType objectType = type.containedType(0);
            JavaType keyType = type.containedType(1);
            return new DBRefDeserializer(objectType, keyType);
        }
        return super.findBeanDeserializer(type, config, beanDesc);
    }
}
