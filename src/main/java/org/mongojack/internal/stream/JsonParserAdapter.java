package org.mongojack.internal.stream;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.base.ParserBase;
import com.fasterxml.jackson.core.io.IOContext;
import com.mongodb.MongoClientSettings;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public class JsonParserAdapter extends ParserBase {

    protected ObjectCodec _codec;

    protected final AbstractBsonReader reader;

    protected final PatternCodec patternCodec = new PatternCodec();

    protected final BsonJavaScriptWithScopeCodec withScopeCodec = new BsonJavaScriptWithScopeCodec(new BsonDocumentCodec(MongoClientSettings.getDefaultCodecRegistry()));

    protected Object currentValue;

    private final UuidRepresentation uuidRepresentation;

    /**
     * Constructs a new parser
     *
     * @param ctxt         the Jackson IO context
     * @param jsonFeatures bit flag composed of bits that indicate which
     *                     {@link com.fasterxml.jackson.core.JsonParser.Feature}s are enabled.
     * @param reader       Bson reader to read from
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
        if (isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE)) {
            reader.close();
        }
        _closed = true;
    }

    @Override
    public JsonToken nextToken() throws IOException {
        return _currToken = _nextToken();
    }

    private JsonToken _nextToken() throws IOException {
        currentValue = null;

        while (state() == AbstractBsonReader.State.TYPE) {
            reader.readBsonType();
        }

        switch (state()) {
            case INITIAL:
                reader.readStartDocument();
                return JsonToken.START_OBJECT;
            case NAME:
                getParsingContext().setCurrentName(reader.readName());
                return JsonToken.FIELD_NAME;
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
                throw new JsonParseException(
                    this,
                    "Unknown state " + state(),
                    getTokenLocation()
                );
        }
    }

    protected JsonToken toJsonToken(BsonType type) throws IOException {
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
                return nextToken();
            case TIMESTAMP:
                currentValue = reader.readTimestamp();
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            case SYMBOL:
                currentValue = new Symbol(reader.readSymbol());
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            case JAVASCRIPT_WITH_SCOPE:
                currentValue = withScopeCodec.decode(reader, DecoderContext.builder().build());
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
                return JsonToken.VALUE_STRING;
            case MAX_KEY:
                currentValue = "MaxKey";
                return JsonToken.VALUE_STRING;
            case BOOLEAN:
                final boolean value = reader.readBoolean();
                currentValue = value;
                return value ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE;
            default:
                throw new JsonParseException(
                    this,
                    "Unknown element type " + type,
                    getTokenLocation()
                );
        }
    }

    @Override
    public String nextFieldName() throws IOException {
        if (nextToken() == JsonToken.FIELD_NAME) {
            return getParsingContext().getCurrentName();
        }
        return null;
    }

    @Override
    public String getCurrentName() throws IOException {
        if (state() == AbstractBsonReader.State.NAME) {
            return nextFieldName();
        } else if (state() == AbstractBsonReader.State.VALUE) {
            final String currentName = reader.getCurrentName();
            getParsingContext().setCurrentName(currentName);
            return currentName;
        }
        return getParsingContext().getCurrentName();
    }

    @Override
    public JsonLocation getTokenLocation() {
        String currentName;
        try {
            currentName = getCurrentName();
        } catch (IOException e) {
            currentName = "unknown";
        }
        return new JsonLocation(currentName, -1L, -1, -1);
    }

    @Override
    public JsonLocation getCurrentLocation() {
        String currentName;
        try {
            currentName = getCurrentName();
        } catch (IOException e) {
            currentName = "unknown";
        }
        return new JsonLocation(currentName, -1L, -1, -1);
    }

    @Override
    public String getText() throws IOException {
        if (currentToken() == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        return String.valueOf(currentValue);
    }

    @Override
    public char[] getTextCharacters() throws IOException {
        //not very efficient; that's why hasTextCharacters()
        //always returns false
        return getText().toCharArray();
    }

    @Override
    public int getTextLength() throws IOException {
        return getText().length();
    }

    @Override
    public int getTextOffset() {
        return 0;
    }

    @Override
    public boolean hasTextCharacters() {
        //getTextCharacters is obviously not the most efficient way
        return false;
    }

    @Override
    public Number getNumberValue() {
        return (Number) currentValue;
    }

    @Override
    public Number getNumberValueExact() {
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
    protected void _handleEOF() throws JsonParseException {
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

}
