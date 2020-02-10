package org.mongojack;

import com.mongodb.MongoNamespace;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.ClientSession;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.EstimatedDocumentCountOptions;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.RenameCollectionOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.lang.Nullable;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * A base class for decorating a MongoCollection.
 * <p>
 * {@inheritDoc}
 */
public abstract class MongoCollectionDecorator<TDocument> implements MongoCollection<TDocument> {

    /**
     * Get the mongo collection that we're operating on.
     *
     * @return A mongo collection.  Must not be null.  Must not be this.
     */
    protected abstract MongoCollection<TDocument> mongoCollection();

    /**
     * Manage the input Bson in any way necessary to produce a proper update.  The expectation
     * is that this is passed through the mapper or the codec, but the implementation is free
     * to do what it wants as long as the returned Bson is valid.
     *
     * @param update a valid Bson update document (e.g. containing $set, etc).
     * @return a valid Bson update document (e.g. containing $set, etc) transformed as necessary
     */
    protected abstract Bson manageUpdateBson(Bson update);

    /**
     * Manage the input Bson in any way necessary to produce a proper update.  The expectation
     * is that this is passed through the mapper or the codec, but the implementation is free
     * to do what it wants as long as the returned Bson is valid.
     *
     * @param update a valid Bson update pipeline (e.g. containing $set, etc).
     * @return a valid Bson update pipeline (e.g. containing $set, etc) transformed as necessary
     */
    protected abstract List<Bson> manageUpdatePipeline(List<? extends Bson> update);

    /**
     * Manage the input Bson in any way necessary to produce a proper filter.  The expectation
     * is that this is passed through the mapper or the codec, but the implementation is free
     * to do what it wants as long as the returned Bson is valid.
     *
     * @param filter a valid Bson filter document.
     * @return a valid Bson filter document transformed as necessary
     */
    protected abstract Bson manageFilterBson(Bson filter);

    /**
     * Manage the input Bson in any way necessary to produce a proper pipeline.  The expectation
     * is that this is passed through the mapper or the codec, but the implementation is free
     * to do what it wants as long as the returned Bson is valid.
     *
     * @param pipeline a list of Bson documents making up an aggregation pipeline
     * @return a list of Bson documents making up an aggregation pipeline, transformed as necessary
     */
    protected abstract List<Bson> manageAggregationPipeline(List<? extends Bson> pipeline);

    /**
     * Manages the input write bson for a bulk write.
     *
     * @param requests A list of WriteModel instances
     * @return A list of WriteModel instances with filter and update BSON documents updated with the mapper
     */
    protected abstract List<WriteModel<TDocument>> manageBulkWriteRequests(List<? extends WriteModel<? extends TDocument>> requests);

    /**
     * Wraps an iterable that supports further .filter() calls so that we can perform mapping on them.
     *
     * @param input a valid iterable
     * @param <T>   return type of the iterable
     * @return a wrapped iterable that supports some extended filter operations
     */
    protected abstract <T> DistinctIterable<T> wrapIterable(DistinctIterable<T> input);

    /**
     * Wraps an iterable that supports further .filter() calls so that we can perform mapping on them.
     *
     * @param input a valid iterable
     * @param <T>   return type of the iterable
     * @return a wrapped iterable that supports some extended filter operations
     */
    protected abstract <T> FindIterable<T> wrapIterable(FindIterable<T> input);

    /**
     * Wraps an iterable that supports further .filter() calls so that we can perform mapping on them.
     *
     * @param input a valid iterable
     * @param <T>   return type of the iterable
     * @return a wrapped iterable that supports some extended filter operations
     */
    protected abstract <T> MapReduceIterable<T> wrapIterable(MapReduceIterable<T> input);

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoNamespace getNamespace() {
        return mongoCollection().getNamespace();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<TDocument> getDocumentClass() {
        return mongoCollection().getDocumentClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CodecRegistry getCodecRegistry() {
        return mongoCollection().getCodecRegistry();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReadPreference getReadPreference() {
        return mongoCollection().getReadPreference();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WriteConcern getWriteConcern() {
        return mongoCollection().getWriteConcern();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReadConcern getReadConcern() {
        return mongoCollection().getReadConcern();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countDocuments() {
        return mongoCollection().countDocuments();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countDocuments(final Bson filter) {
        return mongoCollection().countDocuments(manageFilterBson(filter));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countDocuments(final Bson filter, final CountOptions options) {
        return mongoCollection().countDocuments(manageFilterBson(filter), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countDocuments(final ClientSession clientSession) {
        return mongoCollection().countDocuments(clientSession);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countDocuments(final ClientSession clientSession, final Bson filter) {
        return mongoCollection().countDocuments(clientSession, manageFilterBson(filter));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countDocuments(final ClientSession clientSession, final Bson filter, final CountOptions options) {
        return mongoCollection().countDocuments(clientSession, manageFilterBson(filter), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimatedDocumentCount() {
        return mongoCollection().estimatedDocumentCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimatedDocumentCount(final EstimatedDocumentCountOptions options) {
        return mongoCollection().estimatedDocumentCount(options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> DistinctIterable<TResult> distinct(final String fieldName, final Class<TResult> tResultClass) {
        return wrapIterable(mongoCollection().distinct(fieldName, tResultClass));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> DistinctIterable<TResult> distinct(final String fieldName, final Bson filter, final Class<TResult> tResultClass) {
        return wrapIterable(mongoCollection().distinct(fieldName, manageFilterBson(filter), tResultClass));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> DistinctIterable<TResult> distinct(final ClientSession clientSession, final String fieldName, final Class<TResult> tResultClass) {
        return wrapIterable(mongoCollection().distinct(clientSession, fieldName, tResultClass));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> DistinctIterable<TResult> distinct(
        final ClientSession clientSession,
        final String fieldName,
        final Bson filter,
        final Class<TResult> tResultClass
    ) {
        return wrapIterable(mongoCollection().distinct(clientSession, fieldName, manageFilterBson(filter), tResultClass));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TDocument> find() {
        return wrapIterable(mongoCollection().find());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> FindIterable<TResult> find(final Class<TResult> tResultClass) {
        return wrapIterable(mongoCollection().find(tResultClass));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TDocument> find(final Bson filter) {
        return wrapIterable(mongoCollection().find(manageFilterBson(filter)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> FindIterable<TResult> find(final Bson filter, final Class<TResult> tResultClass) {
        return wrapIterable(mongoCollection().find(manageFilterBson(filter), tResultClass));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TDocument> find(final ClientSession clientSession) {
        return wrapIterable(mongoCollection().find(clientSession));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> FindIterable<TResult> find(final ClientSession clientSession, final Class<TResult> tResultClass) {
        return wrapIterable(mongoCollection().find(clientSession, tResultClass));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindIterable<TDocument> find(final ClientSession clientSession, final Bson filter) {
        return wrapIterable(mongoCollection().find(clientSession, manageFilterBson(filter)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> FindIterable<TResult> find(final ClientSession clientSession, final Bson filter, final Class<TResult> tResultClass) {
        return wrapIterable(mongoCollection().find(clientSession, manageFilterBson(filter), tResultClass));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AggregateIterable<TDocument> aggregate(final List<? extends Bson> pipeline) {
        return mongoCollection().aggregate(manageAggregationPipeline(pipeline));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> AggregateIterable<TResult> aggregate(final List<? extends Bson> pipeline, final Class<TResult> tResultClass) {
        return mongoCollection().aggregate(manageAggregationPipeline(pipeline), tResultClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AggregateIterable<TDocument> aggregate(final ClientSession clientSession, final List<? extends Bson> pipeline) {
        return mongoCollection().aggregate(clientSession, manageAggregationPipeline(pipeline));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> AggregateIterable<TResult> aggregate(
        final ClientSession clientSession,
        final List<? extends Bson> pipeline,
        final Class<TResult> tResultClass
    ) {
        return mongoCollection().aggregate(clientSession, manageAggregationPipeline(pipeline), tResultClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeStreamIterable<TDocument> watch() {
        return mongoCollection().watch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(final Class<TResult> tResultClass) {
        return mongoCollection().watch(tResultClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeStreamIterable<TDocument> watch(final List<? extends Bson> pipeline) {
        return mongoCollection().watch(manageAggregationPipeline(pipeline));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(final List<? extends Bson> pipeline, final Class<TResult> tResultClass) {
        return mongoCollection().watch(manageAggregationPipeline(pipeline), tResultClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeStreamIterable<TDocument> watch(final ClientSession clientSession) {
        return mongoCollection().watch(clientSession);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(final ClientSession clientSession, final Class<TResult> tResultClass) {
        return mongoCollection().watch(clientSession, tResultClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeStreamIterable<TDocument> watch(final ClientSession clientSession, final List<? extends Bson> pipeline) {
        return mongoCollection().watch(clientSession, manageAggregationPipeline(pipeline));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(
        final ClientSession clientSession,
        final List<? extends Bson> pipeline,
        final Class<TResult> tResultClass
    ) {
        return mongoCollection().watch(clientSession, manageAggregationPipeline(pipeline), tResultClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapReduceIterable<TDocument> mapReduce(final String mapFunction, final String reduceFunction) {
        return wrapIterable(mongoCollection().mapReduce(mapFunction, reduceFunction));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> MapReduceIterable<TResult> mapReduce(final String mapFunction, final String reduceFunction, final Class<TResult> tResultClass) {
        return wrapIterable(mongoCollection().mapReduce(mapFunction, reduceFunction, tResultClass));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapReduceIterable<TDocument> mapReduce(final ClientSession clientSession, final String mapFunction, final String reduceFunction) {
        return wrapIterable(mongoCollection().mapReduce(clientSession, mapFunction, reduceFunction));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> MapReduceIterable<TResult> mapReduce(
        final ClientSession clientSession,
        final String mapFunction,
        final String reduceFunction,
        final Class<TResult> tResultClass
    ) {
        return wrapIterable(mongoCollection().mapReduce(clientSession, mapFunction, reduceFunction, tResultClass));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BulkWriteResult bulkWrite(final List<? extends WriteModel<? extends TDocument>> requests) {
        return mongoCollection().bulkWrite(manageBulkWriteRequests(requests));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BulkWriteResult bulkWrite(final List<? extends WriteModel<? extends TDocument>> requests, final BulkWriteOptions options) {
        return mongoCollection().bulkWrite(manageBulkWriteRequests(requests), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BulkWriteResult bulkWrite(final ClientSession clientSession, final List<? extends WriteModel<? extends TDocument>> requests) {
        return mongoCollection().bulkWrite(clientSession, manageBulkWriteRequests(requests));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BulkWriteResult bulkWrite(
        final ClientSession clientSession,
        final List<? extends WriteModel<? extends TDocument>> requests,
        final BulkWriteOptions options
    ) {
        return mongoCollection().bulkWrite(clientSession, manageBulkWriteRequests(requests), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InsertOneResult insertOne(final TDocument tDocument) {
        return mongoCollection().insertOne(tDocument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InsertOneResult insertOne(final TDocument tDocument, final InsertOneOptions options) {
        return mongoCollection().insertOne(tDocument, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InsertOneResult insertOne(final ClientSession clientSession, final TDocument tDocument) {
        return mongoCollection().insertOne(clientSession, tDocument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InsertOneResult insertOne(final ClientSession clientSession, final TDocument tDocument, final InsertOneOptions options) {
        return mongoCollection().insertOne(clientSession, tDocument, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InsertManyResult insertMany(final List<? extends TDocument> tDocuments) {
        return mongoCollection().insertMany(tDocuments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InsertManyResult insertMany(final List<? extends TDocument> tDocuments, final InsertManyOptions options) {
        return mongoCollection().insertMany(tDocuments, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InsertManyResult insertMany(final ClientSession clientSession, final List<? extends TDocument> tDocuments) {
        return mongoCollection().insertMany(clientSession, tDocuments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InsertManyResult insertMany(final ClientSession clientSession, final List<? extends TDocument> tDocuments, final InsertManyOptions options) {
        return mongoCollection().insertMany(clientSession, tDocuments, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteResult deleteOne(final Bson filter) {
        return mongoCollection().deleteOne(manageFilterBson(filter));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteResult deleteOne(final Bson filter, final DeleteOptions options) {
        return mongoCollection().deleteOne(manageFilterBson(filter), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteResult deleteOne(final ClientSession clientSession, final Bson filter) {
        return mongoCollection().deleteOne(clientSession, manageFilterBson(filter));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteResult deleteOne(final ClientSession clientSession, final Bson filter, final DeleteOptions options) {
        return mongoCollection().deleteOne(clientSession, manageFilterBson(filter), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteResult deleteMany(final Bson filter) {
        return mongoCollection().deleteMany(manageFilterBson(filter));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteResult deleteMany(final Bson filter, final DeleteOptions options) {
        return mongoCollection().deleteMany(manageFilterBson(filter), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteResult deleteMany(final ClientSession clientSession, final Bson filter) {
        return mongoCollection().deleteMany(clientSession, manageFilterBson(filter));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteResult deleteMany(final ClientSession clientSession, final Bson filter, final DeleteOptions options) {
        return mongoCollection().deleteMany(clientSession, manageFilterBson(filter), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult replaceOne(final Bson filter, final TDocument replacement) {
        return mongoCollection().replaceOne(manageFilterBson(filter), replacement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult replaceOne(final Bson filter, final TDocument replacement, final ReplaceOptions replaceOptions) {
        return mongoCollection().replaceOne(manageFilterBson(filter), replacement, replaceOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult replaceOne(final ClientSession clientSession, final Bson filter, final TDocument replacement) {
        return mongoCollection().replaceOne(clientSession, manageFilterBson(filter), replacement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult replaceOne(
        final ClientSession clientSession,
        final Bson filter,
        final TDocument replacement,
        final ReplaceOptions replaceOptions
    ) {
        return mongoCollection().replaceOne(clientSession, manageFilterBson(filter), replacement, replaceOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateOne(final Bson filter, final Bson update) {
        return mongoCollection().updateOne(manageFilterBson(filter), manageUpdateBson(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateOne(final Bson filter, final Bson update, final UpdateOptions updateOptions) {
        return mongoCollection().updateOne(manageFilterBson(filter), manageUpdateBson(update), updateOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateOne(final ClientSession clientSession, final Bson filter, final Bson update) {
        return mongoCollection().updateOne(clientSession, manageFilterBson(filter), manageUpdateBson(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateOne(
        final ClientSession clientSession,
        final Bson filter,
        final Bson update,
        final UpdateOptions updateOptions
    ) {
        return mongoCollection().updateOne(clientSession, manageFilterBson(filter), manageUpdateBson(update), updateOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateOne(final Bson filter, final List<? extends Bson> update) {
        return mongoCollection().updateOne(manageFilterBson(filter), manageUpdatePipeline(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateOne(
        final Bson filter,
        final List<? extends Bson> update,
        final UpdateOptions updateOptions
    ) {
        return mongoCollection().updateOne(manageFilterBson(filter), manageUpdatePipeline(update), updateOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateOne(final ClientSession clientSession, final Bson filter, final List<? extends Bson> update) {
        return mongoCollection().updateOne(clientSession, manageFilterBson(filter), manageUpdatePipeline(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateOne(
        final ClientSession clientSession,
        final Bson filter,
        final List<? extends Bson> update,
        final UpdateOptions updateOptions
    ) {
        return mongoCollection().updateOne(clientSession, manageFilterBson(filter), manageUpdatePipeline(update), updateOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateMany(final Bson filter, final Bson update) {
        return mongoCollection().updateMany(manageFilterBson(filter), manageUpdateBson(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateMany(final Bson filter, final Bson update, final UpdateOptions updateOptions) {
        return mongoCollection().updateMany(manageFilterBson(filter), manageUpdateBson(update), updateOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateMany(final ClientSession clientSession, final Bson filter, final Bson update) {
        return mongoCollection().updateMany(clientSession, manageFilterBson(filter), manageUpdateBson(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateMany(
        final ClientSession clientSession,
        final Bson filter,
        final Bson update,
        final UpdateOptions updateOptions
    ) {
        return mongoCollection().updateMany(clientSession, manageFilterBson(filter), manageUpdateBson(update), updateOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateMany(final Bson filter, final List<? extends Bson> update) {
        return mongoCollection().updateMany(manageFilterBson(filter), manageUpdatePipeline(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateMany(
        final Bson filter,
        final List<? extends Bson> update,
        final UpdateOptions updateOptions
    ) {
        return mongoCollection().updateMany(manageFilterBson(filter), manageUpdatePipeline(update), updateOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateMany(final ClientSession clientSession, final Bson filter, final List<? extends Bson> update) {
        return mongoCollection().updateMany(clientSession, manageFilterBson(filter), manageUpdatePipeline(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updateMany(
        final ClientSession clientSession,
        final Bson filter,
        final List<? extends Bson> update,
        final UpdateOptions updateOptions
    ) {
        return mongoCollection().updateMany(clientSession, manageFilterBson(filter), manageUpdatePipeline(update), updateOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndDelete(final Bson filter) {
        return mongoCollection().findOneAndDelete(manageFilterBson(filter));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndDelete(final Bson filter, final FindOneAndDeleteOptions options) {
        return mongoCollection().findOneAndDelete(manageFilterBson(filter), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndDelete(final ClientSession clientSession, final Bson filter) {
        return mongoCollection().findOneAndDelete(clientSession, manageFilterBson(filter));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndDelete(final ClientSession clientSession, final Bson filter, final FindOneAndDeleteOptions options) {
        return mongoCollection().findOneAndDelete(clientSession, manageFilterBson(filter), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndReplace(final Bson filter, final TDocument replacement) {
        return mongoCollection().findOneAndReplace(manageFilterBson(filter), replacement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndReplace(final Bson filter, final TDocument replacement, final FindOneAndReplaceOptions options) {
        return mongoCollection().findOneAndReplace(manageFilterBson(filter), replacement, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndReplace(final ClientSession clientSession, final Bson filter, final TDocument replacement) {
        return mongoCollection().findOneAndReplace(clientSession, manageFilterBson(filter), replacement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndReplace(final ClientSession clientSession, final Bson filter, final TDocument replacement, final FindOneAndReplaceOptions options) {
        return mongoCollection().findOneAndReplace(clientSession, manageFilterBson(filter), replacement, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndUpdate(final Bson filter, final Bson update) {
        return mongoCollection().findOneAndUpdate(manageFilterBson(filter), manageUpdateBson(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndUpdate(final Bson filter, final Bson update, final FindOneAndUpdateOptions options) {
        return mongoCollection().findOneAndUpdate(manageFilterBson(filter), manageUpdateBson(update), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndUpdate(final ClientSession clientSession, final Bson filter, final Bson update) {
        return mongoCollection().findOneAndUpdate(clientSession, manageFilterBson(filter), manageUpdateBson(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndUpdate(
        final ClientSession clientSession,
        final Bson filter,
        final Bson update,
        final FindOneAndUpdateOptions options
    ) {
        return mongoCollection().findOneAndUpdate(clientSession, manageFilterBson(filter), manageUpdateBson(update), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndUpdate(final Bson filter, final List<? extends Bson> update) {
        return mongoCollection().findOneAndUpdate(manageFilterBson(filter), manageUpdatePipeline(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndUpdate(final Bson filter, final List<? extends Bson> update, final FindOneAndUpdateOptions options) {
        return mongoCollection().findOneAndUpdate(manageFilterBson(filter), manageUpdatePipeline(update), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndUpdate(final ClientSession clientSession, final Bson filter, final List<? extends Bson> update) {
        return mongoCollection().findOneAndUpdate(clientSession, manageFilterBson(filter), manageUpdatePipeline(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public TDocument findOneAndUpdate(
        final ClientSession clientSession,
        final Bson filter,
        final List<? extends Bson> update,
        final FindOneAndUpdateOptions options
    ) {
        return mongoCollection().findOneAndUpdate(clientSession, manageFilterBson(filter), manageUpdatePipeline(update), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drop() {
        mongoCollection().drop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drop(final ClientSession clientSession) {
        mongoCollection().drop(clientSession);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createIndex(final Bson keys) {
        return mongoCollection().createIndex(keys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createIndex(final Bson keys, final IndexOptions indexOptions) {
        return mongoCollection().createIndex(keys, indexOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createIndex(final ClientSession clientSession, final Bson keys) {
        return mongoCollection().createIndex(clientSession, keys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createIndex(final ClientSession clientSession, final Bson keys, final IndexOptions indexOptions) {
        return mongoCollection().createIndex(clientSession, keys, indexOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> createIndexes(final List<IndexModel> indexes) {
        return mongoCollection().createIndexes(indexes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> createIndexes(final List<IndexModel> indexes, final CreateIndexOptions createIndexOptions) {
        return mongoCollection().createIndexes(indexes, createIndexOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> createIndexes(final ClientSession clientSession, final List<IndexModel> indexes) {
        return mongoCollection().createIndexes(clientSession, indexes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> createIndexes(
        final ClientSession clientSession,
        final List<IndexModel> indexes,
        final CreateIndexOptions createIndexOptions
    ) {
        return mongoCollection().createIndexes(clientSession, indexes, createIndexOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIndexesIterable<Document> listIndexes() {
        return mongoCollection().listIndexes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> ListIndexesIterable<TResult> listIndexes(final Class<TResult> tResultClass) {
        return mongoCollection().listIndexes(tResultClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIndexesIterable<Document> listIndexes(final ClientSession clientSession) {
        return mongoCollection().listIndexes(clientSession);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TResult> ListIndexesIterable<TResult> listIndexes(final ClientSession clientSession, final Class<TResult> tResultClass) {
        return mongoCollection().listIndexes(clientSession, tResultClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropIndex(final String indexName) {
        mongoCollection().dropIndex(indexName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropIndex(final String indexName, final DropIndexOptions dropIndexOptions) {
        mongoCollection().dropIndex(indexName, dropIndexOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropIndex(final Bson keys) {
        mongoCollection().dropIndex(keys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropIndex(final Bson keys, final DropIndexOptions dropIndexOptions) {
        mongoCollection().dropIndex(keys, dropIndexOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropIndex(final ClientSession clientSession, final String indexName) {
        mongoCollection().dropIndex(clientSession, indexName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropIndex(final ClientSession clientSession, final Bson keys) {
        mongoCollection().dropIndex(clientSession, keys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropIndex(final ClientSession clientSession, final String indexName, final DropIndexOptions dropIndexOptions) {
        mongoCollection().dropIndex(clientSession, indexName, dropIndexOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropIndex(final ClientSession clientSession, final Bson keys, final DropIndexOptions dropIndexOptions) {
        mongoCollection().dropIndex(clientSession, keys, dropIndexOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropIndexes() {
        mongoCollection().dropIndexes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropIndexes(final ClientSession clientSession) {
        mongoCollection().dropIndexes(clientSession);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropIndexes(final DropIndexOptions dropIndexOptions) {
        mongoCollection().dropIndexes(dropIndexOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropIndexes(final ClientSession clientSession, final DropIndexOptions dropIndexOptions) {
        mongoCollection().dropIndexes(clientSession, dropIndexOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renameCollection(final MongoNamespace newCollectionNamespace) {
        mongoCollection().renameCollection(newCollectionNamespace);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renameCollection(final MongoNamespace newCollectionNamespace, final RenameCollectionOptions renameCollectionOptions) {
        mongoCollection().renameCollection(newCollectionNamespace, renameCollectionOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renameCollection(final ClientSession clientSession, final MongoNamespace newCollectionNamespace) {
        mongoCollection().renameCollection(clientSession, newCollectionNamespace);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renameCollection(
        final ClientSession clientSession,
        final MongoNamespace newCollectionNamespace,
        final RenameCollectionOptions renameCollectionOptions
    ) {
        mongoCollection().renameCollection(clientSession, newCollectionNamespace, renameCollectionOptions);
    }

}
