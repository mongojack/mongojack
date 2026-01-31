package org.mongojack.internal.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Function;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.cursor.TimeoutMode;
import com.mongodb.client.model.Collation;
import com.mongodb.lang.Nullable;
import org.bson.BsonValue;
import org.bson.conversions.Bson;
import org.mongojack.InitializationRequiredForTransformation;
import org.mongojack.JacksonCodecRegistry;
import org.mongojack.SerializationOptions;

import java.util.Collection;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Wraps a DistinctIterable so we can map filters
 * <p>
 * {@inheritDoc}
 */
public class DistinctIterableDecorator<TResult> implements DistinctIterable<TResult> {

    private final DistinctIterable<TResult> delegate;
    private final ObjectMapper objectMapper;
    private final JavaType type;
    private final JacksonCodecRegistry codecRegistry;
    private final SerializationOptions serializationOptions;

    public DistinctIterableDecorator(
        final DistinctIterable<TResult> delegate,
        final ObjectMapper objectMapper,
        final JavaType type,
        final JacksonCodecRegistry codecRegistry,
        final SerializationOptions serializationOptions
    ) {
        this.delegate = delegate;
        this.objectMapper = objectMapper;
        this.type = type;
        this.codecRegistry = codecRegistry;
        this.serializationOptions = serializationOptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DistinctIterable<TResult> filter(final Bson filter) {
        if (filter instanceof InitializationRequiredForTransformation) {
            ((InitializationRequiredForTransformation) filter).initialize(objectMapper, type, codecRegistry);
            return delegate.filter(filter);
        }
        if (serializationOptions.isSimpleFilterSerialization()) {
            return delegate.filter(filter);
        }
        return delegate.filter(DocumentSerializationUtils.serializeFilter(objectMapper, type, filter, codecRegistry));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DistinctIterable<TResult> maxTime(final long maxTime, final TimeUnit timeUnit) {
        return delegate.maxTime(maxTime, timeUnit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DistinctIterable<TResult> batchSize(final int batchSize) {
        return delegate.batchSize(batchSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DistinctIterable<TResult> collation(final Collation collation) {
        return delegate.collation(collation);
    }

    @Override
    public DistinctIterable<TResult> comment(final String s) {
        return delegate.comment(s);
    }

    @Override
    public DistinctIterable<TResult> comment(final BsonValue bsonValue) {
        return delegate.comment(bsonValue);
    }

    @Override
    public DistinctIterable<TResult> hint(Bson bson) {
        return delegate.hint(bson);
    }

    @Override
    public DistinctIterable<TResult> hintString(String s) {
        return delegate.hintString(s);
    }

    @Override
    public DistinctIterable<TResult> timeoutMode(TimeoutMode timeoutMode) {
        return delegate.timeoutMode(timeoutMode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoCursor<TResult> iterator() {
        return delegate.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoCursor<TResult> cursor() {
        return delegate.cursor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TResult first() {
        return delegate.first();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <U> MongoIterable<U> map(final Function<TResult, U> mapper) {
        return delegate.map(mapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <A extends Collection<? super TResult>> A into(final A target) {
        return delegate.into(target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forEach(final Consumer<? super TResult> action) {
        delegate.forEach(action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Spliterator<TResult> spliterator() {
        return delegate.spliterator();
    }
}
