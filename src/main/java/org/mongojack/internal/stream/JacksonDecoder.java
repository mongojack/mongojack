package org.mongojack.internal.stream;

import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bson.BsonBinaryWriter;
import org.bson.BsonReader;
import org.bson.codecs.Decoder;
import org.bson.codecs.DecoderContext;
import org.bson.io.BasicOutputBuffer;
import org.mongojack.JacksonMongoCollection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JacksonDecoder<T> implements Decoder<T> {

    private final Class<T> clazz;
    private final ObjectMapper objectMapper;
    private final Class<?> view;
    private final JacksonMongoCollection<?> collection;

    public JacksonDecoder(Class<T> clazz, Class<?> view, ObjectMapper objectMapper, JacksonMongoCollection<?> collection) {
        this.clazz = clazz;
        this.objectMapper = objectMapper;
        this.view = view;
        this.collection = collection;
    }

    private T decode(byte[] b) {
        try {
            return decode(new ByteArrayInputStream(b));
        } catch (IOException e) {
            // Not possible
            throw new RuntimeException(
                    "IOException encountered while reading from a byte array input stream",
                    e);
        }
    }

    private T decode(InputStream in)
            throws IOException {
        JacksonDBObject<T> decoded = new JacksonDBObject<T>();
        try (DBDecoderBsonParser parser = new DBDecoderBsonParser(
                new IOContext(new BufferRecycler(), in, false), 0, in, decoded,
                collection, objectMapper)) {
            return objectMapper.reader().forType(clazz).withView(view).readValue(parser);
        }
    }

    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        BasicOutputBuffer bob = new BasicOutputBuffer();
        BsonBinaryWriter binaryWriter = new BsonBinaryWriter(bob);
        try {
        binaryWriter.pipe(reader);
        return decode(bob.getInternalBuffer());
        } finally {
            binaryWriter.close();
            bob.close();
        }
    }
}
