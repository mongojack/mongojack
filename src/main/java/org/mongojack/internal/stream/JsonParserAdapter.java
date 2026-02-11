package org.mongojack.internal.stream;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.bson.AbstractBsonReader;
import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.BsonJavaScript;
import org.bson.BsonType;
import org.bson.UuidRepresentation;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.BsonJavaScriptWithScopeCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.PatternCodec;
import org.bson.types.Symbol;
import org.mongojack.internal.MongoJackModule;

import com.mongodb.MongoClientSettings;

import tools.jackson.core.Base64Variant;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.TokenStreamContext;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.Version;
import tools.jackson.core.base.ParserBase;
import tools.jackson.core.exc.InputCoercionException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.io.ContentReference;
import tools.jackson.core.io.IOContext;
import tools.jackson.core.util.SimpleStreamReadContext;

public class JsonParserAdapter extends ParserBase {

    protected final AbstractBsonReader reader;

    protected final PatternCodec patternCodec = new PatternCodec();

    private final AtomicReference<BsonJavaScriptWithScopeCodec> withScopeCodec = new AtomicReference<>();

    protected Object currentValue;

    private final UuidRepresentation uuidRepresentation;

    /**
     * Constructs a new parser
     *
     * @param ctxt the Jackson IO context
     * @param jsonFeatures bit flag composed of bits that indicate which
     *            {@link tools.jackson.core.JsonParser.Feature}s are enabled.
     * @param reader Bson reader to read from
     */
    public JsonParserAdapter(IOContext ctxt, int jsonFeatures, AbstractBsonReader reader, final UuidRepresentation uuidRepresentation) {
        super(ctxt, jsonFeatures);
        this.reader = reader;
        this.uuidRepresentation = uuidRepresentation;
    }

    @Override
    public ObjectCodec getCodec() {
        return _codec;
    }

    @Override
    public void setCodec(ObjectCodec c) {
        _codec = c;
    }

    @Override
    public void close() {
        if (isEnabled(StreamReadFeature.AUTO_CLOSE_SOURCE)) {
            reader.close();
        }
        _closed = true;
    }

    @Override
    public JsonToken nextToken() throws JacksonException {
        return _currToken = _nextToken();
    }

    private JsonToken _nextToken() throws JacksonException {
        currentValue = null;

        while (state() == AbstractBsonReader.State.TYPE) {
            reader.readBsonType();
        }

        switch (state()) {
            case INITIAL:
                reader.readStartDocument();
                return JsonToken.START_OBJECT;
            case NAME:
                ((SimpleStreamReadContext) streamReadContext()).setCurrentName(reader.readName());
                return JsonToken.PROPERTY_NAME;
            case VALUE:
                return toJsonToken(type());
            case END_OF_DOCUMENT:
                reader.readEndDocument();
                return JsonToken.END_OBJECT;
            case END_OF_ARRAY:
                reader.readEndArray();
                return JsonToken.END_ARRAY;
            case DONE:
                return null;
            default:
                throw new StreamReadException(
                        this,
                        "Unknown state " + state(),
                        currentTokenLocation());
        }
    }

    protected JsonToken toJsonToken(BsonType type) throws JacksonException {
        switch (type) {
            case END_OF_DOCUMENT:
                reader.readEndDocument();
                return JsonToken.END_OBJECT;
            case DOCUMENT:
                reader.readStartDocument();
                return JsonToken.START_OBJECT;
            case ARRAY:
                reader.readStartArray();
                return JsonToken.START_ARRAY;
            case STRING:
                currentValue = reader.readString();
                return JsonToken.VALUE_STRING;
            case DOUBLE:
                currentValue = reader.readDouble();
                return JsonToken.VALUE_NUMBER_FLOAT;
            case DECIMAL128:
                currentValue = reader.readDecimal128().bigDecimalValue();
                return JsonToken.VALUE_NUMBER_FLOAT;
            case INT32:
                currentValue = reader.readInt32();
                return JsonToken.VALUE_NUMBER_INT;
            case INT64:
                currentValue = reader.readInt64();
                return JsonToken.VALUE_NUMBER_INT;
            case NULL:
                reader.readNull();
                return JsonToken.VALUE_NULL;
            case UNDEFINED:
                reader.readUndefined();
                return JsonToken.VALUE_NULL;
            case TIMESTAMP:
                currentValue = reader.readTimestamp();
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            case SYMBOL:
                currentValue = new Symbol(reader.readSymbol());
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            case JAVASCRIPT_WITH_SCOPE:
                currentValue = getWithScopeCodec().decode(reader, DecoderContext.builder().build());
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            case JAVASCRIPT:
                currentValue = new BsonJavaScript(reader.readJavaScript());
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            case DB_POINTER:
                currentValue = reader.readDBPointer();
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            case REGULAR_EXPRESSION:
                currentValue = patternCodec.decode(reader, DecoderContext.builder().build());
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            case DATE_TIME:
                currentValue = new Date(reader.readDateTime());
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            case OBJECT_ID:
                currentValue = reader.readObjectId();
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            case BINARY:
                byte subtype = reader.peekBinarySubType();
                final BsonBinary bsonBinary = reader.readBinaryData();
                if (BsonBinarySubType.isUuid(subtype)) {
                    currentValue = bsonBinary.asUuid(uuidRepresentation);
                } else {
                    currentValue = bsonBinary.getData();
                }
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            case MIN_KEY:
                currentValue = "MinKey";
                reader.readMinKey();
                return JsonToken.VALUE_STRING;
            case MAX_KEY:
                currentValue = "MaxKey";
                reader.readMaxKey();
                return JsonToken.VALUE_STRING;
            case BOOLEAN:
                final boolean value = reader.readBoolean();
                currentValue = value;
                return value ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE;
            default:
                throw new StreamReadException(
                        this,
                        "Unknown element type " + type,
                        currentTokenLocation());
        }
    }

    @Override
    public String nextName() throws JacksonException {
        if (nextToken() == JsonToken.PROPERTY_NAME) {
            return streamReadContext().currentName();
        }
        return null;
    }

    @Override
    public String currentName() throws JacksonException {
        if (state() == AbstractBsonReader.State.NAME) {
            return nextName();
        } else if (state() == AbstractBsonReader.State.VALUE) {
            final String currentName = reader.getCurrentName();
            parserContext.setCurrentName(currentName);
            return currentName;
        }
        return streamReadContext().currentName();
    }

    @Override
    public TokenStreamLocation currentTokenLocation() {
        String currentName;
        try {
            currentName = currentName();
        } catch (JacksonException e) {
            currentName = "unknown";
        }
        return new TokenStreamLocation(ContentReference.rawReference(currentName), -1L, -1, -1);
    }

    @Override
    public TokenStreamLocation currentLocation() {
        String currentName;
        try {
            currentName = currentName();
        } catch (JacksonException e) {
            currentName = "unknown";
        }
        return new TokenStreamLocation(ContentReference.rawReference(currentName), -1L, -1, -1);
    }

    @Override
    public String getText() throws JacksonException {
        if (currentToken() == JsonToken.PROPERTY_NAME) {
            return currentName();
        }
        return String.valueOf(currentValue);
    }

    @Override
    public char[] getTextCharacters() throws JacksonException {
        // not very efficient; that's why hasTextCharacters()
        // always returns false
        return getText().toCharArray();
    }

    @Override
    public int getTextLength() throws JacksonException {
        return getText().length();
    }

    @Override
    public int getTextOffset() {
        return 0;
    }

    @Override
    public Number getNumberValue() {
        return (Number) currentValue;
    }

    public Number getNumberValueExact() {
        return getNumberValue();
    }

    @Override
    public Object getNumberValueDeferred() throws JacksonException {
        return getNumberValue();
    }

    @Override
    public JsonParser.NumberType getNumberType() {
        if (currentValue == null) {
            return null;
        }
        if (currentValue instanceof Integer) {
            return NumberType.INT;
        } else if (currentValue instanceof Long) {
            return NumberType.LONG;
        } else if (currentValue instanceof BigInteger) {
            return NumberType.BIG_INTEGER;
        } else if (currentValue instanceof Float) {
            return NumberType.FLOAT;
        } else if (currentValue instanceof Double) {
            return NumberType.DOUBLE;
        } else if (currentValue instanceof BigDecimal) {
            return NumberType.BIG_DECIMAL;
        }
        return null;
    }

    @Override
    public int getIntValue() {
        return ((Number) currentValue).intValue();
    }

    @Override
    public long getLongValue() {
        return ((Number) currentValue).longValue();
    }

    @Override
    public BigInteger getBigIntegerValue() {
        Number n = getNumberValue();
        if (n == null) {
            return null;
        }
        if (n instanceof Byte || n instanceof Integer ||
                n instanceof Long || n instanceof Short) {
            return BigInteger.valueOf(n.longValue());
        } else if (n instanceof Double || n instanceof Float) {
            return BigDecimal.valueOf(n.doubleValue()).toBigInteger();
        }
        return new BigInteger(n.toString());
    }

    @Override
    public float getFloatValue() {
        return ((Number) currentValue).floatValue();
    }

    @Override
    public double getDoubleValue() {
        return ((Number) currentValue).doubleValue();
    }

    @Override
    public BigDecimal getDecimalValue() {
        Number n = getNumberValue();
        if (n == null) {
            return null;
        }
        if (n instanceof Byte || n instanceof Integer ||
                n instanceof Long || n instanceof Short) {
            return BigDecimal.valueOf(n.longValue());
        } else if (n instanceof Double || n instanceof Float) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return new BigDecimal(n.toString());
    }

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) {
        return (byte[]) currentValue;
    }

    @Override
    public Object getEmbeddedObject() {
        return currentValue;
    }

    @Override
    protected void _handleEOF() throws StreamReadException {
        _reportInvalidEOF();
    }

    @Override
    protected void _closeInput() {
        reader.close();
    }

    private AbstractBsonReader.State state() {
        return reader.getState();
    }

    private BsonType type() {
        return reader.getCurrentBsonType();
    }

    private BsonJavaScriptWithScopeCodec getWithScopeCodec() {
        return withScopeCodec.updateAndGet((existing) -> {
            if (existing == null) {
                return new BsonJavaScriptWithScopeCodec(new BsonDocumentCodec(MongoClientSettings.getDefaultCodecRegistry()));
            }
            return existing;
        });
    }
}
