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
package org.mongojack;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.mongojack.internal.stream.DBEncoderBsonGenerator;

import java.io.IOException;

/**
 * Safe embedded object serializer.
 * <p>
 * When used with BsonObjectGenerator or DBEncoderBsonGenerator, passes values straight through.
 * <p>
 * When used with a {@link TokenBuffer} (as by {@link
 * com.fasterxml.jackson.databind.deser.BeanDeserializer#deserializeWithUnwrapped}),
 * temporarily clears the TokenBuffer codec before passing the value through,
 * so it will be properly serialized as an embedded object.
 * (Failure to do so would blow up the stack, as the TokenBuffer would
 * pass the object right back to the ObjectMapper.)
 * <p>
 * When used with other JsonSerializers, throws {@link IllegalArgumentException}
 * with a message that it's designed for use only with BsonObjectGenerator or
 * DBEncoderBsonGenerator or TokenBuffer.
 *
 * @author Kevin D. Keck
 * @since 3.0.4
 */
public abstract class TransformingEmbeddedObjectSerializer<InputType, TransformedType> extends JsonSerializer<InputType> {

    protected final boolean writeNullAsNull;

    protected TransformingEmbeddedObjectSerializer() {
        this(false);
    }

    protected TransformingEmbeddedObjectSerializer(final boolean writeNullAsNull) {
        this.writeNullAsNull = writeNullAsNull;
    }

    protected void writeEmbeddedObject(TransformedType value, JsonGenerator jgen)
        throws IOException {
        if (jgen instanceof DBEncoderBsonGenerator) {
            if (value == null && writeNullAsNull) {
                jgen.writeNull();
            } else {
                jgen.writeObject(value);
            }
        } else if (jgen instanceof TokenBuffer) {
            TokenBuffer buffer = (TokenBuffer) jgen;
            ObjectCodec codec = buffer.getCodec();
            buffer.setCodec(null);
            if (value == null && writeNullAsNull) {
                buffer.writeNull();
            } else {
                buffer.writeObject(value);
            }
            buffer.setCodec(codec);
        } else {
            String message = "JsonGenerator of type "
                + jgen.getClass().getName()
                + " not supported: " + getClass().getName()
                + " is designed for use only with "
                + DBEncoderBsonGenerator.class.getName()
                + " or "
                + TokenBuffer.class.getName();
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Transform to the desired type.  Careful of nulls!
     * 
     * @param value
     * @return
     */
    protected abstract TransformedType transform(InputType value);

    @Override
    public void serialize(
        InputType value, JsonGenerator jgen,
        SerializerProvider provider
    ) throws IOException {
        writeEmbeddedObject(transform(value), jgen);
    }

}
