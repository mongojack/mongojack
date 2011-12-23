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

import com.mongodb.DBEncoder;
import com.mongodb.MongoException;
import de.undercouch.bson4jackson.BsonGenerator;
import net.vz.mongodb.jackson.MongoJsonMappingException;
import org.bson.BSONObject;
import org.bson.io.OutputBuffer;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * DBEncoder that uses bson4jackson if the object is a JacksonDBObject
 */
public class JacksonDBEncoder implements DBEncoder {
    private final DBEncoder defaultEncoder;
    private final ObjectMapper objectMapper;

    public JacksonDBEncoder(DBEncoder defaultEncoder, ObjectMapper objectMapper) {
        this.defaultEncoder = defaultEncoder;
        this.objectMapper = objectMapper;
    }

    public int writeObject(OutputBuffer buf, BSONObject object) {
        if (object instanceof JacksonDBObject) {
            Object actualObject = ((JacksonDBObject) object).getObject();
            OutputBufferOutputStream stream = new OutputBufferOutputStream(buf);
            BsonGenerator generator = new BsonGenerator(JsonGenerator.Feature.collectDefaults(), 0, stream);
            try {
                objectMapper.writeValue(generator, actualObject);
                // The generator buffers everything so that it can write the number of bytes to the stream
                generator.close();
            } catch (JsonMappingException e) {
                throw new MongoJsonMappingException(e);
            } catch (IOException e) {
                throw new MongoException("Error writing object out", e);
            }
            return stream.getCount();
        } else {
            // Fallback to the default one
            return defaultEncoder.writeObject(buf, object);
        }
    }
}
