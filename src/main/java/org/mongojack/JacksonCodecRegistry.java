package org.mongojack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;
import com.mongodb.client.model.mql.MqlValue;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.mongojack.internal.stream.JacksonCodec;
import org.mongojack.internal.stream.JacksonDecoder;
import org.mongojack.internal.stream.JacksonEncoder;
import org.mongojack.internal.util.DocumentSerializationUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This is an experimental JacksonCodecRegistry for use with the Mongo 3.0+ java driver. It has only undergone basic
 * testing. This is use at your own risk.
 *
 * @author christopher.ogrady
 */
public class JacksonCodecRegistry implements CodecRegistry, CodecProvider {

    private final ObjectMapper objectMapper;
    private final Class<?> view;
    private final ConcurrentHashMap<Class<?>, Codec<?>> codecCache = new ConcurrentHashMap<>();
    private final CodecRegistry defaultCodecRegistry;
    private final UuidRepresentation uuidRepresentation;

    public JacksonCodecRegistry(ObjectMapper objectMapper, CodecRegistry defaultCodecRegistry, final UuidRepresentation uuidRepresentation) {
        this(objectMapper, defaultCodecRegistry, null, uuidRepresentation);
    }

    public JacksonCodecRegistry(ObjectMapper objectMapper, CodecRegistry defaultCodecRegistry, Class<?> view, final UuidRepresentation uuidRepresentation) {
        this.objectMapper = objectMapper;
        this.view = view;
        this.defaultCodecRegistry = defaultCodecRegistry;
        this.uuidRepresentation = uuidRepresentation;
    }

    @Override
    public <T> Codec<T> get(Class<T> clazz) {
        if (isDefault(clazz)) {
            return defaultCodecRegistry.get(clazz);
        }
        return addCodecForClass(clazz);
    }

    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        if (isDefault(clazz)) {
            return defaultCodecRegistry.get(clazz, registry);
        }
        return addCodecForClass(clazz);
    }

    protected <T> boolean isDefault(final Class<T> clazz) {
        return DocumentSerializationUtils.isKnownClass(clazz) ||
            DBObject.class.isAssignableFrom(clazz) ||
            Document.class.isAssignableFrom(clazz) ||
            Bson.class.isAssignableFrom(clazz) ||
            BsonValue.class.isAssignableFrom(clazz) ||
            MqlValue.class.isAssignableFrom(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> Codec<T> addCodecForClass(Class<T> clazz) {
        return (Codec<T>) codecCache.computeIfAbsent(clazz, (k) -> {
            JacksonEncoder<T> encoder = new JacksonEncoder<>(clazz, view, objectMapper, uuidRepresentation);
            JacksonDecoder<T> decoder = new JacksonDecoder<>(clazz, view, objectMapper, uuidRepresentation);
            return new JacksonCodec<>(encoder, decoder, objectMapper, this);
        });
    }

    public UuidRepresentation getUuidRepresentation() {
        return uuidRepresentation;
    }

}
