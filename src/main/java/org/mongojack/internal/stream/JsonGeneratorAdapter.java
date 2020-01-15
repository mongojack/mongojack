package org.mongojack.internal.stream;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.base.GeneratorBase;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import org.bson.BsonBinary;
import org.bson.BsonWriter;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class JsonGeneratorAdapter extends GeneratorBase {

    protected final BsonWriter writer;

    protected JsonGeneratorAdapter(final int features, final ObjectCodec codec, final BsonWriter writer) {
        super(features, codec);
        this.writer = writer;
    }

    protected JsonGeneratorAdapter(final int features, final ObjectCodec codec, final JsonWriteContext ctxt, final BsonWriter writer) {
        super(features, codec, ctxt);
        this.writer = writer;
    }

    @Override
    public void writeStartArray() throws IOException {
        writer.writeStartArray();
    }

    @Override
    public void writeEndArray() throws IOException {
        writer.writeEndArray();
    }

    @Override
    public void writeStartObject() throws IOException {
        writer.writeStartDocument();
    }

    @Override
    public void writeEndObject() throws IOException {
        writer.writeEndDocument();
    }

    @Override
    public void writeFieldName(final String name) throws IOException {
        writer.writeName(name);
    }

    @Override
    public void writeString(final String text) throws IOException {
        writer.writeString(text);
    }

    @Override
    public void writeString(final char[] text, final int offset, final int len) throws IOException {
        writer.writeString(new String(text, offset, len));
    }

    @Override
    public void writeRawUTF8String(final byte[] text, final int offset, final int length) throws IOException {
        writer.writeString(new String(text, offset, length, StandardCharsets.UTF_8));
    }

    @Override
    public void writeUTF8String(final byte[] text, final int offset, final int length) throws IOException {
        writer.writeString(new String(text, offset, length, StandardCharsets.UTF_8));
    }

    @Override
    public void writeRaw(final String text) throws IOException {
        throw new UnsupportedOperationException("writeRaw not supported");
    }

    @Override
    public void writeRaw(final String text, final int offset, final int len) throws IOException {
        throw new UnsupportedOperationException("writeRaw not supported");
    }

    @Override
    public void writeRaw(final char[] text, final int offset, final int len) throws IOException {
        throw new UnsupportedOperationException("writeRaw not supported");
    }

    @Override
    public void writeRaw(final char c) throws IOException {
        throw new UnsupportedOperationException("writeRaw not supported");
    }

    @Override
    public void writeBinary(final Base64Variant bv, final byte[] data, final int offset, final int len) throws IOException {
        writer.writeBinaryData(new BsonBinary(Arrays.copyOfRange(data, offset, len)));
    }

    @Override
    public void writeNumber(final int v) throws IOException {
        writer.writeInt32(v);
    }

    @Override
    public void writeNumber(final long v) throws IOException {
        writer.writeInt64(v);
    }

    @Override
    public void writeNumber(final BigInteger v) throws IOException {
        int bl = v.bitLength();
        if (bl < 32) {
            writeNumber(v.intValue());
        } else if (bl < 64) {
            writeNumber(v.longValue());
        } else {
            writeString(v.toString());
        }
    }

    @Override
    public void writeNumber(final double v) throws IOException {
        writer.writeDouble(v);
    }

    @Override
    public void writeNumber(final float v) throws IOException {
        writeNumber((double)v);
    }

    @Override
    public void writeNumber(final BigDecimal v) throws IOException {
        writer.writeDecimal128(new Decimal128(v));
    }

    @Override
    public void writeNumber(final String encodedValue) throws IOException {
        writeString(encodedValue);
    }

    @Override
    public void writeBoolean(final boolean state) throws IOException {
        writer.writeBoolean(state);
    }

    @Override
    public void writeNull() throws IOException {
        writer.writeNull();
    }

    public void writeBsonObjectId(final ObjectId objectId) {
        writer.writeObjectId(objectId);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    protected void _releaseBuffers() {
        writer.flush();
        // nothing to do
    }

    @Override
    protected void _verifyValueWrite(final String typeMsg) throws IOException {
        // no implementation
    }

}
