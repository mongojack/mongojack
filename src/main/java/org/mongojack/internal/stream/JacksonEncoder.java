package org.mongojack.internal.stream;

import org.bson.BsonWriter;
import org.bson.UuidRepresentation;
import org.bson.codecs.Encoder;
import org.bson.codecs.EncoderContext;
import org.mongojack.MongoDatabindException;

import com.mongodb.MongoException;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;

public class JacksonEncoder<T> implements Encoder<T> {

    private final Class<T> clazz;
    private final Class<?> view;
    private final ObjectMapper objectMapper;
    private final UuidRepresentation uuidRepresentation;

    public JacksonEncoder(Class<T> clazz, Class<?> view, ObjectMapper objectMapper, final UuidRepresentation uuidRepresentation) {
        this.clazz = clazz;
        this.view = view;
        this.objectMapper = objectMapper;
        this.uuidRepresentation = uuidRepresentation;
    }

    public JacksonEncoder<T> withUuidRepresentation(final UuidRepresentation uuidRepresentation) {
        return new JacksonEncoder<>(
                clazz,
                view,
                objectMapper,
                uuidRepresentation);
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        // TODO jackson3: This still uses ObjectMapper._serializationContext() because Jackson 3.0.4 does not expose
        // a public mapper-level API for obtaining the configured live SerializationContext/ObjectWriteContext used by
        // custom generators. If Jackson exposes one later, or if this flow is refactored around ObjectWriter-only
        // entry points, this underscore API use can be removed.
        var context = objectMapper._serializationContext();
        try (JsonGenerator generator = new DBEncoderBsonGenerator(context, writer, uuidRepresentation)) {
            objectMapper.writerWithView(view).writeValue(generator, value);
        } catch (DatabindException e) {
            throw new MongoDatabindException(e);
        } catch (JacksonException e) {
            throw new MongoException("Error writing object out", e);
        }
    }

    @Override
    public Class<T> getEncoderClass() {
        return clazz;
    }
}
