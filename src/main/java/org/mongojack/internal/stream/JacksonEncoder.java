package org.mongojack.internal.stream;

import org.bson.BsonWriter;
import org.bson.UuidRepresentation;
import org.bson.codecs.Encoder;
import org.bson.codecs.EncoderContext;
import org.mongojack.MongoDatabindException;

import com.mongodb.MongoException;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.io.ContentReference;
import tools.jackson.core.io.IOContext;
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
        var context = objectMapper._serializationContext();
        var ioContext = new IOContext(
                context.tokenStreamFactory().streamReadConstraints(),
                context.tokenStreamFactory().streamWriteConstraints(),
                context.tokenStreamFactory().errorReportConfiguration(),
                context.tokenStreamFactory()._getBufferRecycler(),
                ContentReference.unknown(),
                false,
                null);
        try (JsonGenerator generator = new DBEncoderBsonGenerator(context, ioContext, writer, uuidRepresentation)) {
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
