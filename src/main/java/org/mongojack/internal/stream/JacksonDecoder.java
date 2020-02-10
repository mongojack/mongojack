package org.mongojack.internal.stream;

import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.AbstractBsonReader;
import org.bson.BsonReader;
import org.bson.UuidRepresentation;
import org.bson.codecs.Decoder;
import org.bson.codecs.DecoderContext;

import java.io.IOException;
import java.io.InputStream;

public class JacksonDecoder<T> implements Decoder<T> {

    private static final InputStream EMPTY_INPUT_STREAM = new EmptyInputStream();

    private final Class<T> clazz;
    private final ObjectMapper objectMapper;
    private final Class<?> view;
    private final UuidRepresentation uuidRepresentation;

    public JacksonDecoder(Class<T> clazz, Class<?> view, ObjectMapper objectMapper, final UuidRepresentation uuidRepresentation) {
        this.clazz = clazz;
        this.objectMapper = objectMapper;
        this.view = view;
        this.uuidRepresentation = uuidRepresentation;
    }

    public JacksonDecoder<T> withUuidRepresentation(final UuidRepresentation uuidRepresentation) {
        return new JacksonDecoder<>(
            clazz,
            view,
            objectMapper,
            uuidRepresentation
        );
    }

    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        try (DBDecoderBsonParser parser = new DBDecoderBsonParser(new IOContext(new BufferRecycler(), EMPTY_INPUT_STREAM, false), 0, (AbstractBsonReader) reader, objectMapper, uuidRepresentation)) {
            return objectMapper.reader().forType(clazz).withView(view).readValue(parser);
        } catch (IOException e) {
            throw new RuntimeException("IOException encountered while parsing", e);
        }
    }

    private static class EmptyInputStream extends InputStream {
        @Override
        public int available() {
            return 0;
        }

        public int read() {
            return -1;
        }
    }

}
