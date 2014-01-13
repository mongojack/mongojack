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
package org.mongojack.internal.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.mongodb.DBRef;

import de.undercouch.bson4jackson.BsonGenerator;

/**
 * BsonGenerator that adds a bit of functionality specific to DBEncoding to the
 * bson4jackson DBEncoder
 */
public class DBEncoderBsonGenerator extends BsonGenerator {
    public DBEncoderBsonGenerator(int jsonFeatures, OutputStream out) {
        super(jsonFeatures, 0, out);
    }

    @Override
    protected void _writeSimpleObject(Object value) throws IOException,
            JsonGenerationException {
        if (value instanceof Date) {
            writeDateTime((Date) value);
        } else if (value instanceof Calendar) {
            writeDateTime(((Calendar) value).getTime());
        } else if (value instanceof ObjectId) {
            writeObjectId(ObjectIdConvertor.convert((ObjectId) value));
        } else if (value instanceof DBRef) {
            DBRef dbRef = (DBRef) value;
            writeStartObject();
            writeFieldName("$ref");
            writeString(dbRef.getRef());
            writeFieldName("$id");
            writeObject(dbRef.getId());
            if (dbRef.getDB() != null) {
                writeFieldName("$db");
                writeString(dbRef.getDB().getName());
            }
            writeEndObject();
        } else {
            super._writeSimpleObject(value);
        }
    }
}
