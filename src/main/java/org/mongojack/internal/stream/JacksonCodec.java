package org.mongojack.internal.stream;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class JacksonCodec<T> implements Codec<T> {

    private final JacksonEncoder<T> encoder;
    private final JacksonDecoder<T> decoder;

    public JacksonCodec(JacksonEncoder<T> encoder, JacksonDecoder<T> decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        this.encoder.encode(writer, value, encoderContext);
    }

    @Override
    public Class<T> getEncoderClass() {
        return encoder.getEncoderClass();
    }

    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        return decoder.decode(reader, decoderContext);
    }
}
