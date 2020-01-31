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

import com.fasterxml.jackson.core.JsonGenerator;
import com.mongodb.DBRef;
import org.bson.BsonWriter;
import org.bson.types.ObjectId;
import org.mongojack.internal.util.DocumentSerializationUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * BsonGenerator that adds a bit of functionality specific to DBEncoding to the
 * bson4jackson DBEncoder
 */
public class DBEncoderBsonGenerator extends JsonGeneratorAdapter {

    public DBEncoderBsonGenerator(int jsonFeatures, BsonWriter out) {
        super(jsonFeatures, null, out);
    }

    public DBEncoderBsonGenerator(final BsonWriter writer) {
        this(JsonGenerator.Feature.collectDefaults(), writer);
    }

    @Override
    protected void _writeSimpleObject(Object value) throws IOException {
        if (value instanceof Date) {
            writer.writeDateTime(((Date) value).getTime());
        } else if (value instanceof Calendar) {
            writer.writeDateTime(((Calendar) value).getTime().getTime());
        } else if (value instanceof ObjectId) {
            writeBsonObjectId((ObjectId) value);
        } else if (value instanceof DBRef) {
            DBRef dbRef = (DBRef) value;
            writeStartObject();
            writeFieldName("$ref");
            writeString(dbRef.getCollectionName());
            writeFieldName("$id");
            writeObject(dbRef.getId());
            if (dbRef.getDatabaseName() != null) {
                writeFieldName("$db");
                writeString(dbRef.getDatabaseName());
            }
            writeEndObject();
        } else {
            if (!DocumentSerializationUtils.writeKnownType(value, writer)) {
                super._writeSimpleObject(value);
            }
        }
    }
}
