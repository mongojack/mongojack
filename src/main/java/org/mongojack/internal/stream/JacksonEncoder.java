package org.mongojack.internal.stream;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import de.undercouch.bson4jackson.BsonGenerator;
import org.bson.BsonBinaryReader;
import org.bson.BsonWriter;
import org.bson.ByteBufNIO;
import org.bson.codecs.Encoder;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;
import org.bson.io.ByteBufferBsonInput;
import org.bson.io.OutputBuffer;
import org.mongojack.MongoJsonMappingException;

import java.io.IOException;

import static java.nio.ByteBuffer.wrap;
import javax.annotation.Generated;

public class JacksonEncoder<T> implements Encoder<T> {

    private Class<T> clazz;
    private Class<?> view;
    private ObjectMapper objectMapper;

    @Generated("SparkTools")
    public JacksonEncoder(Class<T> clazz, Class<?> view, ObjectMapper objectMapper) {
        this.clazz = clazz;
        this.view = view;
        this.objectMapper = objectMapper;
    }

    private int writeObject(OutputBuffer buf, T object) {
        OutputBufferOutputStream stream = new OutputBufferOutputStream(buf);
        BsonGenerator generator = new DBEncoderBsonGenerator(
                JsonGenerator.Feature.collectDefaults(), stream);
        try {
            objectMapper.writerWithView(view).writeValue(generator, object);
            // The generator buffers everything so that it can write the
            // number of bytes to the stream
            generator.close();
        } catch (JsonMappingException e) {
            throw new MongoJsonMappingException(e);
        } catch (IOException e) {
            throw new MongoException("Error writing object out", e);
        }
        return stream.getCount();
    }


    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        BasicOutputBuffer buffer = new BasicOutputBuffer();
        try {
            writeObject(buffer, value);
            BsonBinaryReader reader = new BsonBinaryReader(new ByteBufferBsonInput(new ByteBufNIO(wrap(buffer.toByteArray()))));
            try {
                writer.pipe(reader);
            } finally {
                reader.close();
            }
        } finally {
            buffer.close();
        }
    }

    @Override
    public Class<T> getEncoderClass() {
        return clazz;
    }
}
