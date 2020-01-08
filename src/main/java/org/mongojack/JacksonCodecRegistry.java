package org.mongojack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClientSettings;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.mongojack.internal.MongoJackModule;
import org.mongojack.internal.stream.JacksonCodec;
import org.mongojack.internal.stream.JacksonDecoder;
import org.mongojack.internal.stream.JacksonEncoder;

import java.util.Optional;

/**
 * This is an experimental JacksonCodecRegistry for use with the Mongo 3.0+ java driver. It has only undergone basic
 * testing. This is use at your own risk.
 *
 * @author christopher.ogrady
 */
public class JacksonCodecRegistry implements CodecRegistry {

    protected static final ObjectMapper DEFAULT_OBJECT_MAPPER = MongoJackModule
        .configure(new ObjectMapper());

    private final ObjectMapper objectMapper;
    private final Class<?> view;
    private CodecRegistry codecRegistry;
    private final JacksonMongoCollection<?> collection;

    private JacksonCodecRegistry() {
        this(DEFAULT_OBJECT_MAPPER);
    }

    public JacksonCodecRegistry(ObjectMapper objectMapper) {
        this(objectMapper, null, null);
    }

    public JacksonCodecRegistry(ObjectMapper objectMapper, Class<?> view, JacksonMongoCollection<?> collection) {
        this.collection = collection;
        if (objectMapper == null) {
            objectMapper = DEFAULT_OBJECT_MAPPER;
        }
        this.objectMapper = objectMapper;
        this.view = view;
        codecRegistry = MongoClientSettings.getDefaultCodecRegistry();
    }

    public static JacksonCodecRegistry withDefaultObjectMapper() {
        return new JacksonCodecRegistry(DEFAULT_OBJECT_MAPPER);
    }

    @Override
    public <T> Codec<T> get(Class<T> clazz) {
        Optional<Codec<T>> maybeCodec = getSafely(clazz);
        return maybeCodec.orElseGet(() -> {
            addCodecForClass(clazz);
            return codecRegistry.get(clazz);
        });
    }

    public <T> Optional<Codec<T>> getSafely(Class<T> clazz) {
        try {
            return Optional.of(codecRegistry.get(clazz));
        } catch (CodecConfigurationException e) {
            return Optional.empty();
        }
    }

    public <T> void addCodecForClass(Class<T> clazz) {
        JacksonEncoder<T> encoder = new JacksonEncoder<>(clazz, view, objectMapper);
        JacksonDecoder<T> decoder = new JacksonDecoder<>(clazz, view, objectMapper, collection);
        JacksonCodec<T> jacksonCodec = new JacksonCodec<>(encoder, decoder);
        codecRegistry = CodecRegistries.fromRegistries(codecRegistry, CodecRegistries.fromCodecs(jacksonCodec));
    }
}
