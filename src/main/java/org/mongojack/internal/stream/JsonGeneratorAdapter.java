package org.mongojack.internal.stream;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.bson.BsonBinary;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.UuidRepresentation;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.mongojack.internal.MongoJackModule;
import org.mongojack.internal.util.DocumentSerializationUtils;

import tools.jackson.core.Base64Variant;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.StreamWriteCapability;
import tools.jackson.core.TokenStreamContext;
import tools.jackson.core.Version;
import tools.jackson.core.base.GeneratorBase;
import tools.jackson.core.io.IOContext;
import tools.jackson.core.util.JacksonFeatureSet;

public class JsonGeneratorAdapter extends GeneratorBase {

    protected final BsonWriter writer;
    protected final UuidRepresentation uuidRepresentation;

    protected JsonGeneratorAdapter(
            final ObjectWriteContext writeCtxt,
            final IOContext ioCtxt,
            final int streamWriteFeatures,
            final BsonWriter writer,
            final UuidRepresentation uuidRepresentation) {
        super(writeCtxt, ioCtxt, streamWriteFeatures);
        this.writer = writer;
        this.uuidRepresentation = uuidRepresentation;
    }

    @Override
    public JsonGenerator writeStartArray() throws JacksonException {
        writer.writeStartArray();
        return this;
    }

    @Override
    public JsonGenerator writeEndArray() throws JacksonException {
        writer.writeEndArray();
        return this;
    }

    @Override
    public JsonGenerator writeStartObject() throws JacksonException {
        writer.writeStartDocument();
        return this;
    }

    @Override
    public JsonGenerator writeEndObject() throws JacksonException {
        writer.writeEndDocument();
        return this;
    }

    @Override
    public JsonGenerator writeName(final String name) throws JacksonException {
        writer.writeName(name);
        return this;
    }

    @Override
    public JsonGenerator writeString(final String text) throws JacksonException {
        writer.writeString(text);
        return this;
    }

    @Override
    public JsonGenerator writeString(final char[] text, final int offset, final int len) throws JacksonException {
        writer.writeString(new String(text, offset, len));
        return this;
    }

    @Override
    public JsonGenerator writeRawUTF8String(final byte[] text, final int offset, final int length) throws JacksonException {
        writer.writeString(new String(text, offset, length, StandardCharsets.UTF_8));
        return this;
    }

    @Override
    public JsonGenerator writeUTF8String(final byte[] text, final int offset, final int length) throws JacksonException {
        writer.writeString(new String(text, offset, length, StandardCharsets.UTF_8));
        return this;
    }

    @Override
    public JsonGenerator writeRaw(final String text) throws JacksonException {
        throw new UnsupportedOperationException("writeRaw not supported");
    }

    @Override
    public JsonGenerator writeRaw(final String text, final int offset, final int len) throws JacksonException {
        throw new UnsupportedOperationException("writeRaw not supported");
    }

    @Override
    public JsonGenerator writeRaw(final char[] text, final int offset, final int len) throws JacksonException {
        throw new UnsupportedOperationException("writeRaw not supported");
    }

    @Override
    public JsonGenerator writeRaw(final char c) throws JacksonException {
        throw new UnsupportedOperationException("writeRaw not supported");
    }

    @Override
    public JsonGenerator writeBinary(final Base64Variant bv, final byte[] data, final int offset, final int len) throws JacksonException {
        writer.writeBinaryData(new BsonBinary(Arrays.copyOfRange(data, offset, len)));
        return this;
    }

    @Override
    public JsonGenerator writeNumber(final short v) throws JacksonException {
        writer.writeInt32(v);
        return this;
    }

    @Override
    public JsonGenerator writeNumber(final int v) throws JacksonException {
        writer.writeInt32(v);
        return this;
    }

    @Override
    public JsonGenerator writeNumber(final long v) throws JacksonException {
        writer.writeInt64(v);
        return this;
    }

    @Override
    public JsonGenerator writeNumber(final BigInteger v) throws JacksonException {
        int bl = v.bitLength();
        if (bl < 32) {
            writeNumber(v.intValue());
        } else if (bl < 64) {
            writeNumber(v.longValue());
        } else {
            writeString(v.toString());
        }
        return this;
    }

    @Override
    public JsonGenerator writeNumber(final double v) throws JacksonException {
        writer.writeDouble(v);
        return this;
    }

    @Override
    public JsonGenerator writeNumber(final float v) throws JacksonException {
        writeNumber((double) v);
        return this;
    }

    @Override
    public JsonGenerator writeNumber(final BigDecimal v) throws JacksonException {
        writer.writeDecimal128(new Decimal128(v));
        return this;
    }

    @Override
    public JsonGenerator writeNumber(final String encodedValue) throws JacksonException {
        writeString(encodedValue);
        return this;
    }

    @Override
    public JsonGenerator writeBoolean(final boolean state) throws JacksonException {
        writer.writeBoolean(state);
        return this;
    }

    @Override
    public JsonGenerator writeNull() throws JacksonException {
        writer.writeNull();
        return this;
    }

    public JsonGenerator writeBsonObjectId(final ObjectId objectId) {
        writer.writeObjectId(objectId);
        return this;
    }

    public JsonGenerator writeBsonValue(final BsonValue value) {
        if (!DocumentSerializationUtils.writeKnownType(value, writer)) {
            throw new IllegalStateException("Asked to write unknown type " + value.getClass());
        }
        return this;
    }

    @Override
    public void flush() throws JacksonException {
        writer.flush();
    }

    @Override
    protected void _releaseBuffers() {
        writer.flush();
        // nothing to do
    }

    @Override
    protected void _verifyValueWrite(final String typeMsg) throws JacksonException {
        // no implementation
    }

}
