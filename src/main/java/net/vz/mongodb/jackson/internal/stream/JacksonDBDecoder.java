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
package net.vz.mongodb.jackson.internal.stream;

import com.mongodb.*;
import org.bson.BSONCallback;
import org.bson.BSONObject;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * DB decoder that decodes the database stream using Jackson and bson4jackson
 *
 * @author James Roper
 * @since 1.1.2
 */
public class JacksonDBDecoder<T> implements DBDecoder {

    private final ObjectMapper objectMapper;
    private final Class<T> type;

    public JacksonDBDecoder(ObjectMapper objectMapper, Class<T> type) {
        this.objectMapper = objectMapper;
        this.type = type;
    }

    public DBCallback getDBCallback(DBCollection collection) {
        throw new UnsupportedOperationException("JacksonDBDecoder does not support callback style decoding");
    }

    public DBObject decode(byte[] b, DBCollection collection) {
        try {
            return decode(new ByteArrayInputStream(b), collection);
        } catch (IOException e) {
            // Not possible
            throw new RuntimeException("IOException encountered while reading from a byte array input stream", e);
        }
    }

    public DBObject decode(InputStream in, DBCollection collection) throws IOException {
        JacksonDBObject<T> decoded = new JacksonDBObject<T>();
        decoded.setObject(objectMapper.readValue(new DBDecoderBsonParser(0, new LimitingInputStream(in), decoded), type));
        return decoded;
    }

    public BSONObject readObject(byte[] b) {
        return decode(b, (DBCollection) null);
    }

    public BSONObject readObject(InputStream in) throws IOException {
        return decode(in, (DBCollection) null);
    }

    public int decode(byte[] b, BSONCallback callback) {
        throw new UnsupportedOperationException("JacksonDBDecoder does not support callback style decoding");
    }

    public int decode(InputStream in, BSONCallback callback) throws IOException {
        throw new UnsupportedOperationException("JacksonDBDecoder does not support callback style decoding");
    }
}
