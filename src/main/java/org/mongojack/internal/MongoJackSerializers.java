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

import com.fasterxml.jackson.databind.module.SimpleSerializers;
import org.bson.BsonValue;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.MongoJackModuleConfiguration;
import org.mongojack.MongoJackModuleFeature;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Serializers for MongoJack
 *
 * @author James Roper
 * @since 1.2
 */
public class MongoJackSerializers extends SimpleSerializers {

    public MongoJackSerializers() {
        this(MongoJackModule.DEFAULT_CONFIGURATION);
    }

    public MongoJackSerializers(final MongoJackModuleConfiguration moduleConfiguration) {
        addSerializer(new DBRefSerializer());
        addSerializer(new MongoDBRefSerializer());
        addSerializer(ObjectId.class, new ObjectIdSerializer());
        addSerializer(Date.class, new DateSerializer());
        addSerializer(Calendar.class, new CalendarSerializer());
        if (moduleConfiguration.isEnabled(MongoJackModuleFeature.WRITE_INSTANT_AS_BSON_DATE)) {
            addSerializer(Instant.class, new MongoJackInstantSerializer());
        }
        addSerializer(UUID.class, new UUIDSerializer());
        if (moduleConfiguration.isEnabled(MongoJackModuleFeature.ENABLE_BSON_VALUE_SERIALIZATION)) {
            addSerializer(Bson.class, new BsonSerializer());
            addSerializer(BsonValue.class, new BsonValueSerializer());
        }
    }
}
