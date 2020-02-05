/*
 * Copyright 2011 VZ Netzwerke Ltd
 * Copyright 2014 devbliss GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mongojack;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.DeleteManyModel;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.mongojack.internal.MongoJackModule;
import org.mongojack.internal.stream.JacksonCodec;
import org.mongojack.internal.util.DistinctIterableDecorator;
import org.mongojack.internal.util.DocumentSerializationUtils;
import org.mongojack.internal.util.FindIterableDecorator;
import org.mongojack.internal.util.MapReduceIterableDecorator;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * A DBCollection that marshals/demarshals objects to/from Jackson annotated
 * classes. It provides a very thin wrapper over an existing MongoCollection.
 * <p>
 * A JacksonMongoCollection is threadsafe, with a few caveats:
 * <p>
 * If you pass your own ObjectMapper to it, it is not thread safe to reconfigure
 * that ObjectMapper at all after creating it. The setWritePreference and a few
 * other methods on JacksonMongoCollection should not be called from multiple
 * threads
 * <p>
 * Obtain an instance using JacksonMongoCollection.builder()...build()
 * <p>
 * Many of these methods accept queries or update documents in Bson format.  You can assemble
 * the Bson any way you want, including using Document:
 *
 * <pre>
 *     new Document("foo", new Document("$gt", 7))
 * </pre>
 * <p>
 * or using the mongo model builders for these objects:
 *
 * <pre>
 *     Filters.eq("foo", 7))
 *
 *     Updates.inc("bar", 3)
 * </pre>
 *
 * @author James Roper
 * @since 1.0
 */
@SuppressWarnings({"UnusedReturnValue"})
public class JacksonMongoCollection<TResult> extends MongoCollectionDecorator<TResult> {

    private static final AtomicReference<ObjectMapper> DEFAULT_OBJECT_MAPPER = new AtomicReference<>();
    private final ObjectMapper objectMapper;
    private final JacksonCodecRegistry jacksonCodecRegistry;
    @SuppressWarnings("FieldCanBeLocal")
    private final Class<?> view;
    private final Class<TResult> valueClass;
    private final JavaType type;
    private final com.mongodb.client.MongoCollection<TResult> mongoCollection;

    /**
     * Private.
     */
    private JacksonMongoCollection(
        com.mongodb.client.MongoCollection<TResult> mongoCollection,
        ObjectMapper objectMapper,
        Class<TResult> valueClass,
        Class<?> view
    ) {
        this.objectMapper = objectMapper != null ? objectMapper : getDefaultObjectMapper();
        this.view = view;
        jacksonCodecRegistry = new JacksonCodecRegistry(this.objectMapper, this.view);
        jacksonCodecRegistry.addCodecForClass(valueClass);
        this.mongoCollection = mongoCollection.withDocumentClass(valueClass).withCodecRegistry(jacksonCodecRegistry);
        this.valueClass = valueClass;
        this.type = this.objectMapper.constructType(valueClass);
    }

    /**
     * Private.
     */
    private JacksonMongoCollection(
        final ObjectMapper objectMapper,
        final JacksonCodecRegistry jacksonCodecRegistry,
        final Class<?> view,
        final Class<TResult> valueClass,
        final JavaType type,
        final MongoCollection<TResult> mongoCollection
    ) {
        this.objectMapper = objectMapper;
        this.jacksonCodecRegistry = jacksonCodecRegistry;
        this.view = view;
        this.valueClass = valueClass;
        this.type = type;
        this.mongoCollection = mongoCollection;
    }

    /**
     * A utility to get the DEFAULT_OBJECT_MAPPER which sets it lazily, so it's never constructed if we don't use it.
     *
     * @return The default object mapper.
     */
    private static ObjectMapper getDefaultObjectMapper() {
        return DEFAULT_OBJECT_MAPPER.updateAndGet((current) -> {
            if (current == null) {
                return MongoJackModule.configure(new ObjectMapper());
            }
            return current;
        });
    }

    /**
     * Creates builder to build JacksonMongoCollection.
     *
     * @return created builder
     */
    public static JacksonMongoCollectionBuilder builder() {
        return new JacksonMongoCollectionBuilder();
    }

    /**
     * Returns a single object from this collection.
     *
     * @return the object found, or <code>null</code> if the collection is empty
     * @throws MongoException If an error occurred
     */
    public TResult findOne() throws MongoException {
        return findOne(new Document());
    }

    /**
     * Returns a single object from this collection matching the query.
     *
     * @param query the query object
     * @return the object found, or <code>null</code> if no such object exists
     */
    public TResult findOne(Bson query) {
        return find(query).first();
    }

    /**
     * Returns a single object from this collection matching the query.
     *
     * @param query      the query object
     * @param projection the projection
     * @return the object found, or <code>null</code> if no such object exists
     */
    public TResult findOne(Bson query, Bson projection) {
        return find(query).projection(projection).first();
    }

    /**
     * Find an object by the given id
     *
     * @param id The id
     * @return The object
     * @throws MongoException If an error occurred
     */
    public TResult findOneById(Object id) throws MongoException {
        return findOne(createIdQuery(id));
    }

    /**
     * Creates a document query object for the _id field using the object as the _id. This object is expected to already
     * be in the correct format... Document, Long, String, etc...
     *
     * @param id  An id to search for
     * @param ids Other ids to search for
     * @return A Bson query for the id or ids
     */
    public Bson createIdQuery(Object id, Object... ids) {
        if (ids.length == 0) {
            if (id instanceof BsonValue) {
                return Filters.eq("_id", id);
            }
            return Filters.eq("_id", JacksonCodec.constructIdValue(id, JacksonCodec.getIdElement(valueClass)));
        }
        List<Object> allIds = Arrays.asList(ids);
        allIds.add(id);
        return createIdInQuery(allIds);
    }

    public Bson createIdInQuery(final List<?> allIds) {
        final Optional<? extends AnnotatedElement> idElement = JacksonCodec.getIdElement(valueClass);
        return Filters.in(
            "_id",
            allIds.stream()
                .map((currentId) -> {
                    if (currentId instanceof BsonValue) {
                        return currentId;
                    }
                    return JacksonCodec.constructIdValue(currentId, idElement);
                })
                .collect(Collectors.toList())
        );
    }

    /**
     * Get the type of this collection
     *
     * @return The type
     */
    public JacksonCollectionKey<TResult> getCollectionKey() {
        return new JacksonCollectionKey<>(getMongoCollection().getNamespace().getDatabaseName(), getMongoCollection().getNamespace().getCollectionName(), getValueClass());
    }

    /**
     * Get the underlying mongo collection
     *
     * @return The underlying mongo collection
     */
    public com.mongodb.client.MongoCollection<TResult> getMongoCollection() {
        return mongoCollection;
    }

    /**
     * Gets the DB name in which the underlying collection is stored
     *
     * @return The name of the database in which this collection is being stored.
     */
    public String getDatabaseName() {
        return mongoCollection.getNamespace().getDatabaseName();
    }

    /**
     * Gets the name of the underlying collection
     *
     * @return the name of the collection
     */
    public String getName() {
        return mongoCollection.getNamespace().getCollectionName();
    }

    public Class<TResult> getValueClass() {
        return valueClass;
    }

    /**
     * Inserts objects into the database. if the objects' _id are null, they will be generated.
     *
     * @param objects The objects to insert
     * @throws MongoBulkWriteException If there's an exception in the bulk write operation
     * @throws MongoException          If an error occurred
     */
    @SafeVarargs
    public final void insert(TResult... objects) throws MongoException, MongoBulkWriteException {
        ArrayList<TResult> objectList = new ArrayList<>(objects.length);
        Collections.addAll(objectList, objects);
        insert(objectList);
    }

    /**
     * Inserts objects into the database. if the objects' _id are null, they will be generated.
     *
     * @param list The objects to insert
     * @throws MongoBulkWriteException If there's an exception in the bulk write operation
     * @throws MongoException          If an error occurred
     */
    public void insert(List<TResult> list) throws MongoException, MongoBulkWriteException {
        mongoCollection.insertMany(list);
    }

    /**
     * Inserts objects into the database. if the objects' _id are null, they will be generated.
     *
     * @param objects The objects to insert
     * @param concern the write concern
     * @throws MongoBulkWriteException If there's an exception in the bulk write operation
     * @throws MongoException          If an error occurred
     */
    @SafeVarargs
    public final void insert(WriteConcern concern, TResult... objects)
        throws MongoException, MongoBulkWriteException {
        ArrayList<TResult> objectList = new ArrayList<>(objects.length);
        Collections.addAll(objectList, objects);
        insert(objectList, concern);
    }

    /**
     * Inserts objects into the database. if the objects' _id are null, they will be generated.
     *
     * @param list    The objects to insert
     * @param concern the write concern
     * @throws MongoBulkWriteException If there's an exception in the bulk write operation
     * @throws MongoException          If an error occurred
     */
    public void insert(List<TResult> list, WriteConcern concern)
        throws MongoException {
        mongoCollection.withWriteConcern(concern).insertMany(list);
    }

    /**
     * Removes object from the database collection with the default WriteConcern
     *
     * @param _id the id of the document to remove
     * @return The delete result
     * @throws MongoWriteException        If the write failed due some other failure specific to the delete command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public DeleteResult removeById(Object _id) throws MongoException, MongoWriteException, MongoWriteConcernException {
        return deleteOne(createIdQuery(_id));
    }

    /**
     * Performs an update operation, replacing the entire document, for the document with this _id.
     *
     * @param _id    the _id of the object to replace
     * @param object object with which to replace it
     * @return The result
     * @throws MongoWriteException        If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult replaceOneById(Object _id, TResult object) throws MongoException, MongoWriteException, MongoWriteConcernException {
        return replaceOne(createIdQuery(_id), object);
    }

    /**
     * Saves and object to this collection (does insert or update based on the object _id). Uses default write concern.
     *
     * @param object the object to save. will add <code>_id</code> field to object if
     *               needed
     * @return The UpdateResult result
     * @throws MongoWriteException        If the write failed due some other failure specific to the delete command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult save(TResult object) throws MongoWriteException, MongoWriteConcernException, MongoException {
        return this.save(object, null);
    }

    /**
     * Saves an object to this collection (does insert or update based on the
     * object _id).
     *
     * @param object  the <code>DBObject</code> to save
     * @param concern the write concern
     * @return The UpdateResult result
     * @throws MongoWriteException        If the write failed due some other failure specific to the delete command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult save(TResult object, WriteConcern concern) throws MongoWriteException, MongoWriteConcernException, MongoException {
        final JacksonCodec<TResult> codec = getValueClassCodec();
        BsonValue _id = codec.getDocumentId(object);
        if (_id == null || _id.isNull()) {
            if (concern == null) {
                insertOne(object);
            } else {
                withWriteConcern(concern).insertOne(object);
            }
            return UpdateResult.acknowledged(0, 1L, codec.getDocumentId(object));
        } else {
            BsonDocument query = new BsonDocument();
            query.put("_id", _id);
            if (concern != null) {
                return withWriteConcern(concern).replaceOne(query, object, new ReplaceOptions().upsert(true));
            }
            return replaceOne(query, object, new ReplaceOptions().upsert(true));
        }
    }

    private JacksonCodec<TResult> getValueClassCodec() {
        return (JacksonCodec<TResult>) jacksonCodecRegistry.get(valueClass);
    }

    @Override
    protected MongoCollection<TResult> mongoCollection() {
        return mongoCollection;
    }

    /**
     * Serialize the fields of the given object using the object mapper
     * for this collection.
     * This will convert POJOs to DBObjects where necessary.
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected Bson manageUpdateBson(final Bson update) {
        initializeIfNecessary(update);
        if (update instanceof InitializationRequiredForTransformation) {
            return update;
        }
        return DocumentSerializationUtils.serializeFields(update, jacksonCodecRegistry);
    }

    /**
     * Does a simple conversion (using toBsonDocument with this collection's CodecRegistry).
     *
     * {@inheritDoc}
     */
    @Override
    protected List<Bson> manageUpdatePipeline(final List<? extends Bson> update) {
        return update.stream().map((u) -> u.toBsonDocument(Document.class, jacksonCodecRegistry)).collect(Collectors.toList());
    }

    /**
     * Serialize the fields of the given object using the object mapper
     * for this collection.
     * This will convert POJOs to DBObjects where necessary.
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected Bson manageFilterBson(final Bson filter) {
        initializeIfNecessary(filter);
        if (filter instanceof InitializationRequiredForTransformation) {
            return filter;
        }
        return DocumentSerializationUtils.serializeFilter(objectMapper, type, filter, jacksonCodecRegistry);
    }

    /**
     * Does no real conversion, but it does initialize the pipeline correctly if it is one of the deprecated
     * mongojack ones..
     *
     * {@inheritDoc}
     *
     * @param pipeline a list of Bson documents making up an aggregation pipeline
     */
    @Override
    @SuppressWarnings("unchecked")
    protected List<Bson> manageAggregationPipeline(final List<? extends Bson> pipeline) {
        initializeIfNecessary(pipeline);
        return (List<Bson>) pipeline;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected List<WriteModel<TResult>> manageBulkWriteRequests(final List<? extends WriteModel<? extends TResult>> requests) {
        return requests.stream()
            .map(
                request -> {
                    if (request instanceof DeleteOneModel) {
                        final DeleteOneModel<TResult> deleteRequest = (DeleteOneModel) request;
                        return new DeleteOneModel<TResult>(manageFilterBson(deleteRequest.getFilter()), deleteRequest.getOptions());
                    }
                    if (request instanceof DeleteManyModel) {
                        final DeleteManyModel<TResult> deleteRequest = (DeleteManyModel) request;
                        return new DeleteManyModel<TResult>(manageFilterBson(deleteRequest.getFilter()), deleteRequest.getOptions());
                    }
                    if (request instanceof ReplaceOneModel) {
                        final ReplaceOneModel<TResult> replaceRequest = (ReplaceOneModel) request;
                        return new ReplaceOneModel<>(manageFilterBson(replaceRequest.getFilter()), replaceRequest.getReplacement(), replaceRequest.getReplaceOptions());
                    }
                    if (request instanceof UpdateOneModel) {
                        final UpdateOneModel<TResult> updateRequest = (UpdateOneModel) request;
                        if (updateRequest.getUpdatePipeline() != null) {
                            return new UpdateOneModel<TResult>(manageFilterBson(updateRequest.getFilter()), manageUpdatePipeline(updateRequest.getUpdatePipeline()), updateRequest.getOptions());
                        }
                        return new UpdateOneModel<TResult>(manageFilterBson(updateRequest.getFilter()), manageUpdateBson(updateRequest.getUpdate()), updateRequest.getOptions());
                    }
                    if (request instanceof UpdateManyModel) {
                        final UpdateManyModel<TResult> updateRequest = (UpdateManyModel) request;
                        if (updateRequest.getUpdatePipeline() != null) {
                            return new UpdateManyModel<TResult>(manageFilterBson(updateRequest.getFilter()), manageUpdatePipeline(updateRequest.getUpdatePipeline()), updateRequest.getOptions());
                        }
                        return new UpdateManyModel<TResult>(manageFilterBson(updateRequest.getFilter()), manageUpdateBson(updateRequest.getUpdate()), updateRequest.getOptions());
                    }
                    return (WriteModel<TResult>)request;
                }
            )
            .collect(Collectors.toList());
    }

    @Override
    protected <T1> DistinctIterable<T1> wrapIterable(final DistinctIterable<T1> input) {
        return new DistinctIterableDecorator<>(input, objectMapper, type, jacksonCodecRegistry);
    }

    @Override
    protected <T1> FindIterable<T1> wrapIterable(final FindIterable<T1> input) {
        return new FindIterableDecorator<>(input, objectMapper, type, jacksonCodecRegistry);
    }

    @Override
    protected <T1> MapReduceIterable<T1> wrapIterable(final MapReduceIterable<T1> input) {
        return new MapReduceIterableDecorator<>(input, objectMapper, type, jacksonCodecRegistry);
    }

    @Override
    public String toString() {
        return String.format("%s<%s, %s>", getClass().getName(), getMongoCollection().getNamespace().getFullName(), valueClass.getName());
    }

    /**
     * Performs an update operation.
     *
     * @param _id    The id of the document to update
     * @param update update with which to update
     * @return The write result
     * @throws MongoWriteException        If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult updateById(Object _id, Bson update)
        throws MongoException, MongoWriteException, MongoWriteConcernException {
        return updateOne(
            createIdQuery(_id),
            update
        );
    }

    @Override
    public <NewTDocument> JacksonMongoCollection<NewTDocument> withDocumentClass(final Class<NewTDocument> clazz) {
        return new JacksonMongoCollection<>(
            objectMapper,
            jacksonCodecRegistry,
            view,
            clazz,
            objectMapper.constructType(clazz),
            mongoCollection.withDocumentClass(clazz)
        );
    }

    @Override
    public MongoCollection<TResult> withCodecRegistry(final CodecRegistry codecRegistry) {
        return mongoCollection.withCodecRegistry(codecRegistry);
    }

    @Override
    public JacksonMongoCollection<TResult> withReadPreference(final ReadPreference readPreference) {
        return new JacksonMongoCollection<>(
            objectMapper,
            jacksonCodecRegistry,
            view,
            valueClass,
            type,
            mongoCollection.withReadPreference(readPreference)
        );
    }

    @Override
    public JacksonMongoCollection<TResult> withWriteConcern(final WriteConcern writeConcern) {
        return new JacksonMongoCollection<>(
            objectMapper,
            jacksonCodecRegistry,
            view,
            valueClass,
            type,
            mongoCollection.withWriteConcern(writeConcern)
        );
    }

    @Override
    public JacksonMongoCollection<TResult> withReadConcern(final ReadConcern readConcern) {
        return new JacksonMongoCollection<>(
            objectMapper,
            jacksonCodecRegistry,
            view,
            valueClass,
            type,
            mongoCollection.withReadConcern(readConcern)
        );
    }

    private void initializeIfNecessary(Object maybeInitializable) {
        if (maybeInitializable instanceof InitializationRequiredForTransformation) {
            ((InitializationRequiredForTransformation) maybeInitializable).initialize(objectMapper, type, jacksonCodecRegistry);
        }
    }

    /**
     * Builder to build {@link JacksonMongoCollection}.
     */
    public static final class JacksonMongoCollectionBuilder {
        private ObjectMapper objectMapper;
        private Class<?> view;

        private JacksonMongoCollectionBuilder() {
        }

        /**
         * Sets the object mapper for this collection.  Optional
         *
         * @param objectMapper The object mapper to use
         * @return the builder
         */
        public JacksonMongoCollectionBuilder withObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        /**
         * Set a view class for this collection.  Optional.
         * @param view The jackson view class
         * @return the builder
         */
        @SuppressWarnings("unused")
        public JacksonMongoCollectionBuilder withView(Class<?> view) {
            this.view = view;
            return this;
        }

        /**
         * Builds a {@link JacksonMongoCollection}. Required parameters are set here.
         *
         * @param client         A client
         * @param databaseName   Name of the database the collection is in
         * @param collectionName Name of the collection itself
         * @param valueType      The class of the value type
         * @param <CT>           The value type
         * @return A constructed collection meeting the MongoCollection interface.
         */
        public <CT> JacksonMongoCollection<CT> build(MongoClient client, String databaseName, String collectionName, Class<CT> valueType) {
            return build(client.getDatabase(databaseName), collectionName, valueType);
        }

        /**
         * Builds a {@link JacksonMongoCollection}. Required parameters are set here.
         *
         * @param client       A client
         * @param databaseName Name of the database the collection is in
         * @param valueType    The class of the value type.  Must be annotated with {@link org.mongojack.MongoCollection}.
         * @param <CT>         The value type
         * @return A constructed collection meeting the MongoCollection interface.
         */
        public <CT> JacksonMongoCollection<CT> build(MongoClient client, String databaseName, Class<CT> valueType) {
            return build(client.getDatabase(databaseName), valueType);
        }

        /**
         * Builds a {@link JacksonMongoCollection}. Required parameters are set here.
         *
         * @param database         A client
         * @param collectionName Name of the collection itself
         * @param valueType      The class of the value type
         * @param <CT>           The value type
         * @return A constructed collection meeting the MongoCollection interface.
         */
        public <CT> JacksonMongoCollection<CT> build(MongoDatabase database, String collectionName, Class<CT> valueType) {
            return build(database.getCollection(collectionName, valueType), valueType);
        }

        /**
         * Builds a {@link JacksonMongoCollection}. Required parameters are set here.
         *
         * @param database       A client
         * @param valueType      The class of the value type.  Must be annotated with {@link org.mongojack.MongoCollection}.
         * @param <CT>           The value type
         * @return A constructed collection meeting the MongoCollection interface.
         */
        public <CT> JacksonMongoCollection<CT> build(MongoDatabase database, Class<CT> valueType) {
            final org.mongojack.MongoCollection annotation = valueType.getAnnotation(org.mongojack.MongoCollection.class);
            if (annotation == null) {
                throw new IllegalArgumentException("You can only use the builder methods without explicit collection names if you have a class annotated with org.mongojack.MongoCollection");
            }
            return build(database.getCollection(annotation.name(), valueType), valueType);
        }

        /**
         * Builds a {@link JacksonMongoCollection}. Required parameters are set here.
         *
         * @param mongoCollection The underlying collection
         * @param valueType       The value type of the collection
         * @param <CT>            The value type of the collection
         * @return                A constructed collection
         */
        public <CT> JacksonMongoCollection<CT> build(com.mongodb.client.MongoCollection<CT> mongoCollection, Class<CT> valueType) {
            return new JacksonMongoCollection<>(mongoCollection, this.objectMapper, valueType, view);
        }

    }

}

