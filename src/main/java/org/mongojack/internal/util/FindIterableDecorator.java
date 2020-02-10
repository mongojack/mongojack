package org.mongojack.internal.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.CursorType;
import com.mongodb.Function;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Collation;
import com.mongodb.lang.Nullable;
import org.bson.conversions.Bson;
import org.mongojack.InitializationRequiredForTransformation;
import org.mongojack.JacksonCodecRegistry;

import java.util.Collection;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Wraps a FindIterable so we can map the incoming filters.
 *
 * @param <TResult> the type this iterable produces
 */
public class FindIterableDecorator<TResult> implements FindIterable<TResult> {

    private final FindIterable<TResult> delegate;
    private final ObjectMapper objectMapper;
    private final JavaType type;
    private final JacksonCodecRegistry codecRegistry;

    public FindIterableDecorator(
        final FindIterable<TResult> delegate,
        final ObjectMapper objectMapper,
        final JavaType type,
        final JacksonCodecRegistry codecRegistry
    ) {
        this.delegate = delegate;
        this.objectMapper = objectMapper;
        this.type = type;
        this.codecRegistry = codecRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> filter(final Bson filter) {
        if (filter instanceof InitializationRequiredForTransformation) {
            ((InitializationRequiredForTransformation) filter).initialize(objectMapper, type, codecRegistry);
            return delegate.filter(filter);
        }
        return delegate.filter(DocumentSerializationUtils.serializeFilter(objectMapper, type, filter, codecRegistry));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> limit(final int limit) {
        return delegate.limit(limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> skip(final int skip) {
        return delegate.skip(skip);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> maxTime(final long maxTime, final TimeUnit timeUnit) {
        return delegate.maxTime(maxTime, timeUnit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> maxAwaitTime(final long maxAwaitTime, final TimeUnit timeUnit) {
        return delegate.maxAwaitTime(maxAwaitTime, timeUnit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> projection(final Bson projection) {
        return delegate.projection(projection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> sort(final Bson sort) {
        return delegate.sort(sort);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> noCursorTimeout(final boolean noCursorTimeout) {
        return delegate.noCursorTimeout(noCursorTimeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> oplogReplay(final boolean oplogReplay) {
        return delegate.oplogReplay(oplogReplay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> partial(final boolean partial) {
        return delegate.partial(partial);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> cursorType(final CursorType cursorType) {
        return delegate.cursorType(cursorType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> batchSize(final int batchSize) {
        return delegate.batchSize(batchSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> collation(final Collation collation) {
        return delegate.collation(collation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> comment(final String comment) {
        return delegate.comment(comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> hint(final Bson hint) {
        return delegate.hint(hint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> hintString(final String hint) {
        return delegate.hintString(hint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> max(final Bson max) {
        return delegate.max(max);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> min(final Bson min) {
        return delegate.min(min);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> returnKey(final boolean returnKey) {
        return delegate.returnKey(returnKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TResult> showRecordId(final boolean showRecordId) {
        return delegate.showRecordId(showRecordId);
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
