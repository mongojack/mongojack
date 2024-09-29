package org.mongojack.internal.stream;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.bson.BsonDecimal128;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonNull;
import org.bson.BsonObjectId;
import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.UuidRepresentation;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.OverridableUuidRepresentationCodec;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.mongojack.JacksonCodecRegistry;
import org.mongojack.internal.AnnotationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
public class JacksonCodec<T> implements Codec<T>, CollectibleCodec<T>, OverridableUuidRepresentationCodec<T> {

    private final static Logger logger = LoggerFactory.getLogger(JacksonCodec.class);

    private final JacksonEncoder<T> encoder;
    private final JacksonDecoder<T> decoder;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Class<?>, Optional<BeanPropertyDefinition>> serializationBPDCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Optional<BeanPropertyDefinition>> deSerializationBPDCache = new ConcurrentHashMap<>();
    private final JacksonCodecRegistry jacksonCodecRegistry;

    public JacksonCodec(
        JacksonEncoder<T> encoder,
        JacksonDecoder<T> decoder,
        final ObjectMapper objectMapper,
        JacksonCodecRegistry jacksonCodecRegistry
        ) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.objectMapper = objectMapper;
        this.jacksonCodecRegistry = jacksonCodecRegistry;
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

    @Override
    public Codec<T> withUuidRepresentation(final UuidRepresentation uuidRepresentation) {
        return new JacksonCodec<>(
            encoder.withUuidRepresentation(uuidRepresentation),
            decoder.withUuidRepresentation(uuidRepresentation),
            objectMapper,
            jacksonCodecRegistry
        );
    }

    private Supplier<BsonValue> getIdReader(final T t) {
        final Optional<BeanPropertyDefinition> maybeBpd = getIdElementDeserializationDescription(t.getClass());
        return maybeBpd.<Supplier<BsonValue>>map(beanPropertyDefinition -> () -> {
            try {
                return constructIdValue(beanPropertyDefinition.getAccessor().getValue(t), maybeBpd);
            } catch (Exception e) {
                logger.warn("Suppressed error attempting to get reader for object id in " + t.getClass(), e);
                return BsonNull.VALUE;
            }
        }).orElseGet(() -> () -> BsonNull.VALUE);
    }

    private Consumer<BsonObjectId> getIdWriter(final T t) {
        final Optional<BeanPropertyDefinition> maybeBpd = getIdElementSerializationDescription(t.getClass());
        return maybeBpd.<Consumer<BsonObjectId>>map(beanPropertyDefinition -> (bsonObjectId) -> {
            try {
                if (bsonObjectId != null) {
                    AnnotatedMember mutator = beanPropertyDefinition.getNonConstructorMutator();
                    Class<?> rawType = mutator instanceof AnnotatedWithParams ? ((AnnotatedWithParams) mutator).getRawParameterType(0)
                        : beanPropertyDefinition.getRawPrimaryType();

                    mutator.setValue(
                        t,
                        extractIdValue(bsonObjectId, rawType)
                    );
                }
            } catch (Exception e) {
                logger.warn("Suppressed error attempting to get writer for object id in " + t.getClass(), e);
            }
        }).orElseGet(() -> (bsonObjectId) -> {
        });
    }

    /**
     * This is only used for GENERATING an object id, and since we are only interested in auto-generating ObjectIds, then we only have to
     * deal with object ids here.
     *
     * @param value
     * @param valueType
     * @return
     */
    private Object extractIdValue(BsonObjectId value, Class<?> valueType) {
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
        throw new IllegalArgumentException("Unsupported ID generation type: " + valueType);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public BsonValue constructIdValue(Object value, Optional<BeanPropertyDefinition> element) {
        if (element.isPresent() && element.get().getPrimaryMember().hasAnnotation(org.mongojack.ObjectId.class)) {
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
        } else {
            final BsonDocument doc = new BsonDocument();
            try (BsonDocumentWriter bdw = new BsonDocumentWriter(doc)) {
                bdw.writeStartDocument();
                bdw.writeName("_id");
                final Codec codec = jacksonCodecRegistry.get(value.getClass());
                codec.encode(bdw, value, EncoderContext.builder().build());
                bdw.writeEndDocument();
                return bdw.getDocument().get("_id");
            }
        }
    }

    public Optional<BeanPropertyDefinition> getIdElementDeserializationDescription(final Class<?> documentClass) {
        return deSerializationBPDCache.computeIfAbsent(
            documentClass,
            (documentClazz) -> {
                final DeserializationConfig deserializationConfig = objectMapper.getDeserializationConfig();
                final BeanDescription beanDescription = deserializationConfig.introspect(deserializationConfig.constructType(documentClass));

                final Optional<BeanPropertyDefinition> found = beanDescription.findProperties().stream()
                    .filter(
                        bpd -> ("_id".equals(bpd.getName()) ||
                                AnnotationHelper.hasIdAnnotation(bpd.getPrimaryMember())) &&
                                bpd.getAccessor() != null
                    )
                    .findFirst();

                found.ifPresent(
                    bpd -> {
                        if (deserializationConfig.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                            bpd.getAccessor().fixAccess(true);
                        }
                    }
                );

                return found;
            }
        );
    }

    public Optional<BeanPropertyDefinition> getIdElementSerializationDescription(final Class<?> documentClass) {
        return serializationBPDCache.computeIfAbsent(
            documentClass,
            (documentClazz) -> {
                final SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
                final BeanDescription beanDescription = serializationConfig.introspect(serializationConfig.constructType(documentClass));

                final Optional<BeanPropertyDefinition> found = beanDescription.findProperties().stream()
                    .filter(bpd -> bpd.getPrimaryMember() != null)
                    .filter(
                        bpd -> ("_id".equals(bpd.getName()) ||
                                AnnotationHelper.hasIdAnnotation(bpd.getPrimaryMember())) &&
                                bpd.getMutator() != null
                    )
                    .findFirst();

                found.ifPresent(
                    bpd -> {
                        if (serializationConfig.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                            bpd.getMutator().fixAccess(true);
                        }
                    }
                );

                return found;
            }
        );
    }

}
