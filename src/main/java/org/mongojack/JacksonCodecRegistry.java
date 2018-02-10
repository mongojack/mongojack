package org.mongojack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.annotations.Beta;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.mongojack.internal.MongoJackModule;
import org.mongojack.internal.stream.JacksonCodec;
import org.mongojack.internal.stream.JacksonDecoder;
import org.mongojack.internal.stream.JacksonEncoder;

/**
 * This is an experimental JacksonCodecRegistry for use with the Mongo 3.0+ java driver. It has only undergone basic
 * testing. This is use at your own risk.
 * 
 * @author christopher.ogrady
 *
 */
@Beta
public class JacksonCodecRegistry implements CodecRegistry {

    protected static final ObjectMapper DEFAULT_OBJECT_MAPPER = MongoJackModule
            .configure(new ObjectMapper());

    private final ObjectMapper objectMapper;
    private final Class<?> view;
    private CodecRegistry codecRegistry;
    
    public JacksonCodecRegistry(ObjectMapper objectMapper, Class<?> view) {
        if (objectMapper == null) {
            objectMapper = DEFAULT_OBJECT_MAPPER;
        }
        this.objectMapper = objectMapper;
        this.view = view;
        codecRegistry = MongoClient.getDefaultCodecRegistry();
    }

    public JacksonCodecRegistry() {
        this(null, null);
    }

    @Override
    public <T> Codec<T> get(Class<T> clazz) {
        return codecRegistry.get(clazz);
    }
    
    public <T> void addCodecForClass(Class<T> clazz) {
        JacksonEncoder<T> encoder = new JacksonEncoder<>(clazz, view, objectMapper);
        JacksonDecoder<T> decoder = new JacksonDecoder<>(clazz, view, objectMapper);
        JacksonCodec<T> jacksonCodec = new JacksonCodec<T>(encoder, decoder);
        codecRegistry = CodecRegistries.fromRegistries(codecRegistry, CodecRegistries.fromCodecs(jacksonCodec));
    }
}
