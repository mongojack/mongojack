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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.mongojack.internal.FetchableDBRef;
import org.mongojack.internal.JacksonCollectionKey;
import org.mongojack.internal.MongoJackModule;
import org.mongojack.internal.object.document.DocumentObjectGenerator;
import org.mongojack.internal.stream.JacksonCodec;
import org.mongojack.internal.util.DocumentSerializationUtils;
import org.mongojack.internal.util.FindIterableDelegate;
import org.mongojack.internal.util.InitializationRequiredForTransformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
 *
 * @author James Roper
 * @since 1.0
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class JacksonMongoCollection<T> {

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = MongoJackModule
        .configure(new ObjectMapper());

    private com.mongodb.client.MongoCollection<T> mongoCollection;
    private final MongoClient mongoClient;
    private final ObjectMapper objectMapper;
    private final JacksonCodecRegistry jacksonCodecRegistry;
    private final Class<?> view;
    private final Class<T> valueClass;
    private final JavaType type;
    private final Map<JacksonCollectionKey, JacksonMongoCollection<?>> referencedCollectionCache =
        new ConcurrentHashMap<>();

    private JacksonMongoCollection(
        com.mongodb.client.MongoCollection<?> mongoCollection,
        ObjectMapper objectMapper,
        Class<T> valueClass,
        Class<?> view
    ) {
        this(mongoCollection, null, objectMapper, valueClass, view);
    }

    private JacksonMongoCollection(
        com.mongodb.client.MongoCollection<?> mongoCollection,
        MongoClient mongoClient,
        ObjectMapper objectMapper,
        Class<T> valueClass,
        Class<?> view
    ) {
        this.objectMapper = objectMapper == null ? DEFAULT_OBJECT_MAPPER : objectMapper;
        this.view = view;
        jacksonCodecRegistry = new JacksonCodecRegistry(this.objectMapper, this.view, this);
        jacksonCodecRegistry.addCodecForClass(valueClass);
        this.mongoCollection = mongoCollection.withDocumentClass(valueClass).withCodecRegistry(jacksonCodecRegistry);
        this.valueClass = valueClass;
        this.type = this.objectMapper.constructType(valueClass);
        this.mongoClient = mongoClient;
    }

    /**
     * Get the underlying mongo collection
     *
     * @return The underlying mongo collection
     */
    public com.mongodb.client.MongoCollection<T> getMongoCollection() {
        return mongoCollection;
    }

    /**
     * Gets the name of the underlying collection
     *
     * @return
     */
    public String getName() {
        return mongoCollection.getNamespace().getCollectionName();
    }

    /**
     * Gets the DB name in which the underlying collection is stored
     *
     * @return
     */
    public String getDatabaseName() {
        return mongoCollection.getNamespace().getDatabaseName();
    }

    /**
     * Inserts an object into the database. If the objects _id is null, the driver will generate one
     *
     * @param object The object to insert
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoWriteException        If the write failed due some other failure specific to the insert command
     * @throws MongoException             If an error occurred
     */
    public void insert(T object) throws MongoException, MongoWriteException, MongoWriteConcernException {
        mongoCollection.insertOne(object);
    }

    /**
     * Inserts an object into the database. If the objects _id is null, the driver will generate one
     *
     * @param object  The object to insert
     * @param concern the write concern
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoWriteException        If the write failed due some other failure specific to the insert command
     * @throws MongoException             If an error occurred
     */
    public void insert(T object, WriteConcern concern)
        throws MongoException, MongoWriteException, MongoWriteConcernException {
        mongoCollection.withWriteConcern(concern).insertOne(object);
    }

    /**
     * Inserts objects into the database. if the objects' _id are null, they will be generated.
     *
     * @param objects The objects to insert
     * @throws MongoBulkWriteException If there's an exception in the bulk write operation
     * @throws MongoException          If an error occurred
     */
    @SafeVarargs
    public final void insert(T... objects) throws MongoException, MongoBulkWriteException {
        ArrayList<T> objectList = new ArrayList<>(objects.length);
        Collections.addAll(objectList, objects);
        mongoCollection.insertMany(objectList);
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
    public final void insert(WriteConcern concern, T... objects)
        throws MongoException, MongoBulkWriteException {
        ArrayList<T> objectList = new ArrayList<>(objects.length);
        Collections.addAll(objectList, objects);
        mongoCollection.withWriteConcern(concern).insertMany(objectList);
    }

    /**
     * Inserts objects into the database. if the objects' _id are null, they will be generated.
     *
     * @param list The objects to insert
     * @throws MongoBulkWriteException If there's an exception in the bulk write operation
     * @throws MongoException          If an error occurred
     */
    public void insert(List<T> list) throws MongoException, MongoBulkWriteException {
        mongoCollection.insertMany(list);
    }

    /**
     * Inserts objects into the database. if the objects' _id are null, they will be generated.
     *
     * @param list    The objects to insert
     * @param concern the write concern
     * @throws MongoBulkWriteException If there's an exception in the bulk write operation
     * @throws MongoException          If an error occurred
     */
    public void insert(List<T> list, WriteConcern concern)
        throws MongoException {
        mongoCollection.withWriteConcern(concern).insertMany(list);
    }

    /**
     * Performs an update operation.
     *
     * @param query    search query for old object to update
     * @param update a update describing the update, which may not be null. The update to apply must include only update operators.
     * @param upsert   if the database should create the element if it does not exist
     * @param concern  the write concern
     * @return The write result
     * @throws MongoWriteException        If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult updateOne(Bson query, Bson update, boolean upsert, WriteConcern concern) throws MongoException, MongoWriteException,
        MongoWriteConcernException {
        if (concern != null) {
            return mongoCollection.withWriteConcern(concern).updateOne(serializeQueryBson(query), serializeUpdateBson(update), new UpdateOptions().upsert(
                upsert));
        } else {
            return mongoCollection.updateOne(serializeQueryBson(query), serializeUpdateBson(update), new UpdateOptions().upsert(
                upsert));
        }
    }

    /**
     * Performs an update operation.
     *
     * @param query    search query for old object to update
     * @param update a update describing the update, which may not be null. The update to apply must include only update operators.
     * @param upsert   if the database should create the element if it does not exist
     * @return The write result
     * @throws MongoWriteException        If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult updateOne(Bson query, Bson update, boolean upsert) throws MongoException, MongoWriteException,
        MongoWriteConcernException {
        return updateOne(query, update, upsert, null);
    }

    /**
     * Performs an update operation.
     *
     * @param query    search query for old object to update
     * @param update a update describing the update, which may not be null. The update to apply must include only update operators.
     * @return The write result
     * @throws MongoWriteException        If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult updateOne(Bson query, Bson update) throws MongoException, MongoWriteException,
        MongoWriteConcernException {
        return updateOne(query, update, false);
    }

    /**
     * Performs an update operation.
     *
     * @param query    search query for old object to update
     * @param document a document describing the update, which may not be null. The update to apply must include only update operators.
     * @param upsert   if the database should create the element if it does not exist
     * @param concern  the write concern
     * @return The UpdateResult
     * @throws MongoWriteException        If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult updateMany(
        Bson query, Bson document,
        boolean upsert, WriteConcern concern
    ) throws MongoException, MongoWriteException, MongoWriteConcernException {
        if (concern != null) {
            return mongoCollection.withWriteConcern(concern).updateMany(serializeQueryBson(query), serializeUpdateBson(document), new UpdateOptions().upsert(
                upsert));
        } else {
            return mongoCollection.updateMany(serializeQueryBson(query), serializeUpdateBson(document), new UpdateOptions().upsert(
                upsert));
        }

    }

    /**
     * Performs an update operation without upsert and default write concern.
     *
     * @param query  search query for old object to update
     * @param object a document describing the update, which may not be null. The update to apply must include only update operators.
     * @return The result
     * @throws MongoWriteException        If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult update(Bson query, Bson object)
        throws MongoException, MongoWriteException, MongoWriteConcernException {
        return updateOne(query, object, false, null);
    }

    /**
     * Performs an update operation.
     *
     * @param _id    The id of the document to update
     * @param update update with which to update <tt>query</tt>
     * @return The write result
     * @throws MongoWriteException        If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult updateById(Object _id, Bson update)
        throws MongoException, MongoWriteException, MongoWriteConcernException {
        return update(
            createIdQuery(_id),
            update
        );
    }

    /**
     * Update all matching records
     *
     * @param query  search query for old update to update
     * @param update update with which to update <tt>query</tt>
     * @return The result
     * @throws MongoWriteException        If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult updateMany(Bson query, Bson update)
        throws MongoException, MongoWriteException, MongoWriteConcernException {
        return updateMany(query, update, false, null);
    }

    /**
     * Performs an update operation, replacing the entire document.
     *
     * @param query   search query for old object to replace
     * @param object  object with which to replace <tt>query</tt>
     * @param upsert  if the database should create the element if it does not exist
     * @param concern the write concern
     * @return The write result
     * @throws MongoWriteException        If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult replaceOne(Bson query, T object, boolean upsert, WriteConcern concern) throws MongoException, MongoWriteException,
        MongoWriteConcernException {
        if (concern != null) {
            return mongoCollection.withWriteConcern(concern).replaceOne(serializeQueryBson(query), object, new ReplaceOptions().upsert(
                upsert));
        } else {
            return mongoCollection.replaceOne(serializeQueryBson(query), object, new ReplaceOptions().upsert(upsert));
        }

    }

    /**
     * Performs an update operation, replacing the entire document.
     *
     * @param query   search query for old object to replace
     * @param object  object with which to replace <tt>query</tt>
     * @param upsert  if the database should create the element if it does not exist
     * @return The write result
     * @throws MongoWriteException        If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult replaceOne(Bson query, T object, boolean upsert) throws MongoException, MongoWriteException,
        MongoWriteConcernException {
        return replaceOne(query, object, upsert, null);
    }

    /**
     * Performs an update operation, replacing the entire document.
     *
     * @param query   search query for old object to replace
     * @param object  object with which to replace <tt>query</tt>
     * @param upsert  if the database should create the element if it does not exist
     * @return The write result
     * @throws MongoWriteException        If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult replaceOne(Bson query, T object) throws MongoException, MongoWriteException,
        MongoWriteConcernException {
        return replaceOne(query, object, false);
    }

    /**
     * Performs an update operation, replacing the entire document, for the document with this _id.
     *
     * @param _id    the _id of the object to replace
     * @param object object with which to replace <tt>query</tt>
     * @return The result
     * @throws MongoWriteException        If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public UpdateResult replaceOneById(Object _id, T object) throws MongoException, MongoWriteException, MongoWriteConcernException {
        return replaceOne(createIdQuery(_id), object, false, null);
    }

    /**
     * Removes objects from the database collection.
     *
     * @param query   the object that documents to be removed must match
     * @param concern WriteConcern for this operation
     * @return The result
     * @throws MongoWriteException        If the write failed due some other failure specific to the delete command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public DeleteResult remove(Bson query, WriteConcern concern) throws MongoException, MongoWriteException, MongoWriteConcernException {
        if (concern != null) {
            return mongoCollection.withWriteConcern(concern).deleteMany(serializeQueryBson(query));
        } else {
            return mongoCollection.deleteMany(serializeQueryBson(query));
        }
    }

    /**
     * Removes objects from the database collection with the default WriteConcern
     *
     * @param query the query that documents to be removed must match
     * @return The Delete result
     * @throws MongoWriteException        If the write failed due some other failure specific to the delete command
     * @throws MongoWriteConcernException If the write failed due being unable to fulfill the write concern
     * @throws MongoException             If an error occurred
     */
    public DeleteResult remove(Bson query) throws MongoException, MongoWriteException, MongoWriteConcernException {
        return remove(query, null);
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
        return remove(createIdQuery(_id));
    }

    /**
     * Finds the first document in the query and updates it.
     *
     * @param query     query to match
     * @param fields    fields to be returned
     * @param sort      sort to apply before picking first document
     * @param update    update to apply. This must contain only update operators
     * @param returnNew if true, the updated document is returned, otherwise the old
     *                  document is returned (or it would be lost forever)
     * @param upsert    do upsert (insert if document not present)
     * @return the object
     */
    public T findAndModify(Bson query, Bson fields, Bson sort, Bson update, boolean returnNew, boolean upsert) {
        return mongoCollection.findOneAndUpdate(serializeQueryBson(query), serializeUpdateBson(update), new FindOneAndUpdateOptions().returnDocument(returnNew
            ? ReturnDocument.AFTER
            : ReturnDocument.BEFORE).projection(fields).sort(sort).upsert(upsert));
    }

    /**
     * Finds a document and deletes it.
     *
     * @param query The query
     * @return the removed object
     */
    public T findAndRemove(Bson query) {
        return mongoCollection.findOneAndDelete(serializeQueryBson(query));
    }

    /**
     * creates an index with default index options
     *
     * @param keys an object with a key set of the fields desired for the index
     * @throws MongoException If an error occurred
     */
    public void createIndex(Bson keys) throws MongoException {
        mongoCollection.createIndex(keys);
    }

    /**
     * Forces creation of an index on a set of fields, if one does not already
     * exist.
     *
     * @param keys    The keys to index
     * @param options The options
     * @throws MongoException If an error occurred
     */
    public void createIndex(Bson keys, IndexOptions options)
        throws MongoException {
        mongoCollection.createIndex(keys, options);
    }


    /**
     * Queries for an object in this collection.
     * <p>
     * <p>
     * An empty DBObject will match every document in the collection. Regardless of fields specified, the _id fields are
     * always returned.
     * </p>
     * <p>
     * An example that returns the "x" and "_id" fields for every document in the collection that has an "x" field:
     * </p>
     *
     * <pre>
     * BasicDBObject keys = new BasicDBObject();
     * keys.put("x", 1);
     *
     * DBCursor cursor = collection.find(new BasicDBObject(), keys);
     * </pre>
     *
     * @param query object for which to search
     * @return a cursor to iterate over results
     */
    public FindIterable<T> find(Bson query) {
        return new FindIterableDelegate<>(mongoCollection.find(serializeQueryBson(query)), objectMapper, type, jacksonCodecRegistry);
    }


    /**
     * Queries for all objects in this collection.
     *
     * @return a cursor which will iterate over every object
     * @throws MongoException If an error occurred
     */
    public FindIterable<T> find() throws MongoException {
        return new FindIterableDelegate<>(mongoCollection.find(), objectMapper, type, jacksonCodecRegistry);
    }

    /**
     * Returns a single object from this collection.
     *
     * @return the object found, or <code>null</code> if the collection is empty
     * @throws MongoException If an error occurred
     */
    public T findOne() throws MongoException {
        return findOne(new Document());
    }

    /**
     * Find an object by the given id
     *
     * @param id The id
     * @return The object
     * @throws MongoException If an error occurred
     */
    public T findOneById(Object id) throws MongoException {
        return findOne(createIdQuery(id));
    }

    /**
     * Returns a single object from this collection matching the query.
     *
     * @param query the query object
     * @return the object found, or <code>null</code> if no such object exists
     */
    public T findOne(Bson query) {
        return this.find(query).first();
    }

    /**
     * Returns a single object from this collection matching the query.
     *
     * @param query   the query object
     * @param project the projection
     * @return the object found, or <code>null</code> if no such object exists
     */
    public T findOne(Bson query, Bson projection) {
        return this.find(query).projection(projection).first();
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
    public UpdateResult save(T object) throws MongoWriteException, MongoWriteConcernException, MongoException {
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
    public UpdateResult save(T object, WriteConcern concern) throws MongoWriteException, MongoWriteConcernException, MongoException {
        final JacksonCodec<T> codec = getValueClassCodec();
        BsonValue _id = codec.getDocumentId(object);
        if (_id == null || _id.isNull()) {
            if (concern == null) {
                this.insert(object);
            } else {
                this.insert(object, concern);
            }
            return UpdateResult.acknowledged(0, 1L, codec.getDocumentId(object));
        } else {
            return this.replaceOne(new Document("_id", _id), object, true, concern);
        }
    }

    /**
     * Drops all indices from this collection
     *
     * @throws MongoException If an error occurred
     */
    public void dropIndexes() throws MongoException {
        mongoCollection.dropIndexes();
    }

    /**
     * Drops an index from this collection
     *
     * @param name the index name
     * @throws MongoException If an error occurred
     */
    public void dropIndex(String name) throws MongoException {
        mongoCollection.dropIndex(name);
    }

    /**
     * Drops (deletes) this collection. Use with care.
     *
     * @throws MongoException If an error occurred
     */
    public void drop() throws MongoException {
        mongoCollection.drop();
    }

    /**
     * Gets a count of documents in the collection
     *
     * @return number of documents that match query
     * @throws MongoException If an error occurred
     */
    public long count() throws MongoException {
        return getCount(new Document());
    }

    /**
     * Gets a count of documents which match the query
     *
     * @param query query to match
     * @return The count
     * @throws MongoException If an error occurred
     */
    public long getCount(Bson query) throws MongoException {
        return mongoCollection.countDocuments(serializeQueryBson(query));
    }

    /**
     * find distinct values for a key
     *
     * @param key The key
     * @return The results
     */
    public <ResultType> List<ResultType> distinct(String key, Class<ResultType> resultClass) {
        return mongoCollection.distinct(key, resultClass).into(new ArrayList<>());
    }

    /**
     * find distinct values for a key
     *
     * @param key   The key
     * @param query query to match
     * @return The results
     */
    public <ResultType> List<ResultType> distinct(String key, Bson query, Class<ResultType> resultClass) {
        return mongoCollection.distinct(key, serializeQueryBson(query), resultClass).into(new ArrayList<>());
    }


    /**
     * Performs a map reduce operation
     *
     * @param mapFunction    - The map function to execute
     * @param reduceFunction - The reduce function to execute
     * @param resultClass    - The class for the expected result type
     * @return MapReduceIterable of the resultClass
     * @throws MongoException
     */
    public <ResultType> MapReduceIterable<ResultType> mapReduce(String mapFunction, String reduceFunction, Class<ResultType> resultClass) throws MongoException {
        return mongoCollection.mapReduce(mapFunction, reduceFunction, resultClass);
    }

    /**
     * Performs an aggregation pipeline against this collection.  This method takes a <code>List&lt;? extends Bson&gt;</code>.  That list
     * can be generated in a number of ways, such as:
     *
     * Using the mongo driver's Aggregates builder:
     *
     * <pre>
     *   Arrays.asList(
     *       Aggregates.match(Filters.eq("categories", "Bakery")),
     *       Aggregates.group("$stars", Accumulators.sum("count", 1))
     *   )
     * </pre>
     *
     * or using documents directly:
     *
     * <pre>
     *   Arrays.asList(
     *       new Document("$match", new Document("categories", "Bakery")),
     *       ...
     *   )
     * </pre>
     *
     * or using MongoJack's Aggregation class
     *
     * <pre>
     *   Aggregation.group("string").set("integer", Group.sum("integer")).sort(DBSort.asc("_id"))
     * </pre>
     *
     * @param pipeline    - This should be a List of Bson Documents in the Mongo aggregation language.
     * @param resultClass - The class for the type that will be returned
     * @return an AggregateIterable with the result objects mapped to the type specified by the resultClass.
     * @throws MongoException If an error occurred
     * @see <a
     * href="http://www.mongodb.org/display/DOCS/Aggregation">http://www.mongodb.org/display/DOCS/Aggregation</a>
     * @since 2.1.0
     */
    public <ResultType> AggregateIterable<ResultType> aggregate(List<? extends Bson> pipeline, Class<ResultType> resultClass)
        throws MongoException {
        initializeIfNecessary(pipeline);
        return mongoCollection.aggregate(pipeline, resultClass);
    }

    private void initializeIfNecessary(Object maybeInitializable) {
        if (maybeInitializable instanceof InitializationRequiredForTransformation) {
            ((InitializationRequiredForTransformation) maybeInitializable).initialize(objectMapper, type, jacksonCodecRegistry);
        }
    }

    /**
     * Set the write concern for this collection. Will be used for writes to
     * this collection. Overrides any setting of write concern at the DB level.
     * See the documentation for {@link WriteConcern} for more information.
     *
     * @param concern write concern to use
     */
    public void setWriteConcern(WriteConcern concern) {
        this.mongoCollection = mongoCollection.withWriteConcern(concern);
    }

    /**
     * Get the write concern for this collection.
     *
     * @return THe write concern
     */
    public WriteConcern getWriteConcern() {
        return mongoCollection.getWriteConcern();
    }

    /**
     * Sets the read preference for this collection. Will be used as default for
     * reads from this collection; overrides DB &amp; Connection level settings. See
     * the * documentation for {@link ReadPreference} for more information.
     *
     * @param preference Read Preference to use
     */
    public void setReadPreference(ReadPreference preference) {
        this.mongoCollection = mongoCollection.withReadPreference(preference);
    }

    /**
     * Gets the read preference
     *
     * @return The read preference
     */
    public ReadPreference getReadPreference() {
        return mongoCollection.getReadPreference();
    }

    /**
     * Creates a document query object for the _id field using the object as the _id. This object is expected to already
     * be in the correct format... Document, Long, String, etc...
     *
     * @param _id
     * @return
     */
    private Bson createIdQuery(Object _id) {
        if (_id instanceof BsonValue) {
            return Filters.eq("_id", _id);
        }
        return Filters.eq("_id", JacksonCodec.constructIdValue(_id, JacksonCodec.getIdElement(valueClass)));
    }

    private JacksonCodec<T> getValueClassCodec() {
        return (JacksonCodec<T>) jacksonCodecRegistry.get(valueClass);
    }

    private Bson convertToDocument(T object) throws MongoException {
        return convertToDocument(object, this.objectMapper, this.view);
    }

    /**
     * This method provides a static way to convert an object into a Document. Defaults will be used for all parameters
     * left null.
     *
     * @param object       The object to convert
     * @param objectMapper The specific Jackson ObjectMapper to use. (Default MongoJack ObjectMapper)
     * @param view         The Jackson View to use in serialization. (Default null)
     * @return
     */
    public static <T> Bson convertToDocument(T object, ObjectMapper objectMapper, Class<?> view) {
        if (object == null) {
            return null;
        }
        if (objectMapper == null) {
            objectMapper = DEFAULT_OBJECT_MAPPER;
        }
        DocumentObjectGenerator generator = new DocumentObjectGenerator();
        try {
            objectMapper.writerWithView(view).writeValue(generator, object);
        } catch (JsonMappingException e) {
            throw new MongoJsonMappingException(e);
        } catch (IOException e) {
            // This shouldn't happen
            throw new MongoException("Unknown error occurred converting BSON to object", e);
        }
        return generator.getDocument();
    }

    /**
     * Serialize the fields of the given object using the object mapper
     * for this collection.
     * This will convert POJOs to DBObjects where necessary.
     *
     * @param value The object to serialize the fields of
     * @return The DBObject, safe for use in a mongo query.
     */
    private Bson serializeQueryBson(Bson value) {
        initializeIfNecessary(value);
        if (value instanceof InitializationRequiredForTransformation) {
            return value;
        }
        return DocumentSerializationUtils.serializeFilter(objectMapper, type, value, jacksonCodecRegistry);
    }


    /**
     * Serialize the fields of the given object using the object mapper
     * for this collection.
     * This will convert POJOs to DBObjects where necessary.
     *
     * @param value The object to serialize the fields of
     * @return The DBObject, safe for use in a mongo query.
     */
    private Bson serializeUpdateBson(Bson value) {
        initializeIfNecessary(value);
        if (value instanceof InitializationRequiredForTransformation) {
            return value;
        }
        return DocumentSerializationUtils.serializeFields(objectMapper, value, jacksonCodecRegistry);
    }


    ObjectMapper getObjectMapper() {
        return objectMapper;
    }


    /**
     * Get the type of this collection
     *
     * @return The type
     */
    public JacksonCollectionKey getCollectionKey() {
        return new JacksonCollectionKey(getMongoCollection().getNamespace().getCollectionName(), getMongoCollection().getNamespace().getDatabaseName(), type);
    }

    /**
     * Get a collection for loading a reference of the given type
     *
     * @param collectionName The name of the collection
     * @param type           The type of the object
     * @param keyType        the type of the id
     * @return The collection
     */
    public <CT> JacksonMongoCollection<CT> getReferenceCollection(
        String collectionName, String databaseName, JavaType type
    ) {
        return getReferenceCollection(new JacksonCollectionKey(collectionName,
            databaseName, type
        ));
    }

    /**
     * Get a collection for loading a reference of the given type
     *
     * @param collectionKey The key for the collection
     * @return The collection
     */
    @SuppressWarnings("unchecked")
    public <CT> JacksonMongoCollection<CT> getReferenceCollection(
        JacksonCollectionKey collectionKey
    ) {
        return (JacksonMongoCollection<CT>) referencedCollectionCache.computeIfAbsent(
            collectionKey,
            (k) -> {
                com.mongodb.client.MongoCollection<CT> referencedCollection;
                if (getMongoClient() == null) {
                    throw new RuntimeException("Cannot resolve DBRefs if JacksonMongoCollection not instantiated with MongoDatabase");
                }
                if (collectionKey.getDbName() == null) {
                    referencedCollection =
                        (com.mongodb.client.MongoCollection<CT>) getMongoClient().getDatabase(getMongoCollection().getNamespace().getDatabaseName())
                            .getCollection(collectionKey.getName(), collectionKey.getType().getRawClass());
                } else {
                    referencedCollection =
                        (com.mongodb.client.MongoCollection<CT>) getMongoClient().getDatabase(collectionKey.getDbName())
                            .getCollection(collectionKey.getName(), collectionKey.getType().getRawClass());
                }
                return new JacksonMongoCollection<>(
                    referencedCollection,
                    getMongoClient(),
                    objectMapper,
                    (Class<CT>) collectionKey.getType().getRawClass(),
                    null
                );
            }
        );
    }

    /**
     * Fetch a collection of dbrefs. This is more efficient than fetching one at
     * a time.
     *
     * @param collection the collection to fetch
     * @param <R>        The type of the reference
     * @return The collection of referenced objcets
     */
    public <R, RK> List<R> fetch(
        Collection<DBRef<R, RK>> collection
    ) {
        return fetch(collection, null);
    }

    /**
     * Fetch a collection of dbrefs. This is more efficient than fetching one at
     * a time.
     *
     * @param collection the collection to fetch
     * @param fields     The fields to retrieve for each of the documents
     * @return The collection of referenced objcets
     */
    @SuppressWarnings("unchecked")
    public <R, RK> List<R> fetch(
        Collection<org.mongojack.DBRef<R, RK>> collection, DBObject fields
    ) {

        if (!collection.stream().allMatch((ref -> ref instanceof FetchableDBRef))) {
            throw new IllegalArgumentException("There are non-fetchable DBRefs in the input collection");
        }

        return collection.stream()
            .map(ref -> (FetchableDBRef<R, RK>) ref)
            .collect(Collectors.groupingBy(FetchableDBRef::getCollectionKey))
            .entrySet()
            .stream()
            .flatMap(
                entry -> (Stream<R>) StreamSupport.stream(
                    getReferenceCollection(entry.getKey()).find(Filters.in("_id", entry.getValue().stream().map(DBRef::getId).collect(Collectors.toList()))).spliterator(),
                    false
                )
            )
            .collect(Collectors.toList());
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public Class<T> getValueClass() {
        return valueClass;
    }

    @Override
    public String toString() {
        return String.format("%s<%s, %s>", getClass().getName(), getMongoCollection().getNamespace().getFullName(), valueClass.getName());
    }

    public static <CT> List<CT> resultsToList(FindIterable<CT> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Creates builder to build {@link JacksonMongoCollection}.
     *
     * @return created builder
     */
    public static <T> JacksonMongoCollectionBuilder<T> builder() {
        return new JacksonMongoCollectionBuilder<>();
    }

    /**
     * Builder to build {@link JacksonMongoCollection}.
     */
    public static final class JacksonMongoCollectionBuilder<T> {
        private ObjectMapper objectMapper;
        private Class<?> view;
        private MongoClient client;

        private JacksonMongoCollectionBuilder() {
        }

        public JacksonMongoCollectionBuilder<T> withObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public JacksonMongoCollectionBuilder<T> withView(Class<?> view) {
            this.view = view;
            return this;
        }

        public JacksonMongoCollectionBuilder<T> withClient(MongoClient client) {
            this.client = client;
            return this;
        }

        /**
         * Builds a {@link JacksonMongoCollection}. Required parameters are set here.
         *
         * @param mongoCollection - The MongoCollection that {@link JacksonMongoCollection} will wrap.
         * @param valueType       - The type that this should serialize and deserialize to.
         * @return A new instance of a JacksonMongoCollection
         */
        public <CT> JacksonMongoCollection<CT> build(com.mongodb.client.MongoCollection<CT> mongoCollection, Class<CT> valueType) {
            return new JacksonMongoCollection<>(mongoCollection, client, this.objectMapper, valueType, view);
        }
    }
}
