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

import net.vz.mongodb.jackson.DBRef;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.Deserializers;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.module.SimpleDeserializers;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.type.JavaType;

import java.util.Calendar;
import java.util.Date;

/**
 * Deserializers for Mongo Jackson Mapper
 *
 * @author James Roper
 * @since 1.2
 */
public class MongoJacksonDeserializers extends SimpleDeserializers {
    public MongoJacksonDeserializers() {
        addDeserializer(Date.class, new DateDeserializer());
        addDeserializer(Calendar.class, new CalendarDeserializer());
    }

    public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, DeserializerProvider provider, BeanDescription beanDesc, BeanProperty property) throws JsonMappingException {
        if (type.getRawClass() == DBRef.class) {
            if (!type.hasGenericTypes()) {
                throw new JsonMappingException("Property " + property + " doesn't declare object and key type");
            }
            JavaType objectType = type.containedType(0);
            JavaType keyType = type.containedType(1);
            return new DBRefDeserializer(objectType, keyType);
        }
        return super.findBeanDeserializer(type, config, provider, beanDesc, property);
    }
}
