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

import java.lang.reflect.AnnotatedElement;
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
            getIdWriter(t).accept(new BsonObjectId());
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
        return getIdReader(t).get();
    }

    private Supplier<BsonValue> getIdReader(final T t) {
        final Class<?> documentClass = t.getClass();
        final Optional<Method> maybeIdGetter = getIdGetter(documentClass);
        if (maybeIdGetter.isPresent()) {
            Method getter = maybeIdGetter.get();
            getter.setAccessible(true);
            return () -> {
                try {
                    return constructIdValue(getter.invoke(t), maybeIdGetter);
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
                        return constructIdValue(field.get(t), maybeField);
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

    private Consumer<BsonObjectId> getIdWriter(final T t) {
        final Class<?> documentClass = t.getClass();
        final Optional<Method> maybeSetter = getIdSetter(documentClass);
        if (maybeSetter.isPresent()) {
            Method setter = maybeSetter.get();
            setter.setAccessible(true);
            return (value) -> {
                try {
                    if (value != null) {
                        setter.invoke(t, extractIdValue(value, setter.getParameterTypes()[0]));
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
                        field.set(t, extractIdValue(value, field.getType()));
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

    private static Optional<Field> getIdField(final Class<?> documentClass) {
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

    private static Optional<Method> getIdGetter(final Class<?> documentClass) {
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

    private static Optional<Method> getIdSetter(final Class<?> documentClass) {
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

    /**
     * This is only used for GENERATING an object id, and since we are only interested in auto-generating ObjectIds, then we only have to
     * deal with object ids here.
     *
     * @param value
     * @param valueType
     * @return
     */
    private static Object extractIdValue(BsonObjectId value, Class<?> valueType) {
        if (String.class.equals(valueType)) {
            return value.asObjectId().getValue().toHexString();
        } else if (ObjectId.class.equals(valueType)) {
            return value.asObjectId().getValue();
        } else if (byte[].class.equals(valueType)) {
            return value.asObjectId().getValue().toByteArray();
        } else if (Byte[].class.equals(valueType)) {
            final byte[] inputArray = value.asObjectId().getValue().toByteArray();
            Byte[] outputArray = new Byte[inputArray.length];
            for (int i = 0; i < inputArray.length; i++) {
                outputArray[i] = inputArray[i];
            }
            return outputArray;
        }
        throw new IllegalArgumentException("Unsupported ID type: " + value.getClass());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static BsonValue constructIdValue(Object value, Optional<? extends AnnotatedElement> element) {
        if (element.isPresent() && element.get().isAnnotationPresent(org.mongojack.ObjectId.class)) {
            if (value instanceof String) {
                return new BsonObjectId(new ObjectId((String) value));
            }
            if (value instanceof byte[]) {
                return new BsonObjectId(new ObjectId((byte[]) value));
            }
            if (value instanceof Byte[]) {
                final Byte[] inputArray = (Byte[]) value;
                byte[] outputArray = new byte[inputArray.length];
                for (int i = 0; i < inputArray.length; i++) {
                    outputArray[i] = inputArray[i];
                }
                return new BsonObjectId(new ObjectId(outputArray));
            }
        }
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
        throw new IllegalArgumentException(String.format("Unsupported ID type: %s", value.getClass()));
    }

    public static Optional<? extends AnnotatedElement> getIdElement(final Class<?> documentClass) {
        final Optional<Method> maybeIdGetter = getIdGetter(documentClass);
        if (maybeIdGetter.isPresent()) {
            return maybeIdGetter;
        }
        return getIdField(documentClass);
    }

}
