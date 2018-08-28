package org.mongojack.internal.stream;

import org.bson.BsonDecimal128;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonNull;
import org.bson.BsonObjectId;
import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
public class JacksonCodec<T> implements Codec<T>, CollectibleCodec<T> {

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

    @Override
    public T generateIdIfAbsentFromDocument(final T t) {
        if (!documentHasId(t)) {
            getWriter(t).accept(new BsonObjectId());
        }
        return t;
    }

    @Override
    public boolean documentHasId(final T t) {
        final BsonValue readValue = getDocumentId(t);
        return readValue != null && !readValue.isNull();
    }

    @Override
    public BsonValue getDocumentId(final T t) {
        return getReader(t).get();
    }

    protected Supplier<BsonValue> getReader(final T t) {
        final Class<?> documentClass = t.getClass();
        final Optional<Method> maybeIdGetter = getIdGetter(documentClass);
        if (maybeIdGetter.isPresent()) {
            Method getter = maybeIdGetter.get();
            getter.setAccessible(true);
            return () -> {
                try {
                    return constructValue(getter.invoke(t));
                } catch (Exception e) {
                    e.printStackTrace();
                    return BsonNull.VALUE;
                }
            };
        } else {
            final Optional<Field> maybeField = getIdField(documentClass);
            if (maybeField.isPresent()) {
                Field field = maybeField.get();
                field.setAccessible(true);
                return () -> {
                    try {
                        return constructValue(field.get(t));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return BsonNull.VALUE;
                    }
                };
            } else {
                return () -> BsonNull.VALUE;
            }
        }
    }

    protected Consumer<BsonObjectId> getWriter(final T t) {
        final Class<?> documentClass = t.getClass();
        final Optional<Method> maybeSetter = getIdSetter(documentClass);
        if (maybeSetter.isPresent()) {
            Method setter = maybeSetter.get();
            setter.setAccessible(true);
            return (value) -> {
                try {
                    if (value != null) {
                        setter.invoke(t, extractValue(value, setter.getParameterTypes()[0]));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        } else {
            final Optional<Field> maybeField = getIdField(documentClass);
            if (maybeField.isPresent()) {
                Field field = maybeField.get();
                field.setAccessible(true);
                return (value) -> {
                    try {
                        field.set(t, extractValue(value, field.getType()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
            } else {
                return (value) -> {
                };
            }
        }
    }

    protected Optional<Field> getIdField(final Class<?> documentClass) {
        Field[] fields = documentClass.getDeclaredFields();
        Optional<Field> maybeField = Arrays.stream(fields)
            .filter(field -> field.isAnnotationPresent(javax.persistence.Id.class) ||
                field.isAnnotationPresent(org.mongojack.Id.class) ||
                field.getName().equals("_id"))
            .findFirst();
        if (maybeField.isPresent()) {
            return maybeField;
        }

        Class<?> superClass = documentClass.getSuperclass();
        if (superClass != null && !Object.class.equals(superClass)) {
            return getIdField(superClass);
        }
        return Optional.empty();
    }

    protected Optional<Method> getIdGetter(final Class<?> documentClass) {
        Method[] methods = documentClass.getDeclaredMethods();
        Optional<Method> maybeGetter = Arrays.stream(methods)
            .filter(method -> method.getName().startsWith("get") &&
                method.getParameterCount() == 0 &&
                (method.isAnnotationPresent(javax.persistence.Id.class) ||
                    method.isAnnotationPresent(org.mongojack.Id.class) ||
                    method.getName().equals("get_id")))
            .findFirst();
        if (maybeGetter.isPresent()) {
            return maybeGetter;
        }

        Class<?> superClass = documentClass.getSuperclass();
        if (superClass != null && !Object.class.equals(superClass)) {
            return getIdGetter(superClass);
        }
        return Optional.empty();
    }

    protected Optional<Method> getIdSetter(final Class<?> documentClass) {
        Method[] methods = documentClass.getDeclaredMethods();
        Optional<Method> maybeSetter = Arrays.stream(methods)
            .filter(method -> method.getName().startsWith("set") &&
                method.getParameterCount() == 1 &&
                (method.isAnnotationPresent(javax.persistence.Id.class) ||
                    method.isAnnotationPresent(org.mongojack.Id.class) ||
                    method.getName().equals("set_id")))
            .findFirst();
        if (maybeSetter.isPresent()) {
            return maybeSetter;
        }

        Class<?> superClass = documentClass.getSuperclass();
        if (superClass != null && !Object.class.equals(superClass)) {
            return getIdSetter(superClass);
        }
        return Optional.empty();
    }

    protected Object extractValue(BsonObjectId value, Class<?> valueType) {
        if (String.class.equals(valueType)) {
            return value.asObjectId().getValue().toHexString();
        } else if (ObjectId.class.equals(valueType)) {
            return value.asObjectId().getValue();
        }
        throw new IllegalArgumentException("Unsupported ID type: " + value.getClass());
    }

    public static Object extractValueEx(BsonValue value) {
        switch (value.getBsonType()) {
            case DOUBLE:
                return value.asDouble().getValue();
            case STRING:
                return value.asString().getValue();
            case OBJECT_ID:
                return value.asObjectId().getValue();
            case INT32:
                return value.asInt32().getValue();
            case INT64:
                return value.asInt64().getValue();
            case DECIMAL128:
                return value.asDecimal128().getValue();
            case NULL:
                return null;
        }
        throw new IllegalArgumentException("Unsupported ID type: " + value.getClass());
    }

    protected BsonValue constructValue(Object value) {
        if (value == null) {
            return BsonNull.VALUE;
        } else if (value instanceof Double) {
            return new BsonDouble((Double) value);
        } else if (value instanceof String) {
            return new BsonString((String) value);
        } else if (value instanceof ObjectId) {
            return new BsonObjectId((ObjectId) value);
        } else if (value instanceof Integer) {
            return new BsonInt32((Integer) value);
        } else if (value instanceof Long) {
            return new BsonInt64((Long) value);
        } else if (value instanceof Decimal128) {
            return new BsonDecimal128((Decimal128) value);
        }
        throw new IllegalArgumentException("Unsupported ID type: " + value.getClass());
    }

}
