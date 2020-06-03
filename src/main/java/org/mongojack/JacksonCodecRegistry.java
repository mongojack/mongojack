package org.mongojack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;
import org.bson.BsonValue;
import com.mongodb.MongoClientSettings;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
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
    private CodecRegistry codecRegistry;
    private ConcurrentHashMap<Class<?>, Codec<?>> codecCache = new ConcurrentHashMap<>();
    final CodecRegistry defaultCodecRegistry;

    public JacksonCodecRegistry(ObjectMapper objectMapper) {
        this(objectMapper, null);
    }

    public JacksonCodecRegistry(ObjectMapper objectMapper, Class<?> view) {
        this.objectMapper = objectMapper;
        this.view = view;
        defaultCodecRegistry = MongoClientSettings.getDefaultCodecRegistry();
        codecRegistry = CodecRegistries.fromRegistries(
            CodecRegistries.fromProviders(this),
            defaultCodecRegistry
        );
    }

    @Override
    public <T> Codec<T> get(Class<T> clazz) {
        return codecRegistry.get(clazz);
    }

    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        if (DocumentSerializationUtils.isKnownClass(clazz) ||
            DBObject.class.isAssignableFrom(clazz) ||
            Document.class.isAssignableFrom(clazz) ||
            Bson.class.isAssignableFrom(clazz) ||
            BsonValue.class.isAssignableFrom(clazz)) {
            return null;
        }
        return addCodecForClass(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> Codec<T> addCodecForClass(Class<T> clazz) {
        return (Codec<T>)codecCache.computeIfAbsent(clazz, (k) -> {
            JacksonEncoder<T> encoder = new JacksonEncoder<>(clazz, view, objectMapper);
            JacksonDecoder<T> decoder = new JacksonDecoder<>(clazz, view, objectMapper);
            return new JacksonCodec<>(encoder, decoder);
        });
    }

}
