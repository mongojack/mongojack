package org.mongojack.internal.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Block;
import com.mongodb.CursorType;
import com.mongodb.Function;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Collation;
import com.mongodb.lang.Nullable;
import org.bson.conversions.Bson;
import org.mongojack.JacksonCodecRegistry;

import java.util.Collection;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Wraps a FindIterable so we can map the incoming filters.
 *
 * @param <TResult>
 */
public class FindIterableDelegate<TResult> implements FindIterable<TResult> {

    private final FindIterable<TResult> delegate;
    private final ObjectMapper objectMapper;
    private final JavaType type;
    private final JacksonCodecRegistry codecRegistry;

    public FindIterableDelegate(
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

    @Override
    public FindIterable<TResult> filter(final Bson filter) {
        if (filter instanceof InitializationRequiredForTransformation) {
            ((InitializationRequiredForTransformation) filter).initialize(objectMapper, type, codecRegistry);
            return delegate.filter(filter);
        }
        return delegate.filter(DocumentSerializationUtils.serializeFilter(objectMapper, type, filter, codecRegistry));
    }

    @Override
    public FindIterable<TResult> limit(final int limit) {
        return delegate.limit(limit);
    }

    @Override
    public FindIterable<TResult> skip(final int skip) {
        return delegate.skip(skip);
    }

    @Override
    public FindIterable<TResult> maxTime(final long maxTime, final TimeUnit timeUnit) {
        return delegate.maxTime(maxTime, timeUnit);
    }

    @Override
    public FindIterable<TResult> maxAwaitTime(final long maxAwaitTime, final TimeUnit timeUnit) {
        return delegate.maxAwaitTime(maxAwaitTime, timeUnit);
    }

    @Override
    @Deprecated
    public FindIterable<TResult> modifiers(final Bson modifiers) {
        return delegate.modifiers(modifiers);
    }

    @Override
    public FindIterable<TResult> projection(final Bson projection) {
        return delegate.projection(projection);
    }

    @Override
    public FindIterable<TResult> sort(final Bson sort) {
        return delegate.sort(sort);
    }

    @Override
    public FindIterable<TResult> noCursorTimeout(final boolean noCursorTimeout) {
        return delegate.noCursorTimeout(noCursorTimeout);
    }

    @Override
    public FindIterable<TResult> oplogReplay(final boolean oplogReplay) {
        return delegate.oplogReplay(oplogReplay);
    }

    @Override
    public FindIterable<TResult> partial(final boolean partial) {
        return delegate.partial(partial);
    }

    @Override
    public FindIterable<TResult> cursorType(final CursorType cursorType) {
        return delegate.cursorType(cursorType);
    }

    @Override
    public FindIterable<TResult> batchSize(final int batchSize) {
        return delegate.batchSize(batchSize);
    }

    @Override
    public FindIterable<TResult> collation(final Collation collation) {
        return delegate.collation(collation);
    }

    @Override
    public FindIterable<TResult> comment(final String comment) {
        return delegate.comment(comment);
    }

    @Override
    public FindIterable<TResult> hint(final Bson hint) {
        return delegate.hint(hint);
    }

    @Override
    public FindIterable<TResult> hintString(final String hint) {
        return delegate.hintString(hint);
    }

    @Override
    public FindIterable<TResult> max(final Bson max) {
        return delegate.max(max);
    }

    @Override
    public FindIterable<TResult> min(final Bson min) {
        return delegate.min(min);
    }

    @Override
    @Deprecated
    public FindIterable<TResult> maxScan(final long maxScan) {
        return delegate.maxScan(maxScan);
    }

    @Override
    public FindIterable<TResult> returnKey(final boolean returnKey) {
        return delegate.returnKey(returnKey);
    }

    @Override
    public FindIterable<TResult> showRecordId(final boolean showRecordId) {
        return delegate.showRecordId(showRecordId);
    }

    @Override
    @Deprecated
    public FindIterable<TResult> snapshot(final boolean snapshot) {
        return delegate.snapshot(snapshot);
    }

    @Override
    public MongoCursor<TResult> iterator() {
        return delegate.iterator();
    }

    @Override
    public MongoCursor<TResult> cursor() {
        return delegate.cursor();
    }

    @Override
    @Nullable
    public TResult first() {
        return delegate.first();
    }

    @Override
    public <U> MongoIterable<U> map(final Function<TResult, U> mapper) {
        return delegate.map(mapper);
    }

    @Override
    @Deprecated
    public void forEach(final Block<? super TResult> block) {
        delegate.forEach(block);
    }

    @Override
    public <A extends Collection<? super TResult>> A into(final A target) {
        return delegate.into(target);
    }

    @Override
    public void forEach(final Consumer<? super TResult> action) {
        delegate.forEach(action);
    }

    @Override
    public Spliterator<TResult> spliterator() {
        return delegate.spliterator();
    }
}
