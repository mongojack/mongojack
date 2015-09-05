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

import java.io.IOException;

import org.mongojack.internal.object.BsonObjectGenerator;
import org.mongojack.internal.stream.DBEncoderBsonGenerator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/**
 * Safe embedded object serializer.
 *
 * When used with BsonObjectGenerator or DBEncoderBsonGenerator, passes values straight through.
 *
 * When used with a {@link TokenBuffer} (as by {@link
 * com.fasterxml.jackson.databind.deser.BeanDeserializer#deserializeWithUnwrapped}),
 * temporarily clears the TokenBuffer codec before passing the value through,
 * so it will be properly serialized as an embedded object.
 * (Failure to do so would blow up the stack, as the TokenBuffer would
 * pass the object right back to the ObjectMapper.)
 *
 * When used with other JsonSerializers, throws {@link java.lang.IllegalArgumentException}
 * with a message that it's designed for use only with BsonObjectGenerator or
 * DBEncoderBsonGenerator or TokenBuffer.
 *
 * @author Kevin D. Keck
 * @since 3.0.4
 */
public abstract class EmbeddedObjectSerializer<T> extends JsonSerializer<T> {

    protected void writeEmbeddedObject(T value, JsonGenerator jgen)
            throws IOException {
        if (jgen instanceof BsonObjectGenerator || jgen instanceof DBEncoderBsonGenerator) {
            jgen.writeObject(value);
        } else if (jgen instanceof TokenBuffer) {
            TokenBuffer buffer = (TokenBuffer) jgen;
            ObjectCodec codec = buffer.getCodec();
            buffer.setCodec(null);
            buffer.writeObject(value);
            buffer.setCodec(codec);
        } else {
            String message = "JsonGenerator of type "
                    + jgen.getClass().getName()
                    + " not supported: " + getClass().getName()
                    + " is designed for use only with "
                    + BsonObjectGenerator.class.getName()
                    + " or "
                    + DBEncoderBsonGenerator.class.getName()
                    + " or "
                    + TokenBuffer.class.getName();
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public void serialize(T value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException {
        writeEmbeddedObject(value, jgen);
    }
}
