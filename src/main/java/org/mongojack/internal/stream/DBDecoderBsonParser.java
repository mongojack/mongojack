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

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.AbstractBsonReader;
import org.bson.UuidRepresentation;

import java.io.IOException;

/**
 * Parser that wraps BSONParser to convert bson4jackson ObjectIds to org.bson
 * ObjectIds, and stores error codes
 *
 * @author James Roper
 * @since 1.1.2
 */
public class DBDecoderBsonParser extends JsonParserAdapter {

    public DBDecoderBsonParser(
        IOContext ctxt,
        int jsonFeatures,
        AbstractBsonReader reader,
        ObjectMapper objectMapper,
        final UuidRepresentation uuidRepresentation
    ) {
        // Honor document length must be true
        super(ctxt, jsonFeatures, reader, uuidRepresentation);
        setCodec(objectMapper);
    }

    @Override
    public String getText() throws IOException {
        if (JsonToken.VALUE_EMBEDDED_OBJECT == getCurrentToken()) {
            return null;
        }
        return super.getText();
    }

}
