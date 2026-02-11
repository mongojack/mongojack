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

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.bson.BsonBinary;
import org.bson.BsonWriter;
import org.bson.UuidRepresentation;
import org.bson.types.ObjectId;
import org.mongojack.internal.util.DocumentSerializationUtils;

import com.mongodb.DBRef;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.io.IOContext;

/**
 * BsonGenerator that adds a bit of functionality specific to DBEncoding to the
 * bson4jackson DBEncoder
 */
public class DBEncoderBsonGenerator extends JsonGeneratorAdapter {

    public DBEncoderBsonGenerator(
            ObjectWriteContext context,
            IOContext ioContext,
            int jsonFeatures,
            BsonWriter out,
            final UuidRepresentation uuidRepresentation) {
        super(context, ioContext, jsonFeatures, out, uuidRepresentation);
    }

    public DBEncoderBsonGenerator(
            ObjectWriteContext context,
            IOContext ioContext,
            final BsonWriter writer,
            final UuidRepresentation uuidRepresentation) {
        this(context, ioContext, StreamWriteFeature.collectDefaults(), writer, uuidRepresentation);
    }

    @Override
    public JsonGenerator writePOJO(Object value) throws JacksonException {
        if (value == null) {
            writeNull();
        } else if (value instanceof Date) {
            writer.writeDateTime(((Date) value).getTime());
        } else if (value instanceof Calendar) {
            writer.writeDateTime(((Calendar) value).getTime().getTime());
        } else if (value instanceof ObjectId) {
            writeBsonObjectId((ObjectId) value);
        } else if (value instanceof UUID) {
            writer.writeBinaryData(new BsonBinary((UUID) value, uuidRepresentation));
        } else if (value instanceof DBRef) {
            DBRef dbRef = (DBRef) value;
            writeStartObject();
            writeName("$ref");
            writeString(dbRef.getCollectionName());
            writeName("$id");
            writePOJO(dbRef.getId());
            if (dbRef.getDatabaseName() != null) {
                writeName("$db");
                writeString(dbRef.getDatabaseName());
            }
            writeEndObject();
        } else {
            if (!DocumentSerializationUtils.writeKnownType(value, writer)) {
                _objectWriteContext.writeValue(this, value);
            }
        }
        return this;
    }
}
