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
import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.annotations.Beta;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.conversions.Bson;
import org.mongojack.internal.MongoJackModule;
import org.mongojack.internal.object.document.DocumentObjectGenerator;
import org.mongojack.internal.object.document.DocumentObjectTraversingParser;
import org.mongojack.internal.query.QueryCondition;
import org.mongojack.internal.stream.JacksonCodec;
import org.mongojack.internal.util.DocumentSerializationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A DBCollection that marshals/demarshals objects to/from Jackson annotated
 * classes. It provides a very thin wrapper over an existing MongoCollection.
 *
 * A JacksonMongoCollection is threadsafe, with a few caveats:
 *
 * If you pass your own ObjectMapper to it, it is not thread safe to reconfigure
 * that ObjectMapper at all after creating it. The setWritePreference and a few
 * other methods on JacksonMongoCollection should not be called from multiple
 * threads
 *
 * @author James Roper
 * @since 1.0
 */
@Beta
public class JacksonMongoCollection<T> {

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = MongoJackModule
            .configure(new ObjectMapper());

    private com.mongodb.client.MongoCollection<T> mongoCollection;
    private final ObjectMapper objectMapper;
    private final Class<?> view;
    private final Class<T> valueClass;
    private final JavaType type;

    private JacksonMongoCollection(
            com.mongodb.client.MongoCollection<?> mongoCollection,
            ObjectMapper objectMapper,
            Class<T> valueClass,
            Class<?> view) {
        this.objectMapper = objectMapper == null ? DEFAULT_OBJECT_MAPPER : objectMapper;
        this.view = view;
        JacksonCodecRegistry jacksonCodecRegistry = new JacksonCodecRegistry(this.objectMapper, this.view);
        jacksonCodecRegistry.addCodecForClass(valueClass);
        this.mongoCollection = mongoCollection.withDocumentClass(valueClass).withCodecRegistry(jacksonCodecRegistry);;
        this.valueClass = valueClass;
        this.type = this.objectMapper.constructType(valueClass);
    }


    /**
     * Get the underlying mongo collection
     *
     * @return The underlying mongo collection
     */
    public com.mongodb.client.MongoCollection<?> getMongoCollection() {
        return mongoCollection;
    }

    /**
     * Inserts an object into the database. If the objects _id is null, the driver will generate one
     *
     * @param object
     *            The object to insert
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the insert command
     * @throws MongoException
     *             If an error occurred
     */
    public void insert(T object) throws MongoException, MongoWriteException, MongoWriteConcernException {
        mongoCollection.insertOne(object);
    }

    /**
     * Inserts an object into the database. If the objects _id is null, the driver will generate one
     *
     * @param object
     *            The object to insert
     * @param concern
     *            the write concern
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the insert command
     * @throws MongoException
     *             If an error occurred
     */
    public void insert(T object, WriteConcern concern)
            throws MongoException, MongoWriteException, MongoWriteConcernException {
        mongoCollection.withWriteConcern(concern).insertOne(object);
    }

    /**
     * Inserts objects into the database. if the objects' _id are null, they will be generated.
     *
     * @param objects
     *            The objects to insert
     * @throws MongoBulkWriteException
     *             If there's an exception in the bulk write operation
     * @throws MongoException
     *             If an error occurred
     *
     */
    public void insert(@SuppressWarnings("unchecked") T... objects) throws MongoException, MongoBulkWriteException {
        ArrayList<T> objectList = new ArrayList<>(objects.length);
        for (T object : objects) {
            objectList.add(object);
        }
        mongoCollection.insertMany(objectList);
    }

    /**
     * Inserts objects into the database. if the objects' _id are null, they will be generated.
     *
     * @param objects
     *            The objects to insert
     * @param concern
     *            the write concern
     * @throws MongoBulkWriteException
     *             If there's an exception in the bulk write operation
     * @throws MongoException
     *             If an error occurred
     */
    public void insert(WriteConcern concern, @SuppressWarnings("unchecked") T... objects)
            throws MongoException, MongoBulkWriteException {
        ArrayList<T> objectList = new ArrayList<>(objects.length);
        for (T object : objects) {
            objectList.add(object);
        }
        mongoCollection.withWriteConcern(concern).insertMany(objectList);
    }

    /**
     * Inserts objects into the database. if the objects' _id are null, they will be generated.
     *
     * @param list
     *            The objects to insert
     * @throws MongoBulkWriteException
     *             If there's an exception in the bulk write operation
     * @throws MongoException
     *             If an error occurred
     */
    public void insert(List<T> list) throws MongoException, MongoBulkWriteException {
        mongoCollection.insertMany(list);
    }

    /**
     * Inserts objects into the database. if the objects' _id are null, they will be generated.
     *
     * @param list
     *            The objects to insert
     * @param concern
     *            the write concern
     * @throws MongoBulkWriteException
     *             If there's an exception in the bulk write operation
     * @throws MongoException
     *             If an error occurred
     */
    public void insert(List<T> list, WriteConcern concern)
            throws MongoException {
        mongoCollection.withWriteConcern(concern).insertMany(list);
    }

    /**
     * Performs an update operation.
     *
     * @param query
     *            search query for old object to update
     * @param document
     *            a document describing the update, which may not be null. The update to apply must include only update operators.
     * @param upsert
     *            if the database should create the element if it does not exist
     * @param concern
     *            the write concern
     * @return The write result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult updateOne(Document query, Document document, boolean upsert, WriteConcern concern) throws MongoException, MongoWriteException,
            MongoWriteConcernException {
        query = serializeFields(query);
        if (concern != null) {
            return mongoCollection.withWriteConcern(concern).updateOne(serializeFields(query), serializeFields(document), new UpdateOptions().upsert(
                    upsert));
        } else {
            return mongoCollection.updateOne(serializeFields(query), serializeFields(document), new UpdateOptions().upsert(
                    upsert));
        }
    }

    /**
     * Performs an update operation.
     *
     * @param query
     *            search query for old object to update
     * @param document
     *            a document describing the update, which may not be null. The update to apply must include only update operators.
     * @param upsert
     *            if the database should create the element if it does not exist
     * @param concern
     *            the write concern
     * @return The UpdateResult
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult updateMany(Document query, Document document,
            boolean upsert, WriteConcern concern) throws MongoException, MongoWriteException, MongoWriteConcernException {
        query = serializeFields(query);
        if (concern != null) {
            return mongoCollection.withWriteConcern(concern).updateMany(serializeFields(query), serializeFields(document), new UpdateOptions().upsert(
                    upsert));
        } else {
            return mongoCollection.updateMany(serializeFields(query), serializeFields(document), new UpdateOptions().upsert(
                    upsert));
        }

    }

    /**
     * Performs an update operation.
     *
     * @param query
     *            search query for old object to update
     * @param update
     *            update with which to update <tt>query</tt>
     * @param upsert
     *            if the database should create the element if it does not exist
     * @param concern
     *            the write concern
     * @return The UpdateResult
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult updateOne(DBQuery.Query query, DBUpdate.Builder update, boolean upsert, WriteConcern concern) throws MongoException,
            MongoWriteException, MongoWriteConcernException {
        if (concern != null) {
            return mongoCollection.withWriteConcern(concern).updateOne(serializeQuery(query), update.serializeAndGetAsDocument(objectMapper, type),
                    new UpdateOptions().upsert(
                            upsert));
        } else {
            return mongoCollection.updateOne(serializeQuery(query), update.serializeAndGetAsDocument(objectMapper, type),
                    new UpdateOptions().upsert(
                            upsert));
        }

    }

    /**
     * Performs an update operation.
     *
     * @param query
     *            search query for old object to update
     * @param update
     *            update with which to update <tt>query</tt>
     * @param upsert
     *            if the database should create the element if it does not exist
     * @param concern
     *            the write concern
     * @return The UpdateResult
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult updateMany(DBQuery.Query query, DBUpdate.Builder update, boolean upsert, WriteConcern concern) throws MongoException,
            MongoWriteException, MongoWriteConcernException {
        if (concern != null) {
            return mongoCollection.withWriteConcern(concern).updateMany(serializeQuery(query), update.serializeAndGetAsDocument(objectMapper, type),
                    new UpdateOptions().upsert(
                            upsert));
        } else {
            return mongoCollection.updateMany(serializeQuery(query), update.serializeAndGetAsDocument(objectMapper, type), new UpdateOptions().upsert(
                    upsert));
        }

    }

    /**
     * Performs an update operation without upsert and default write concern.
     *
     * @param query
     *            search query for old object to update
     * @param object
     *            a document describing the update, which may not be null. The update to apply must include only update operators.
     * @return The result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult update(Document query, Document object)
            throws MongoException, MongoWriteException, MongoWriteConcernException {
        return updateOne(query, object, false, null);
    }

    /**
     * Performs an update operation.
     *
     * @param query
     *            search query for old object to update
     * @param update
     *            update with which to update <tt>query</tt>
     * @return The update result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult update(DBQuery.Query query, DBUpdate.Builder update)
            throws MongoException, MongoWriteException, MongoWriteConcernException {
        return this.updateOne(query, update, false, null);
    }

    /**
     * Performs an update operation.
     *
     * @param _id
     *            The id of the document to update
     * @param update
     *            update with which to update <tt>query</tt>
     * @return The write result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult updateById(Object _id, DBUpdate.Builder update)
            throws MongoException, MongoWriteException, MongoWriteConcernException {
        return this.update(createIdQuery(_id),
                update.serializeAndGetAsDocument(objectMapper, type));
    }

    /**
     * Update all matching records
     *
     * @param query
     *            search query for old object to update
     * @param object
     *            object with which to update <tt>query</tt>
     * @return The result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult updateMany(Document query, Document object)
            throws MongoException, MongoWriteException, MongoWriteConcernException {
        return updateMany(query, object, false, null);
    }

    /**
     * Update all matching records
     *
     * @param query
     *            search query for old object to update
     * @param update
     *            update with which to update <tt>query</tt>
     * @return The write result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult updateMany(DBQuery.Query query, DBUpdate.Builder update) throws MongoException, MongoWriteException,
            MongoWriteConcernException {
        return updateMany(query, update, false, null);
    }

    /**
     * Performs an update operation, replacing the entire document.
     *
     * @param query
     *            search query for old object to replace
     * @param object
     *            object with which to replace <tt>query</tt>
     * @param upsert
     *            if the database should create the element if it does not exist
     * @param concern
     *            the write concern
     * @return The write result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult replaceOne(DBQuery.Query query, T object, boolean upsert, WriteConcern concern) throws MongoException, MongoWriteException,
            MongoWriteConcernException {
        if (concern != null) {
            return mongoCollection.withWriteConcern(concern).replaceOne(serializeQuery(query), object, new ReplaceOptions().upsert(
                    upsert));
        } else {
            return mongoCollection.replaceOne(serializeQuery(query), object, new ReplaceOptions().upsert(upsert));
        }

    }

    /**
     * Performs an update operation, replacing the entire document.
     *
     * @param query
     *            search query for old object to replace
     * @param object
     *            object with which to replace <tt>query</tt>
     * @param upsert
     *            if the database should create the element if it does not exist
     * @param concern
     *            the write concern
     * @return The write result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult replaceOne(Document query, T object, boolean upsert, WriteConcern concern) throws MongoException, MongoWriteException,
            MongoWriteConcernException {
        query = serializeFields(query);
        if (concern != null) {
            return mongoCollection.withWriteConcern(concern).replaceOne(query, object, new ReplaceOptions().upsert(
                    upsert));
        } else {
            return mongoCollection.replaceOne(query, object, new ReplaceOptions().upsert(upsert));
        }

    }

    /**
     * Performs an update operation, replacing the entire document.
     *
     * @param query
     *            search query for old object to replace
     * @param object
     *            object with which to replace <tt>query</tt>
     * @return The result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult replaceOne(DBQuery.Query query, T object)
            throws MongoException, MongoWriteException, MongoWriteConcernException {
        return replaceOne(query, object, false, null);
    }

    /**
     * Performs an update operation, replacing the entire document, for the document with this _id.
     *
     * @param _id
     *            the _id of the object to replace
     * @param object
     *            object with which to replace <tt>query</tt>
     * @return The result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the update command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult replaceOneById(Object _id, T object) throws MongoException, MongoWriteException, MongoWriteConcernException {
        return replaceOne(createIdQuery(_id), object, false, null);
    }

    /**
     * Removes objects from the database collection.
     *
     * @param query
     *            the object that documents to be removed must match
     * @param concern
     *            WriteConcern for this operation
     * @return The result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the delete command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public DeleteResult remove(Document query, WriteConcern concern) throws MongoException, MongoWriteException, MongoWriteConcernException {
        query = serializeFields(query);
        if (concern != null) {
            return mongoCollection.withWriteConcern(concern).deleteMany(query);
        } else {
            return mongoCollection.deleteMany(query);
        }
    }

    /**
     * Removes objects from the database collection.
     *
     * @param query
     *            the query
     * @param concern
     *            WriteConcern for this operation
     * @return The result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the delete command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public DeleteResult remove(DBQuery.Query query, WriteConcern concern)
            throws MongoException, MongoWriteException, MongoWriteConcernException {
        if (concern != null) {
            return mongoCollection.withWriteConcern(concern).deleteMany(serializeQuery(query));
        } else {
            return mongoCollection.deleteMany(serializeQuery(query));
        }
    }

    /**
     * Removes objects from the database collection with the default WriteConcern
     *
     * @param query
     *            the query that documents to be removed must match
     * @return The Delete result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the delete command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public DeleteResult remove(Document query) throws MongoException, MongoWriteException, MongoWriteConcernException {
        return remove(query, null);
    }

    /**
     * Removes objects from the database collection with the default WriteConcern
     *
     * @param query
     *            the query
     * @return The delete result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the delete command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public DeleteResult remove(DBQuery.Query query) throws MongoException, MongoWriteException, MongoWriteConcernException {
        return remove(query, null);
    }

    /**
     * Removes object from the database collection with the default WriteConcern
     *
     * @param _id
     *            the id of the document to remove
     * @return The delete result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the delete command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public DeleteResult removeById(Object _id) throws MongoException, MongoWriteException, MongoWriteConcernException {
        return remove(createIdQuery(_id));
    }

    /**
     * Finds the first document in the query and updates it.
     *
     * @param query
     *            query to match
     * @param fields
     *            fields to be returned
     * @param sort
     *            sort to apply before picking first document
     * @param update
     *            update to apply. This must contain only update operators
     * @param returnNew
     *            if true, the updated document is returned, otherwise the old
     *            document is returned (or it would be lost forever)
     * @param upsert
     *            do upsert (insert if document not present)
     * @return the object
     */
    public T findAndModify(Document query, Document fields, Document sort, Document update, boolean returnNew, boolean upsert) {
        return mongoCollection.findOneAndUpdate(serializeFields(query), update, new FindOneAndUpdateOptions().returnDocument(returnNew
                ? ReturnDocument.AFTER
                : ReturnDocument.BEFORE).projection(fields).sort(sort).upsert(upsert));
    }

    /**
     * Finds the first document in the query and updates it.
     *
     * @param query
     *            query to match
     * @param fields
     *            fields to be returned
     * @param sort
     *            sort to apply before picking first document
     * @param update
     *            update to apply
     * @param returnNew
     *            if true, the updated document is returned, otherwise the old
     *            document is returned (or it would be lost forever)
     * @param upsert
     *            do upsert (insert if document not present)
     * @return the object
     */
    public T findAndModify(DBQuery.Query query, Document fields, Document sort, DBUpdate.Builder update, boolean returnNew, boolean upsert) {
        return mongoCollection.findOneAndUpdate(serializeQuery(query), update.serializeAndGetAsDocument(objectMapper, type),
                new FindOneAndUpdateOptions().returnDocument(
                        returnNew
                                ? ReturnDocument.AFTER
                                : ReturnDocument.BEFORE).projection(fields).sort(sort).upsert(upsert));
    }

    /**
     * Finds the first document in the query and updates it.
     *
     * @param query
     *            query to match
     * @param fields
     *            fields to be returned
     * @param sort
     *            sort to apply before picking first document
     * @param update
     *            update to apply
     * @param returnNew
     *            if true, the updated document is returned, otherwise the old
     *            document is returned (or it would be lost forever)
     * @param upsert
     *            do upsert (insert if document not present)
     * @return the object
     */
    public T findAndModify(Document query, Document fields, Document sort, DBUpdate.Builder update, boolean returnNew,
            boolean upsert) {
        return mongoCollection.findOneAndUpdate(serializeFields(query), update.serializeAndGetAsDocument(objectMapper, type),
                new FindOneAndUpdateOptions().returnDocument(
                        returnNew
                                ? ReturnDocument.AFTER
                                : ReturnDocument.BEFORE).projection(fields).sort(sort).upsert(upsert));
    }

    /**
     * Finds a document and deletes it.
     *
     * @param query
     *            The query
     * @return the removed object
     */
    public T findAndRemove(Document query) {
        return mongoCollection.findOneAndDelete(serializeFields(query));
    }

    /**
     * Finds a document and deletes it.
     *
     * @param query
     *            The query
     * @return the removed object
     */
    public T findAndRemove(DBQuery.Query query) {
        return mongoCollection.findOneAndDelete(serializeQuery(query));
    }

    /**
     * creates an index with default index options
     *
     * @param keys
     *            an object with a key set of the fields desired for the index
     * @throws MongoException
     *             If an error occurred
     */
    public void createIndex(Document keys) throws MongoException {
        mongoCollection.createIndex(keys);
    }

    /**
     * Forces creation of an index on a set of fields, if one does not already
     * exist.
     *
     * @param keys
     *            The keys to index
     * @param options
     *            The options
     * @throws MongoException
     *             If an error occurred
     */
    public void createIndex(Document keys, IndexOptions options)
            throws MongoException {
        mongoCollection.createIndex(keys, options);
    }



    /**
     * Queries for an object in this collection.
     *
     * @param query
     *            object for which to search
     * @return an iterator over the results
     * @throws MongoException
     *             If an error occurred
     */
    public FindIterable<T> find(DBQuery.Query query)
            throws MongoException {
        return mongoCollection.find(serializeQuery(query));
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
     * @param query
     *            object for which to search
     * @return a cursor to iterate over results
     */
    public FindIterable<T> find(Document query) {
        return mongoCollection.find(serializeFields(query));
    }


    /**
     * Queries for all objects in this collection.
     *
     * @return a cursor which will iterate over every object
     * @throws MongoException
     *             If an error occurred
     */
    public FindIterable<T> find() throws MongoException {
        return mongoCollection.find();
    }

    /**
     * Returns a single object from this collection.
     *
     * @return the object found, or <code>null</code> if the collection is empty
     * @throws MongoException
     *             If an error occurred
     */
    public T findOne() throws MongoException {
        return findOne(new Document());
    }

    /**
     * Find an object by the given id
     *
     * @param id
     *            The id
     * @return The object
     * @throws MongoException
     *             If an error occurred
     */
    public T findOneById(Object id) throws MongoException {
        return findOne(createIdQuery(id));
    }

    /**
     * Returns a single object from this collection matching the query.
     *
     * @param query
     *            the query object
     * @return the object found, or <code>null</code> if no such object exists
     */
    public T findOne(Document query) {
        return this.find(query).first();
    }

    /**
     * Returns a single object from this collection matching the query.
     *
     * @param query
     *            the query object
     * @return the object found, or <code>null</code> if no such object exists
     */
    public T findOne(DBQuery.Query query) {
        return this.find(query).first();
    }

    /**
     * Saves and object to this collection (does insert or update based on the object _id). Uses default write concern.
     *
     * @param object
     *            the object to save. will add <code>_id</code> field to object if
     *            needed
     * @return The UpdateResult result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the delete command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult save(T object) throws MongoWriteException, MongoWriteConcernException, MongoException {
        return this.save(object, null);
    }

    /**
     * Saves an object to this collection (does insert or update based on the
     * object _id).
     *
     * @param object
     *            the <code>DBObject</code> to save
     * @param concern
     *            the write concern
     * @return The UpdateResult result
     * @throws MongoWriteException
     *             If the write failed due some other failure specific to the delete command
     * @throws MongoWriteConcernException
     *             If the write failed due being unable to fulfill the write concern
     * @throws MongoException
     *             If an error occurred
     */
    public UpdateResult save(T object, WriteConcern concern) throws MongoWriteException, MongoWriteConcernException, MongoException {
        Object _id;
        @SuppressWarnings("unchecked")
        final Codec<T> codec = getMongoCollection().getCodecRegistry().get((Class<T>) object.getClass());
        if (codec instanceof CollectibleCodec) {
            _id = JacksonCodec.extractValueEx(((CollectibleCodec<T>) codec).getDocumentId(object));
        } else {
            Document dbObject = convertToDocument(object);
            _id = dbObject.get("_id");
        }
        if(_id == null) {
            if (concern == null) {
                this.insert(object);
            } else {
                this.insert(object, concern);
            }
            if (codec instanceof CollectibleCodec) {
                return UpdateResult.acknowledged(0, 1L, ((CollectibleCodec<T>)codec).getDocumentId(object));
            } else {
                return UpdateResult.acknowledged(0, 1L, null);
            }
        } else {
            return this.replaceOne(new Document("_id", _id), object, true, concern);
        }
    }

    /**
     * Drops all indices from this collection
     *
     * @throws MongoException
     *             If an error occurred
     */
    public void dropIndexes() throws MongoException {
        mongoCollection.dropIndexes();
    }

    /**
     * Drops an index from this collection
     *
     * @param name
     *            the index name
     * @throws MongoException
     *             If an error occurred
     */
    public void dropIndex(String name) throws MongoException {
        mongoCollection.dropIndex(name);
    }

    /**
     * Drops (deletes) this collection. Use with care.
     *
     * @throws MongoException
     *             If an error occurred
     */
    public void drop() throws MongoException {
        mongoCollection.drop();
    }

    /**
     * Gets a count of documents in the collection
     *
     * @return number of documents that match query
     * @throws MongoException
     *             If an error occurred
     */
    public long count() throws MongoException {
        return getCount(new Document());
    }

    /**
     * Gets a count of documents which match the query
     *
     * @param query
     *            query to match
     * @return The count
     * @throws MongoException
     *             If an error occurred
     */
    public long getCount(Document query) throws MongoException {
        return mongoCollection.count(query);
    }

    /**
     * Gets a count of documents which match the query
     *
     * @param query
     *            query to match
     * @return The count
     * @throws MongoException
     *             If an error occurred
     */
    public long getCount(DBQuery.Query query) throws MongoException {
        return mongoCollection.count(serializeQuery(query));
    }

    /**
     * find distinct values for a key
     *
     * @param key
     *            The key
     * @return The results
     */
    public <ResultType> List<ResultType> distinct(String key, Class<ResultType> resultClass) {
        return mongoCollection.distinct(key, resultClass).into(new ArrayList<>());
    }

    /**
     * find distinct values for a key
     *
     * @param key
     *            The key
     * @param query
     *            query to match
     * @return The results
     */
    public <ResultType> List<ResultType> distinct(String key, Document query, Class<ResultType> resultClass) {
        return mongoCollection.distinct(key, serializeFields(query), resultClass).into(new ArrayList<>());
    }


    /**
     * Performs a map reduce operation
     *
     * @param mapFunction - The map function to execute
     * @param reduceFunction - The reduce function to execute
     * @param resultClass - The class for the expected result type
     * @return MapReduceIterable of the resultClass
     * @throws MongoException
     */
    public <ResultType> MapReduceIterable<ResultType> mapReduce(String mapFunction, String reduceFunction, Class<ResultType> resultClass) throws MongoException {
        return mongoCollection.mapReduce(mapFunction, reduceFunction, resultClass);
    }

    /**
     * Performs an aggregation pipeline against this collection.
     *
     * @param pipeline - This should be a List of Bson Documents in the Mongo aggregation language.
     * @param resultClass - The class for the type that will be returned
     * @return an AggregateIterable with the result objects mapped to the type specified by the resultClass.
     * @throws MongoException
     *             If an error occurred
     * @see <a
     *      href="http://www.mongodb.org/display/DOCS/Aggregation">http://www.mongodb.org/display/DOCS/Aggregation</a>
     * @since 2.1.0
     */

    public <ResultType> AggregateIterable<ResultType> aggregate(List<? extends Bson> pipeline, Class<ResultType> resultClass)
            throws MongoException {

        return mongoCollection.aggregate(pipeline, resultClass);
    }

    /**
     *
     * @param pipeline - This is a MongoJack Aggregation.Pipeline
     * @param resultClass - Class of the results from the aggregationt.
     * @return an AggregationIterable with result object mapped to the type specified by the resultClass.
     * @throws MongoException
     */
    public <ResultType> AggregateIterable<ResultType> aggregate(Aggregation.Pipeline<?> pipeline, Class<ResultType> resultClass)
            throws MongoException {
        return mongoCollection.aggregate(serializePipeline(pipeline), resultClass);
    }

    /**
     * Set the write concern for this collection. Will be used for writes to
     * this collection. Overrides any setting of write concern at the DB level.
     * See the documentation for {@link WriteConcern} for more information.
     *
     * @param concern
     *            write concern to use
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
     * @param preference
     *            Read Preference to use
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
    private Document createIdQuery(Object _id) {
        return new Document("_id", _id);
    }

    private Document convertToDocument(T object) throws MongoException {
        return convertToDocument(object, this.objectMapper, this.view);
    }

    /**
     * This method provides a static way to convert an object into a Document. Defaults will be used for all parameters
     * left null.
     *
     * @param object The object to convert
     * @param objectMapper The specific Jackson ObjectMapper to use. (Default MongoJack ObjectMapper)
     * @param view The Jackson View to use in serialization. (Default null)
     * @return
     */
    public static <T> Document convertToDocument(T object, ObjectMapper objectMapper, Class<?> view) {
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
     * Convert a Document, normally a query result to the object type for this
     * collection using the Jackson ObjectMapper for this collection.
     *
     * @param document The Document to convert
     * @return A converted instance of the object type of this class.
     * @throws MongoException
     */
    private T convertFromDocument(Document document) throws MongoException {
        return convertFromDocument(document, this.valueClass, this.objectMapper, this.view);
    }

    /**
     * This method provides a static method to convert a DBObject into a given class. If the ObjectMapper is null, use a
     * default ObjectMapper
     *
     * @param document
     * @param clazz
     * @param objectMapper
     * @param view
     * @return
     * @throws MongoException
     */
    public static <S> S convertFromDocument(Document document, Class<S> clazz, ObjectMapper objectMapper, Class<?> view) throws MongoException {
        if (document == null) {
            return null;
        }
        if (objectMapper == null)
            objectMapper = DEFAULT_OBJECT_MAPPER;
        try {
            return objectMapper.readerWithView(view).readValue(new DocumentObjectTraversingParser(document, objectMapper), clazz);
        } catch (JsonMappingException e) {
            throw new MongoJsonMappingException(e);
        } catch (IOException e) {
            // This shouldn't happen
            throw new MongoException(
                    "Unknown error occurred converting BSON to object", e);
        }
    }

    /**
     * Serialize the fields of the given object using the object mapper
     * for this collection.
     * This will convert POJOs to DBObjects where necessary.
     *
     * @param value The object to serialize the fields of
     * @return The DBObject, safe for use in a mongo query.
     */
    public Document serializeFields(Document value) {
        return DocumentSerializationUtils.serializeFields(objectMapper, value);
    }


    /**
     * Serialize the given DBQuery.Query using the object mapper
     * for this collection.
     *
     * @param query The DBQuery.Query to serialize.
     * @return The query as a serialized DBObject ready to pass to mongo.
     */
    public Document serializeQuery(DBQuery.Query query) {
        return DocumentSerializationUtils.serializeQuery(objectMapper, type, query);
    }

    Object serializeQueryCondition(String key, QueryCondition condition) {
        return DocumentSerializationUtils.serializeQueryCondition(objectMapper, type,
                key, condition);
    }

    public List<Document> serializePipeline(Aggregation.Pipeline<?> pipeline) {
        return DocumentSerializationUtils.serializePipeline(objectMapper, type, pipeline);
    }

    ObjectMapper getObjectMapper() {
        return objectMapper;
    }



    /**
     * Creates builder to build {@link JacksonMongoCollection}.
     * @return created builder
     */
    public static <T> JacksonMongoCollectionBuilder<T> builder() {
        return new JacksonMongoCollectionBuilder<T>();
    }

    /**
     * Builder to build {@link JacksonMongoCollection}.
     */
    public static final class JacksonMongoCollectionBuilder<T> {
        private ObjectMapper objectMapper;
        private Class<?> view;

        private JacksonMongoCollectionBuilder() {}

        public JacksonMongoCollectionBuilder<T> withObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public JacksonMongoCollectionBuilder<T> withView(Class<?> view) {
            this.view = view;
            return this;
        }

        /**
         * Builds a {@link JacksonMongoCollection}. Required parameters are set here.
         *
         * @param mongoCollection - The MongoCollection that {@link JacksonMongoCollection} will wrap.
         * @param valueType - The type that this should serialize and deserialize to.
         * @return A new instance of a JacksonMongoCollection
         */
        public JacksonMongoCollection<T> build(com.mongodb.client.MongoCollection<?> mongoCollection, Class<T> valueType) {
            return new JacksonMongoCollection<T>(mongoCollection, this.objectMapper, valueType, view);
        }
    }
}
