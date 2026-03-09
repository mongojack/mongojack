package org.mongojack.internal.stream;

import java.io.InputStream;

import org.bson.AbstractBsonReader;
import org.bson.BsonReader;
import org.bson.UuidRepresentation;
import org.bson.codecs.Decoder;
import org.bson.codecs.DecoderContext;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

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
                uuidRepresentation);
    }

    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        // TODO jackson3: This still uses ObjectMapper._deserializationContext() because Jackson 3.0.4 does not expose
        // a public mapper-level API for obtaining the configured live DeserializationContext/ObjectReadContext used by
        // custom parsers. If Jackson exposes one later, or if this flow is refactored around ObjectReader-only entry
        // points, this underscore API use can be removed.
        var context = objectMapper._deserializationContext();
        try (DBDecoderBsonParser parser = new DBDecoderBsonParser(context, 0,
                (AbstractBsonReader) reader, objectMapper, uuidRepresentation)) {
            return objectMapper.reader().forType(clazz).withView(view).readValue(parser);
        } catch (JacksonException e) {
            throw new RuntimeException("JacksonException encountered while parsing", e);
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
