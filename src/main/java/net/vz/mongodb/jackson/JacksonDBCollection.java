/*
 * Copyright 2011 VZ Netzwerke Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.vz.mongodb.jackson;

import com.mongodb.*;
import net.vz.mongodb.jackson.internal.FetchableDBRef;
import net.vz.mongodb.jackson.internal.util.IdHandler;
import net.vz.mongodb.jackson.internal.util.IdHandlerFactory;
import net.vz.mongodb.jackson.internal.JacksonCollectionKey;
import net.vz.mongodb.jackson.internal.MongoAnnotationIntrospector;
import net.vz.mongodb.jackson.internal.MongoJacksonHandlerInstantiator;
import net.vz.mongodb.jackson.internal.MongoJacksonMapperModule;
import net.vz.mongodb.jackson.internal.object.BsonObjectGenerator;
import net.vz.mongodb.jackson.internal.object.BsonObjectTraversingParser;
import net.vz.mongodb.jackson.internal.stream.JacksonDBObject;
import net.vz.mongodb.jackson.internal.stream.JacksonDecoderFactory;
import net.vz.mongodb.jackson.internal.util.SerializationUtils;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A DBCollection that marshals/demarshals objects to/from Jackson annotated classes.  It provides a very thin wrapper
 * over an existing DBCollection.
 *
 * @author James Roper
 * @since 1.0
 */
public class JacksonDBCollection<T, K> {

    public enum Feature {
        /**
         * Deserialise objects directly from the MongoDB stream.  This is the default, as it performs the best.  If set
         * to false, then it uses the MongoDB driver to deserialise objects to DBObjects, and then traverses those
         * objects to do the Jackson parsing.  This may be desirable, for example, when auto hydrating of objects is
         * enabled, because in order to hydrate objects, a second connection needs to be made to MongoDB, which has the
         * potential to deadlock when the connection pool gets exhausted when using stream deserialization.  Using
         * object deserialization, the hydration occurs after the connection to load the object has been returned to
         * the pool.
         */
        USE_STREAM_DESERIALIZATION(true),

        /**
         * Automatically hydrate all DB references.  This is not advised, either declare the objects to be of type
         * {@link net.vz.mongodb.jackson.DBRef}, and hydrate by calling fetch(), or declare it as the referenced type
         * and use {@link net.vz.mongodb.jackson.JacksonDBCollection.hydrate()} to hydrate the referenced types.
         * <p/>
         * If using this feature, it is strongly recommended that you set {@link Feature.USE_STREAM_DESERIALIZATION} to
         * false, to avoid potential deadlocks.
         */
        AUTO_HYDRATE(false);

        Feature(boolean enabledByDefault) {
            this.enabledByDefault = enabledByDefault;
        }

        private final boolean enabledByDefault;

        public boolean isEnabledByDefault() {
            return enabledByDefault;
        }
    }

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

    static {
        // Configure to use the object id annotation introspector
        DEFAULT_OBJECT_MAPPER.registerModule(MongoJacksonMapperModule.INSTANCE);
        DEFAULT_OBJECT_MAPPER.setHandlerInstantiator(new MongoJacksonHandlerInstantiator(
                new MongoAnnotationIntrospector(DEFAULT_OBJECT_MAPPER.getDeserializationConfig())));
    }

    private final DBCollection dbCollection;
    private final JavaType type;
    private final JavaType keyType;
    private final ObjectMapper objectMapper;
    private final IdHandler<K, Object> idHandler;
    private final JacksonDecoderFactory<T> decoderFactory;
    private final Map<Feature, Boolean> features;

    /**
     * Cache of referenced collections
     */
    private final Map<JacksonCollectionKey, JacksonDBCollection> referencedCollectionCache = new ConcurrentHashMap<JacksonCollectionKey, JacksonDBCollection>();

    protected JacksonDBCollection(DBCollection dbCollection, JavaType type, JavaType keyType, ObjectMapper objectMapper,
                                  Map<Feature, Boolean> features) {
        this.dbCollection = dbCollection;
        this.type = type;
        this.keyType = keyType;
        this.objectMapper = objectMapper;
        this.decoderFactory = new JacksonDecoderFactory<T>(this, objectMapper, type);
        // We want to find how we should serialize the ID, in case it is passed to us
        try {
            this.idHandler = (IdHandler) IdHandlerFactory.getIdHandlerForProperty(objectMapper, type.getRawClass(), keyType.getRawClass());
        } catch (JsonMappingException e) {
            throw new MongoJsonMappingException("Unable to introspect class", e);
        }
        if (features == null) {
            this.features = new ConcurrentHashMap<Feature, Boolean>();
        } else {
            this.features = features;
        }
    }

    /**
     * Wraps a DB collection in a JacksonDBCollection
     *
     * @param dbCollection The DB collection to wrap
     * @param type         The type of objects to deserialise to
     * @return The wrapped collection
     */
    public static <T> JacksonDBCollection<T, Object> wrap(DBCollection dbCollection, Class<T> type) {
        return new JacksonDBCollection<T, Object>(dbCollection, DEFAULT_OBJECT_MAPPER.constructType(type),
                DEFAULT_OBJECT_MAPPER.constructType(Object.class), DEFAULT_OBJECT_MAPPER, null);
    }

    /**
     * Wraps a DB collection in a JacksonDBCollection
     *
     * @param dbCollection The DB collection to wrap
     * @param type         The type of objects to deserialise to
     * @param keyType      The type of the objects key
     * @return The wrapped collection
     */
    public static <T, K> JacksonDBCollection<T, K> wrap(DBCollection dbCollection, Class<T> type, Class<K> keyType) {
        return new JacksonDBCollection<T, K>(dbCollection, DEFAULT_OBJECT_MAPPER.constructType(type),
                DEFAULT_OBJECT_MAPPER.constructType(keyType), DEFAULT_OBJECT_MAPPER, null);
    }

    /**
     * Wraps a DB collection in a JacksonDBCollection
     *
     * @param dbCollection The DB collection to wrap
     * @param type         The type of objects to deserialise to
     * @param keyType      The type of the objects key
     * @param view         The JSON view to use for serialisation
     * @return The wrapped collection
     */
    public static <T, K> JacksonDBCollection<T, K> wrap(DBCollection dbCollection, Class<T> type, Class<K> keyType, Class<?> view) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.withModule(MongoJacksonMapperModule.INSTANCE);
        objectMapper.setSerializationConfig(objectMapper.getSerializationConfig().withView(view));
        objectMapper.setHandlerInstantiator(new MongoJacksonHandlerInstantiator(
                (MongoAnnotationIntrospector) objectMapper.getDeserializationConfig().getAnnotationIntrospector()));
        return new JacksonDBCollection<T, K>(dbCollection, DEFAULT_OBJECT_MAPPER.constructType(type),
                DEFAULT_OBJECT_MAPPER.constructType(keyType), objectMapper, null);
    }

    /**
     * Wraps a DB collection in a JacksonDBCollection, using the given object mapper
     *
     * @param dbCollection The DB collection to wrap
     * @param type         The type of objects to deserialise to
     * @param objectMapper The ObjectMapper
     * @return The wrapped collection
     */
    public static <T, K> JacksonDBCollection<T, K> wrap(DBCollection dbCollection, Class<T> type, Class<K> keyType, ObjectMapper objectMapper) {
        return new JacksonDBCollection<T, K>(dbCollection, objectMapper.constructType(type),
                objectMapper.constructType(keyType), objectMapper, null);
    }

    /**
     * Enable the given feature
     *
     * @param feature The feature to enable
     * @return this object
     */
    public JacksonDBCollection<T, K> enable(Feature feature) {
        features.put(feature, true);
        return this;
    }

    /**
     * Disable the given feature
     *
     * @param feature The feature to disable
     * @return this object
     */
    public JacksonDBCollection<T, K> disable(Feature feature) {
        features.put(feature, false);
        return this;
    }

    /**
     * Whether the given feature is enabled
     *
     * @param feature The feature to check
     * @return whether it is enabled
     */
    public boolean isEnabled(Feature feature) {
        Boolean enabled = features.get(feature);
        if (enabled == null) {
            return feature.isEnabledByDefault();
        } else {
            return enabled;
        }
    }


    /**
     * Get the underlying db collection
     *
     * @return The underlying db collection
     */
    public DBCollection getDbCollection() {
        return dbCollection;
    }

    /**
     * Inserts an object into the database.
     * if the objects _id is null, one will be generated
     * you can get the _id that was generated by calling getSavedObject() or getSavedId() on the result
     *
     * @param object The object to insert
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> insert(T object) throws MongoException {
        DBObject dbObject = convertToDbObject(object);
        return new WriteResult<T, K>(this, dbCollection.insert(dbObject), dbObject);
    }

    /**
     * Inserts an object into the database.
     * if the objects _id is null, one will be generated
     * you can get the _id that was generated by calling getSavedObject() or getSavedId() on the result
     *
     * @param object  The object to insert
     * @param concern the write concern
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> insert(T object, WriteConcern concern) throws MongoException {
        DBObject dbObject = convertToDbObject(object);
        return new WriteResult<T, K>(this, dbCollection.insert(dbObject, concern), dbObject);
    }

    /**
     * Inserts objects into the database.
     * if the objects _id is null, one will be generated
     * you can get the _id that were generated by calling getSavedObjects() or getSavedIds() on the result
     *
     * @param objects The objects to insert
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> insert(T... objects) throws MongoException {
        DBObject[] dbObjects = convertToDbObjects(objects);
        return new WriteResult<T, K>(this, dbCollection.insert(dbObjects), dbObjects);
    }


    /**
     * Inserts objects into the database.
     * if the objects _id is null, one will be generated
     * you can get the _id that were generated by calling getSavedObjects() or getSavedIds() on the result
     *
     * @param objects The objects to insert
     * @param concern the write concern
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> insert(WriteConcern concern, T... objects) throws MongoException {
        DBObject[] dbObjects = convertToDbObjects(objects);
        return new WriteResult<T, K>(this, dbCollection.insert(concern, dbObjects), dbObjects);
    }

    /**
     * Inserts objects into the database.
     * if the objects _id is null, one will be generated
     * you can get the _id that were generated by calling getSavedObjects() or getSavedIds() on the result
     *
     * @param list The objects to insert
     * @return The result
     * @throws MongoException If an error occurred
     */
    @SuppressWarnings({"unchecked"})
    public WriteResult<T, K> insert(List<T> list) throws MongoException {
        return insert(list.toArray((T[]) new Object[list.size()]));
    }

    /**
     * Inserts objects into the database.
     * if the objects _id is null, one will be generated
     * you can get the _id that were generated by calling getSavedObjects() or getSavedIds() on the result
     *
     * @param list    The objects to insert
     * @param concern the write concern
     * @return The result
     * @throws MongoException If an error occurred
     */
    @SuppressWarnings({"unchecked"})
    public WriteResult<T, K> insert(List<T> list, WriteConcern concern) throws MongoException {
        return insert(concern, list.toArray((T[]) new Object[list.size()]));
    }

    /**
     * Performs an update operation.
     *
     * @param query   search query for old object to update
     * @param object  object with which to update <tt>query</tt>
     * @param upsert  if the database should create the element if it does not exist
     * @param multi   if the update should be applied to all objects matching (db version 1.1.3 and above). An object will
     *                not be inserted if it does not exist in the collection and upsert=true and multi=true.
     *                See <a href="http://www.mongodb.org/display/DOCS/Atomic+Operations">http://www.mongodb.org/display/DOCS/Atomic+Operations</a>
     * @param concern the write concern
     * @return The write result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> update(DBObject query, DBObject object, boolean upsert, boolean multi, WriteConcern concern) throws MongoException {
        return new WriteResult<T, K>(this, dbCollection.update(serializeFields(query), object, upsert, multi, concern));
    }

    /**
     * Performs an update operation.
     *
     * @param query   search query for old object to update
     * @param update  update with which to update <tt>query</tt>
     * @param upsert  if the database should create the element if it does not exist
     * @param multi   if the update should be applied to all objects matching (db version 1.1.3 and above). An object will
     *                not be inserted if it does not exist in the collection and upsert=true and multi=true.
     *                See <a href="http://www.mongodb.org/display/DOCS/Atomic+Operations">http://www.mongodb.org/display/DOCS/Atomic+Operations</a>
     * @param concern the write concern
     * @return The write result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> update(DBObject query, DBUpdate.Builder update, boolean upsert, boolean multi, WriteConcern concern) throws MongoException {
        return this.update(query, update.serialiseAndGet(objectMapper), upsert, multi, concern);
    }

    /**
     * Performs an update operation.
     *
     * @param query   search query for old object to update
     * @param object  object with which to update <tt>query</tt>
     * @param upsert  if the database should create the element if it does not exist
     * @param multi   if the update should be applied to all objects matching (db version 1.1.3 and above). An object will
     *                not be inserted if it does not exist in the collection and upsert=true and multi=true.
     *                See <a href="http://www.mongodb.org/display/DOCS/Atomic+Operations">http://www.mongodb.org/display/DOCS/Atomic+Operations</a>
     * @param concern the write concern
     * @return The write result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> update(T query, T object, boolean upsert, boolean multi, WriteConcern concern) throws MongoException {
        return update(convertToDbObject(query), convertToDbObject(object), upsert, multi, concern);
    }

    /**
     * Performs an update operation.
     *
     * @param query   search query for old object to update
     * @param object  object with which to update <tt>q</tt>
     * @param upsert  if the database should create the element if it does not exist
     * @param multi   if the update should be applied to all objects matching (db version 1.1.3 and above). An object will
     *                not be inserted if it does not exist in the collection and upsert=true and multi=true.
     *                See <a href="http://www.mongodb.org/display/DOCS/Atomic+Operations">http://www.mongodb.org/display/DOCS/Atomic+Operations</a>
     * @param concern the write concern
     * @param encoder the DBEncoder to use
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> update(DBObject query, DBObject object, boolean upsert, boolean multi, WriteConcern concern, DBEncoder encoder) throws MongoException {
        return new WriteResult<T, K>(this, dbCollection.update(serializeFields(query), object, upsert, multi, concern, encoder));
    }

    /**
     * Performs an update operation.
     *
     * @param query   search query for old object to update
     * @param object  object with which to update <tt>q</tt>
     * @param upsert  if the database should create the element if it does not exist
     * @param multi   if the update should be applied to all objects matching (db version 1.1.3 and above). An object will
     *                not be inserted if it does not exist in the collection and upsert=true and multi=true.
     *                See <a href="http://www.mongodb.org/display/DOCS/Atomic+Operations">http://www.mongodb.org/display/DOCS/Atomic+Operations</a>
     * @param concern the write concern
     * @param encoder the DBEncoder to use
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> update(T query, T object, boolean upsert, boolean multi, WriteConcern concern, DBEncoder encoder) throws MongoException {
        return update(convertToDbObject(query), convertToDbObject(object), upsert, multi, concern, encoder);
    }

    /**
     * calls {@link DBCollection#update(com.mongodb.DBObject, com.mongodb.DBObject, boolean, boolean, com.mongodb.WriteConcern)} with default WriteConcern.
     *
     * @param query  search query for old object to update
     * @param object object with which to update <tt>q</tt>
     * @param upsert if the database should create the element if it does not exist
     * @param multi  if the update should be applied to all objects matching (db version 1.1.3 and above)
     *               See http://www.mongodb.org/display/DOCS/Atomic+Operations
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> update(DBObject query, DBObject object, boolean upsert, boolean multi)
            throws MongoException {
        return update(query, object, upsert, multi, getWriteConcern());
    }

    /**
     * Performs an update operation.
     *
     * @param query  search query for old object to update
     * @param update update with which to update <tt>query</tt>
     * @param upsert if the database should create the element if it does not exist
     * @param multi  if the update should be applied to all objects matching (db version 1.1.3 and above). An object will
     *               not be inserted if it does not exist in the collection and upsert=true and multi=true.
     *               See <a href="http://www.mongodb.org/display/DOCS/Atomic+Operations">http://www.mongodb.org/display/DOCS/Atomic+Operations</a>
     * @return The write result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> update(DBObject query, DBUpdate.Builder update, boolean upsert, boolean multi) throws MongoException {
        return this.update(query, update.serialiseAndGet(objectMapper), upsert, multi);
    }

    /**
     * calls {@link DBCollection#update(com.mongodb.DBObject, com.mongodb.DBObject, boolean, boolean, com.mongodb.WriteConcern)} with default WriteConcern.
     *
     * @param query  search query for old object to update
     * @param object object with which to update <tt>q</tt>
     * @param upsert if the database should create the element if it does not exist
     * @param multi  if the update should be applied to all objects matching (db version 1.1.3 and above)
     *               See http://www.mongodb.org/display/DOCS/Atomic+Operations
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> update(T query, T object, boolean upsert, boolean multi)
            throws MongoException {
        return update(query, object, upsert, multi, getWriteConcern());
    }

    /**
     * calls {@link DBCollection#update(com.mongodb.DBObject, com.mongodb.DBObject, boolean, boolean)} with upsert=false and multi=false
     *
     * @param query  search query for old object to update
     * @param object object with which to update <tt>query</tt>
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> update(DBObject query, DBObject object) throws MongoException {
        return update(query, object, false, false);
    }

    /**
     * Performs an update operation.
     *
     * @param query  search query for old object to update
     * @param update update with which to update <tt>query</tt>
     * @return The write result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> update(DBObject query, DBUpdate.Builder update) throws MongoException {
        return this.update(query, update.serialiseAndGet(objectMapper));
    }


    /**
     * calls {@link DBCollection#update(com.mongodb.DBObject, com.mongodb.DBObject, boolean, boolean)} with upsert=false and multi=false
     *
     * @param query  search query for old object to update
     * @param object object with which to update <tt>query</tt>
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> update(T query, T object) throws MongoException {
        return update(query, object, false, false);
    }

    /**
     * calls {@link DBCollection#update(com.mongodb.DBObject, com.mongodb.DBObject, boolean, boolean)} with upsert=false and multi=false
     *
     * @param id     the id of the object to update
     * @param object object with which to update <tt>query</tt>
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> updateById(K id, T object) throws MongoException {
        return update(createIdQuery(id), convertToDbObject(object), false, false);
    }

    /**
     * Performs an update operation.
     *
     * @param id     The id of the document to update
     * @param update update with which to update <tt>query</tt>
     * @return The write result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> updateById(K id, DBUpdate.Builder update) throws MongoException {
        return this.update(createIdQuery(id), update.serialiseAndGet(objectMapper));
    }

    /**
     * calls {@link DBCollection#update(com.mongodb.DBObject, com.mongodb.DBObject, boolean, boolean)} with upsert=false and multi=true
     *
     * @param query  search query for old object to update
     * @param object object with which to update <tt>query</tt>
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> updateMulti(DBObject query, DBObject object) throws MongoException {
        return update(query, object, false, true);
    }

    /**
     * calls {@link DBCollection#update(com.mongodb.DBObject, com.mongodb.DBObject, boolean, boolean)} with upsert=false and multi=true
     *
     * @param query  search query for old object to update
     * @param update update with which to update <tt>query</tt>
     * @return The write result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> updateMulti(DBObject query, DBUpdate.Builder update) throws MongoException {
        return this.updateMulti(query, update.serialiseAndGet(objectMapper));
    }

    /**
     * calls {@link DBCollection#update(com.mongodb.DBObject, com.mongodb.DBObject, boolean, boolean)} with upsert=false and multi=true
     *
     * @param query  search query for old object to update
     * @param object object with which to update <tt>query</tt>
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> updateMulti(T query, T object) throws MongoException {
        return update(query, object, false, true);
    }


    /**
     * Removes objects from the database collection.
     *
     * @param query   the object that documents to be removed must match
     * @param concern WriteConcern for this operation
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> remove(DBObject query, WriteConcern concern) throws MongoException {
        return new WriteResult<T, K>(this, dbCollection.remove(serializeFields(query), concern));
    }

    /**
     * Removes objects from the database collection.
     *
     * @param object  the object that documents to be removed must match
     * @param concern WriteConcern for this operation
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> remove(T object, WriteConcern concern) throws MongoException {
        return new WriteResult<T, K>(this, dbCollection.remove(convertToDbObject(object), concern));
    }

    /**
     * Removes objects from the database collection.
     *
     * @param query   the object that documents to be removed must match
     * @param concern WriteConcern for this operation
     * @param encoder the DBEncoder to use
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> remove(DBObject query, WriteConcern concern, DBEncoder encoder) throws MongoException {
        return new WriteResult<T, K>(this, dbCollection.remove(serializeFields(query), concern, encoder));
    }

    /**
     * calls {@link DBCollection#remove(com.mongodb.DBObject, com.mongodb.WriteConcern)} with the default WriteConcern
     *
     * @param query the query that documents to be removed must match
     * @return The write result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> remove(DBObject query) throws MongoException {
        return new WriteResult<T, K>(this, dbCollection.remove(serializeFields(query)));
    }

    /**
     * calls {@link DBCollection#remove(com.mongodb.DBObject, com.mongodb.WriteConcern)} with the default WriteConcern
     *
     * @param object the object that documents to be removed must match
     * @return The write result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> remove(T object) throws MongoException {
        return new WriteResult<T, K>(this, dbCollection.remove(convertToDbObject(object)));
    }

    /**
     * calls {@link DBCollection#remove(com.mongodb.DBObject, com.mongodb.WriteConcern)} with the default WriteConcern
     *
     * @param id the id of the document to remove
     * @return The write result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> removeById(K id) throws MongoException {
        return new WriteResult<T, K>(this, dbCollection.remove(createIdQuery(id)));
    }

    /**
     * Finds the first document in the query and updates it.
     *
     * @param query     query to match
     * @param fields    fields to be returned
     * @param sort      sort to apply before picking first document
     * @param remove    if true, document found will be removed
     * @param update    update to apply
     * @param returnNew if true, the updated document is returned, otherwise the old document is returned (or it would be lost forever)
     * @param upsert    do upsert (insert if document not present)
     * @return the object
     */
    public T findAndModify(DBObject query, DBObject fields, DBObject sort, boolean remove, DBObject update, boolean returnNew, boolean upsert) {
        return convertFromDbObject(dbCollection.findAndModify(serializeFields(query), fields, sort, remove, update, returnNew, upsert));
    }


    /**
     * calls {@link DBCollection#findAndModify(com.mongodb.DBObject, com.mongodb.DBObject, com.mongodb.DBObject, boolean, com.mongodb.DBObject, boolean, boolean)}
     * with fields=null, remove=false, returnNew=false, upsert=false
     *
     * @param query  The query
     * @param sort   The sort
     * @param update The update to apply
     * @return the old object
     */
    public T findAndModify(DBObject query, DBObject sort, DBObject update) {
        return findAndModify(query, null, sort, false, update, false, false);
    }

    /**
     * calls {@link DBCollection#findAndModify(com.mongodb.DBObject, com.mongodb.DBObject, com.mongodb.DBObject, boolean, com.mongodb.DBObject, boolean, boolean)}
     * with fields=null, sort=null, remove=false, returnNew=false, upsert=false
     *
     * @param query  The query
     * @param update The update to apply
     * @return the old object
     */
    public T findAndModify(DBObject query, DBObject update) {
        return findAndModify(query, null, null, false, update, false, false);
    }

    /**
     * calls {@link DBCollection#findAndModify(com.mongodb.DBObject, com.mongodb.DBObject, com.mongodb.DBObject, boolean, com.mongodb.DBObject, boolean, boolean)}
     * with fields=null, sort=null, remove=true, returnNew=false, upsert=false
     *
     * @param query The query
     * @return the removed object
     */
    public T findAndRemove(DBObject query) {
        return findAndModify(query, null, null, true, null, false, false);
    }

    /**
     * calls {@link DBCollection#createIndex(com.mongodb.DBObject, com.mongodb.DBObject)} with default index options
     *
     * @param keys an object with a key set of the fields desired for the index
     * @throws MongoException If an error occurred
     */
    public final void createIndex(final DBObject keys) throws MongoException {
        dbCollection.createIndex(keys);
    }

    /**
     * Forces creation of an index on a set of fields, if one does not already exist.
     *
     * @param keys    The keys to index
     * @param options The options
     * @throws MongoException If an error occurred
     */
    public void createIndex(DBObject keys, DBObject options) throws MongoException {
        dbCollection.createIndex(keys, options);
    }

    /**
     * Forces creation of an index on a set of fields, if one does not already exist.
     *
     * @param keys    The keys to index
     * @param options The index options
     * @param encoder the DBEncoder to use
     * @throws MongoException If an error occurred
     */
    public void createIndex(DBObject keys, DBObject options, DBEncoder encoder) throws MongoException {
        dbCollection.createIndex(keys, options, encoder);
    }

    /**
     * Creates an ascending index on a field with default options, if one does not already exist.
     *
     * @param name name of field to index on
     */
    public final void ensureIndex(final String name) {
        ensureIndex(new BasicDBObject(name, 1));
    }

    /**
     * calls {@link DBCollection#ensureIndex(com.mongodb.DBObject, com.mongodb.DBObject)} with default options
     *
     * @param keys an object with a key set of the fields desired for the index
     * @throws MongoException If an error occurred
     */
    public final void ensureIndex(final DBObject keys) throws MongoException {
        dbCollection.ensureIndex(keys);
    }

    /**
     * calls {@link DBCollection#ensureIndex(com.mongodb.DBObject, java.lang.String, boolean)} with unique=false
     *
     * @param keys fields to use for index
     * @param name an identifier for the index
     * @throws MongoException If an error occurred
     */
    public void ensureIndex(DBObject keys, String name) throws MongoException {
        ensureIndex(keys, name, false);
    }

    /**
     * Ensures an index on this collection (that is, the index will be created if it does not exist).
     *
     * @param keys   fields to use for index
     * @param name   an identifier for the index. If null or empty, the default name will be used.
     * @param unique if the index should be unique
     * @throws MongoException If an error occurred
     */
    public void ensureIndex(DBObject keys, String name, boolean unique) throws MongoException {
        dbCollection.ensureIndex(keys, name, unique);
    }

    /**
     * Creates an index on a set of fields, if one does not already exist.
     *
     * @param keys      an object with a key set of the fields desired for the index
     * @param optionsIN options for the index (name, unique, etc)
     * @throws MongoException If an error occurred
     */
    public void ensureIndex(final DBObject keys, final DBObject optionsIN) throws MongoException {
        dbCollection.ensureIndex(keys, optionsIN);
    }

    /**
     * Clears all indices that have not yet been applied to this collection.
     */
    public void resetIndexCache() {
        dbCollection.resetIndexCache();
    }

    /**
     * Set hint fields for this collection (to optimize queries).
     *
     * @param lst a list of <code>DBObject</code>s to be used as hints
     */
    public void setHintFields(List<DBObject> lst) {
        dbCollection.setHintFields(lst);
    }


    /**
     * Queries for an object in this collection.
     *
     * @param query object for which to search
     * @return an iterator over the results
     * @throws MongoException If an error occurred
     */
    public DBCursor<T> find(DBObject query) throws MongoException {
        return new DBCursor<T>(this, dbCollection.find(serializeFields(query)));
    }

    /**
     * Queries for an object in this collection.
     *
     * @param query object for which to search
     * @return an iterator over the results
     * @throws MongoException If an error occurred
     */
    public DBCursor<T> find(T query) throws MongoException {
        return new DBCursor<T>(this, dbCollection.find(convertToDbObject(query)));
    }

    /**
     * Queries for an object in this collection.
     * <p/>
     * <p>
     * An empty DBObject will match every document in the collection.
     * Regardless of fields specified, the _id fields are always returned.
     * </p>
     * <p>
     * An example that returns the "x" and "_id" fields for every document
     * in the collection that has an "x" field:
     * </p>
     * <blockquote><pre>
     * BasicDBObject keys = new BasicDBObject();
     * keys.put("x", 1);
     * <p/>
     * DBCursor cursor = collection.find(new BasicDBObject(), keys);
     * </pre></blockquote>
     *
     * @param query object for which to search
     * @param keys  fields to return
     * @return a cursor to iterate over results
     */
    public final DBCursor<T> find(DBObject query, DBObject keys) {
        return new DBCursor<T>(this, dbCollection.find(serializeFields(query), keys));
    }

    /**
     * Queries for an object in this collection.
     * <p/>
     * <p>
     * An empty DBObject will match every document in the collection.
     * Regardless of fields specified, the _id fields are always returned.
     * </p>
     * To keys object should have non null values for every key that you want to return
     *
     * @param query object for which to search
     * @param keys  fields to return
     * @return a cursor to iterate over results
     */
    public final DBCursor<T> find(T query, T keys) {
        return new DBCursor<T>(this, dbCollection.find(convertToDbObject(query), convertToDbObject(keys)));
    }


    /**
     * Queries for all objects in this collection.
     *
     * @return a cursor which will iterate over every object
     * @throws MongoException If an error occurred
     */
    public final DBCursor<T> find() throws MongoException {
        return new DBCursor<T>(this, dbCollection.find());
    }

    /**
     * Returns a single object from this collection.
     *
     * @return the object found, or <code>null</code> if the collection is empty
     * @throws MongoException If an error occurred
     */
    public T findOne() throws MongoException {
        return findOne(new BasicDBObject());
    }

    /**
     * Find an object by the given id
     *
     * @param id The id
     * @return The object
     * @throws MongoException If an error occurred
     */
    public T findOneById(K id) throws MongoException {
        return findOneById(id, (DBObject) null);
    }

    /**
     * Find an object by the given id
     *
     * @param id The id
     * @return The object
     * @throws MongoException If an error occurred
     */
    public T findOneById(K id, DBObject fields) throws MongoException {
        return findOne(createIdQuery(id), fields);
    }

    /**
     * Find an object by the given id
     *
     * @param id The id
     * @return The object
     * @throws MongoException If an error occurred
     */
    public T findOneById(K id, T fields) throws MongoException {
        return findOneById(id, convertToDbObject(fields));
    }

    /**
     * Returns a single object from this collection matching the query.
     *
     * @param query the query object
     * @return the object found, or <code>null</code> if no such object exists
     * @throws MongoException If an error occurred
     */
    public T findOne(DBObject query) throws MongoException {
        return findOne(query, null);
    }

    /**
     * Returns a single object from this collection matching the query.
     *
     * @param query the query object
     * @return the object found, or <code>null</code> if no such object exists
     * @throws MongoException If an error occurred
     */
    public T findOne(T query) throws MongoException {
        return findOne(query, null);
    }

    /**
     * Returns a single object from this collection matching the query.
     *
     * @param query  the query object
     * @param fields the fields to return
     * @return the object found, or <code>null</code> if no such object exists
     */
    public T findOne(DBObject query, DBObject fields) {
        return findOne(query, fields, getReadPreference());
    }

    /**
     * Returns a single object from this collection matching the query.
     *
     * @param query  the query object
     * @param fields an object for which every non null field will be returned
     * @return the object found, or <code>null</code> if no such object exists
     */
    public T findOne(T query, T fields) {
        return findOne(query, fields, getReadPreference());
    }

    /**
     * Returns a single object from this collection matching the query.
     *
     * @param query    the query object
     * @param fields   fields to return
     * @param readPref The read preference
     * @return the object found, or <code>null</code> if no such object exists
     */
    public T findOne(DBObject query, DBObject fields, ReadPreference readPref) {
        DBCursor<T> cursor = find(query, fields).setReadPreference(readPref);
        if (cursor.hasNext()) {
            return cursor.next();
        } else {
            return null;
        }
    }

    /**
     * Returns a single object from this collection matching the query.
     *
     * @param query    the query object
     * @param fields   an object for which every non null field will be returned
     * @param readPref The read preferences
     * @return the object found, or <code>null</code> if no such object exists
     */
    public T findOne(T query, T fields, ReadPreference readPref) {
        DBCursor<T> cursor = find(query, fields).setReadPreference(readPref);
        if (cursor.hasNext()) {
            return cursor.next();
        } else {
            return null;
        }
    }

    /**
     * Fetch a collection of dbrefs.  This is more efficient than fetching one at a time.
     *
     * @param collection the collection to fetch
     * @param <R>        The type of the reference
     * @return The collection of referenced objcets
     */
    public <R, RK> List<R> fetch(Collection<DBRef<R, RK>> collection) {
        return fetch(collection, null);
    }

    /**
     * Fetch a collection of dbrefs.  This is more efficient than fetching one at a time.
     *
     * @param collection the collection to fetch
     * @param fields     The fields to retrieve for each of the documents
     * @return The collection of referenced objcets
     */
    public <R, RK> List<R> fetch(Collection<DBRef<R, RK>> collection, DBObject fields) {
        Map<JacksonCollectionKey, List<Object>> collectionsToIds = new HashMap<JacksonCollectionKey, List<Object>>();
        for (DBRef<R, RK> ref : collection) {
            if (ref instanceof FetchableDBRef) {
                JacksonCollectionKey key = ((FetchableDBRef) ref).getCollectionKey();
                List<Object> ids = collectionsToIds.get(key);
                if (ids == null) {
                    ids = new ArrayList<Object>();
                    collectionsToIds.put(key, ids);
                }
                ids.add(getReferenceCollection(key).convertToDbId(ref.getId()));
            }
        }
        List<R> results = new ArrayList<R>();
        for (Map.Entry<JacksonCollectionKey, List<Object>> entry : collectionsToIds.entrySet()) {
            for (R result : this.<R, RK>getReferenceCollection(entry.getKey()).find(
                    new QueryBuilder().put("_id").in(entry.getValue()).get(), fields)) {
                results.add(result);
            }
        }
        return results;
    }

    /**
     * calls {@link DBCollection#save(com.mongodb.DBObject, com.mongodb.WriteConcern)} with default WriteConcern
     *
     * @param object the object to save
     *               will add <code>_id</code> field to jo if needed
     * @return The result
     */
    public final WriteResult<T, K> save(T object) {
        return save(object, getWriteConcern());
    }

    /**
     * Saves an object to this collection (does insert or update based on the object _id).
     *
     * @param object  the <code>DBObject</code> to save
     * @param concern the write concern
     * @return The result
     * @throws MongoException If an error occurred
     */
    public WriteResult<T, K> save(T object, WriteConcern concern) throws MongoException {
        DBObject dbObject = convertToDbObject(object);
        return new WriteResult<T, K>(this, dbCollection.save(dbObject, concern), dbObject);
    }

    /**
     * Drops all indices from this collection
     *
     * @throws MongoException If an error occurred
     */
    public void dropIndexes() throws MongoException {
        dropIndexes("*");
    }


    /**
     * Drops an index from this collection
     *
     * @param name the index name
     * @throws MongoException If an error occurred
     */
    public void dropIndexes(String name) throws MongoException {
        dbCollection.dropIndexes(name);
    }

    /**
     * Drops (deletes) this collection. Use with care.
     *
     * @throws MongoException If an error occurred
     */
    public void drop() throws MongoException {
        dbCollection.drop();
    }

    /**
     * returns the number of documents in this collection.
     *
     * @return The count
     * @throws MongoException If an error occurred
     */
    public long count()
            throws MongoException {
        return getCount(new BasicDBObject(), null);
    }

    /**
     * returns the number of documents that match a query.
     *
     * @param query query to match
     * @return The count
     * @throws MongoException If an error occurred
     */
    public long count(DBObject query) throws MongoException {
        return getCount(query, null);
    }


    /**
     * calls {@link DBCollection#getCount(com.mongodb.DBObject, com.mongodb.DBObject)} with an empty query and null fields.
     *
     * @return number of documents that match query
     * @throws MongoException If an error occurred
     */
    public long getCount() throws MongoException {
        return getCount(new BasicDBObject(), null);
    }

    /**
     * calls {@link DBCollection#getCount(com.mongodb.DBObject, com.mongodb.DBObject)} with null fields.
     *
     * @param query query to match
     * @return The count
     * @throws MongoException If an error occurred
     */
    public long getCount(DBObject query) throws MongoException {
        return getCount(query, null);
    }

    /**
     * calls {@link DBCollection#getCount(com.mongodb.DBObject, com.mongodb.DBObject)} with null fields.
     *
     * @param query query to match
     * @return The count
     * @throws MongoException If an error occurred
     */
    public long getCount(T query) throws MongoException {
        return getCount(query, null);
    }

    /**
     * calls {@link DBCollection#getCount(com.mongodb.DBObject, com.mongodb.DBObject, long, long)} with limit=0 and skip=0
     *
     * @param query  query to match
     * @param fields fields to return
     * @return The count
     * @throws MongoException If an error occurred
     */
    public long getCount(DBObject query, DBObject fields) throws MongoException {
        return getCount(query, fields, 0, 0);
    }

    /**
     * calls {@link DBCollection#getCount(com.mongodb.DBObject, com.mongodb.DBObject, long, long)} with limit=0 and skip=0
     *
     * @param query  query to match
     * @param fields fields to return
     * @return The count
     * @throws MongoException If an error occurred
     */
    public long getCount(T query, T fields) throws MongoException {
        return getCount(query, fields, 0, 0);
    }

    /**
     * Returns the number of documents in the collection
     * that match the specified query
     *
     * @param query  query to select documents to count
     * @param fields fields to return
     * @param limit  limit the count to this value
     * @param skip   number of entries to skip
     * @return number of documents that match query and fields
     * @throws MongoException If an error occurred
     */
    public long getCount(DBObject query, DBObject fields, long limit, long skip) throws MongoException {
        return dbCollection.getCount(serializeFields(query), fields, limit, skip);
    }

    /**
     * Returns the number of documents in the collection
     * that match the specified query
     *
     * @param query  query to select documents to count
     * @param fields fields to return
     * @param limit  limit the count to this value
     * @param skip   number of entries to skip
     * @return number of documents that match query and fields
     * @throws MongoException If an error occurred
     */
    public long getCount(T query, T fields, long limit, long skip) throws MongoException {
        return dbCollection.getCount(convertToDbObject(query), convertToDbObject(fields), limit, skip);
    }

    /**
     * Calls {@link DBCollection#rename(java.lang.String, boolean)} with dropTarget=false
     *
     * @param newName new collection name (not a full namespace)
     * @return the new collection
     * @throws MongoException If an error occurred
     */
    public JacksonDBCollection<T, K> rename(String newName) throws MongoException {
        return rename(newName, false);
    }

    /**
     * renames of this collection to newName
     *
     * @param newName    new collection name (not a full namespace)
     * @param dropTarget if a collection with the new name exists, whether or not to drop it
     * @return the new collection
     * @throws MongoException If an error occurred
     */
    public JacksonDBCollection<T, K> rename(String newName, boolean dropTarget) throws MongoException {
        return new JacksonDBCollection<T, K>(dbCollection.rename(newName, dropTarget), type, keyType, objectMapper, features);
    }

    /**
     * calls {@link DBCollection#group(com.mongodb.DBObject, com.mongodb.DBObject, com.mongodb.DBObject, java.lang.String, java.lang.String)} with finalize=null
     *
     * @param key     - { a : true }
     * @param cond    - optional condition on query
     * @param reduce  javascript reduce function
     * @param initial initial value for first match on a key
     * @return The results
     * @throws MongoException If an error occurred
     * @see <a href="http://www.mongodb.org/display/DOCS/Aggregation">http://www.mongodb.org/display/DOCS/Aggregation</a>
     */
    public DBObject group(DBObject key, DBObject cond, DBObject initial, String reduce) throws MongoException {
        return group(key, cond, initial, reduce, null);
    }

    /**
     * Applies a group operation
     *
     * @param key      - { a : true }
     * @param cond     - optional condition on query
     * @param reduce   javascript reduce function
     * @param initial  initial value for first match on a key
     * @param finalize An optional function that can operate on the result(s) of the reduce function.
     * @return The results
     * @throws MongoException If an error occurred
     * @see <a href="http://www.mongodb.org/display/DOCS/Aggregation">http://www.mongodb.org/display/DOCS/Aggregation</a>
     */
    public DBObject group(DBObject key, DBObject cond, DBObject initial, String reduce, String finalize)
            throws MongoException {
        GroupCommand cmd = new GroupCommand(dbCollection, key, cond, initial, reduce, finalize);
        return group(cmd);
    }

    /**
     * Applies a group operation
     *
     * @param cmd the group command
     * @return The results
     * @throws MongoException
     * @see <a href="http://www.mongodb.org/display/DOCS/Aggregation">http://www.mongodb.org/display/DOCS/Aggregation</a>
     */
    public DBObject group(GroupCommand cmd) {
        return dbCollection.group(cmd);
    }

    /**
     * find distinct values for a key
     *
     * @param key The key
     * @return The results
     */
    public List distinct(String key) {
        return distinct(key, new BasicDBObject());
    }

    /**
     * find distinct values for a key
     *
     * @param key   The key
     * @param query query to match
     * @return The results
     */
    public List distinct(String key, DBObject query) {
        return dbCollection.distinct(key, serializeFields(query));
    }

    /**
     * performs a map reduce operation
     * Runs the command in REPLACE output mode (saves to named collection)
     *
     * @param map          map function in javascript code
     * @param outputTarget optional - leave null if want to use temp collection
     * @param reduce       reduce function in javascript code
     * @param query        to match
     * @return The output
     * @throws MongoException If an error occurred
     */
    @Deprecated
    public com.mongodb.MapReduceOutput mapReduce(String map, String reduce, String outputTarget, DBObject query) throws MongoException {
        return mapReduce(new MapReduceCommand(dbCollection, map, reduce, outputTarget, MapReduceCommand.OutputType.REPLACE, serializeFields(query)));
    }

    /**
     * performs a map reduce operation
     * Specify an outputType to control job execution
     * * INLINE - Return results inline
     * * REPLACE - Replace the output collection with the job output
     * * MERGE - Merge the job output with the existing contents of outputTarget
     * * REDUCE - Reduce the job output with the existing contents of
     * outputTarget
     *
     * @param map          map function in javascript code
     * @param outputTarget optional - leave null if want to use temp collection
     * @param outputType   set the type of job output
     * @param reduce       reduce function in javascript code
     * @param query        to match
     * @return The output
     * @throws MongoException If an error occurred
     */
    @Deprecated
    public com.mongodb.MapReduceOutput mapReduce(String map, String reduce, String outputTarget, MapReduceCommand.OutputType outputType, DBObject query)
            throws MongoException {
        return mapReduce(new MapReduceCommand(dbCollection, map, reduce, outputTarget, outputType, serializeFields(query)));
    }

    /**
     * performs a map reduce operation
     *
     * @param command object representing the parameters
     * @return The results
     * @throws MongoException If an error occurred
     */
    @Deprecated
    public com.mongodb.MapReduceOutput mapReduce(MapReduceCommand command) throws MongoException {
        return dbCollection.mapReduce(command);
    }

    /**
     * performs a map reduce operation
     *
     * @param command object representing the parameters
     * @return The output
     * @throws MongoException If an error occurred
     */
    @Deprecated
    public com.mongodb.MapReduceOutput mapReduce(DBObject command) throws MongoException {
        return dbCollection.mapReduce(command);
    }

    /**
     * Performs a map reduce operation
     *
     * @param command The command to execute
     * @return The output
     * @throws MongoException If an error occurred
     */
    public <S, L> MapReduceOutput<S, L> mapReduce(MapReduce.MapReduceCommand<S, L> command) throws MongoException {
        return new MapReduceOutput<S, L>(this, dbCollection.mapReduce(command.build(this)), command.getResultType(),
                command.getKeyType());
    }

    /**
     * Return a list of the indexes for this collection.  Each object
     * in the list is the "info document" from MongoDB
     *
     * @return list of index documents
     */
    public List<DBObject> getIndexInfo() {
        return dbCollection.getIndexInfo();
    }

    /**
     * Drops an index from this collection
     *
     * @param keys keys of the index
     * @throws MongoException If an error occurred
     */
    public void dropIndex(DBObject keys) throws MongoException {
        dbCollection.dropIndex(keys);
    }

    /**
     * Drops an index from this collection
     *
     * @param name name of index to drop
     * @throws MongoException If an error occurred
     */
    public void dropIndex(String name) throws MongoException {
        dbCollection.dropIndex(name);
    }

    /**
     * gets the collections statistics ("collstats" command)
     *
     * @return the stats
     */
    public CommandResult getStats() {
        return dbCollection.getStats();
    }

    /**
     * returns whether or not this is a capped collection
     *
     * @return whether it is capped
     */
    public boolean isCapped() {
        return dbCollection.isCapped();
    }

    /**
     * Finds a collection that is prefixed with this collection's name.
     * A typical use of this might be
     * <blockquote><pre>
     *    DBCollection users = mongo.getCollection( "wiki" ).getCollection( "users" );
     * </pre></blockquote>
     * Which is equivalent to
     * <pre><blockquote>
     *   DBCollection users = mongo.getCollection( "wiki.users" );
     * </pre></blockquote>
     *
     * @param n    the name of the collection to find
     * @param type The type of the collection
     * @return the matching collection
     */
    public <S, L> JacksonDBCollection<S, L> getCollection(String n, Class<S> type, Class<L> keyType) {
        return wrap(getDB().getCollection(getName() + "." + n), type, keyType, objectMapper);
    }

    /**
     * Returns the name of this collection.
     *
     * @return the name of this collection
     */
    public String getName() {
        return dbCollection.getName();
    }

    /**
     * Returns the full name of this collection, with the database name as a prefix.
     *
     * @return the name of this collection
     */
    public String getFullName() {
        return dbCollection.getFullName();
    }

    /**
     * Returns the database this collection is a member of.
     *
     * @return this collection's database
     */
    public DB getDB() {
        return dbCollection.getDB();
    }

    @Override
    public int hashCode() {
        return dbCollection.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public String toString() {
        return dbCollection.toString();
    }

    /**
     * Set the write concern for this collection. Will be used for
     * writes to this collection. Overrides any setting of write
     * concern at the DB level. See the documentation for
     * {@link WriteConcern} for more information.
     *
     * @param concern write concern to use
     */
    public void setWriteConcern(WriteConcern concern) {
        dbCollection.setWriteConcern(concern);
    }

    /**
     * Get the write concern for this collection.
     *
     * @return THe write concern
     */
    public WriteConcern getWriteConcern() {
        return dbCollection.getWriteConcern();
    }

    /**
     * Sets the read preference for this collection. Will be used as default
     * for reads from this collection; overrides DB & Connection level settings.
     * See the * documentation for {@link ReadPreference} for more information.
     *
     * @param preference Read Preference to use
     */
    public void setReadPreference(ReadPreference preference) {
        dbCollection.setReadPreference(preference);
    }

    /**
     * Gets the read preference
     *
     * @return The read preference
     */
    public ReadPreference getReadPreference() {
        return dbCollection.getReadPreference();
    }

    /**
     * adds a default query option
     *
     * @param option The option to add
     */
    public void addOption(int option) {
        dbCollection.addOption(option);
    }

    /**
     * sets the default query options
     *
     * @param options The options
     */
    public void setOptions(int options) {
        dbCollection.setOptions(options);
    }

    /**
     * resets the default query options
     */
    public void resetOptions() {
        dbCollection.resetOptions();
    }

    /**
     * gets the default query options
     *
     * @return The options
     */
    public int getOptions() {
        return dbCollection.getOptions();
    }

    /**
     * Get the type of this collection
     *
     * @return The type
     */
    public JacksonCollectionKey getCollectionKey() {
        return new JacksonCollectionKey(getName(), type, keyType);
    }

    /**
     * Get a collection for loading a reference of the given type
     *
     * @param collectionName The name of the collection
     * @param type           The type of the object
     * @param keyType        the type of the id
     * @return The collection
     */
    public <T, K> JacksonDBCollection<T, K> getReferenceCollection(String collectionName, JavaType type, JavaType keyType) {
        return getReferenceCollection(new JacksonCollectionKey(collectionName, type, keyType));
    }

    /**
     * Get a collection for loading a reference of the given type
     *
     * @param collectionKey The key for the collection
     * @return The collection
     */
    public <T, K> JacksonDBCollection<T, K> getReferenceCollection(JacksonCollectionKey collectionKey) {
        JacksonDBCollection<T, K> collection = referencedCollectionCache.get(collectionKey);
        if (collection == null) {
            collection = new JacksonDBCollection<T, K>(getDB().getCollection(collectionKey.getName()),
                    collectionKey.getType(), collectionKey.getKeyType(), objectMapper, features);
            referencedCollectionCache.put(collectionKey, collection);
        }
        return collection;
    }

    JacksonDecoderFactory<T> getDecoderFactory() {
        return decoderFactory;
    }

    DBObject createIdQuery(K object) {
        return new BasicDBObject("_id", convertToDbId(object));
    }

    Object convertToDbId(K object) {
        if (object instanceof org.bson.types.ObjectId) {
            // Do not try and convert it
            return object;
        } else {
            return idHandler.toDbId(object);
        }
    }

    public K convertFromDbId(Object object) {
        return idHandler.fromDbId(object);
    }

    DBObject convertToDbObject(T object) throws MongoException {
        if (object == null) {
            return null;
        }
        BsonObjectGenerator generator = new BsonObjectGenerator();
        try {
            objectMapper.writeValue(generator, object);
        } catch (JsonMappingException e) {
            throw new MongoJsonMappingException(e);
        } catch (IOException e) {
            // This shouldn't happen
            throw new MongoException("Unknown error occurred converting BSON to object", e);
        }
        return generator.getDBObject();
    }

    DBObject[] convertToDbObjects(T... objects) throws MongoException {
        // Yay for generic array creation
        DBObject[] results = new DBObject[objects.length];
        for (int i = 0; i < objects.length; i++) {
            results[i] = convertToDbObject(objects[i]);
        }
        return results;
    }

    T convertFromDbObject(DBObject dbObject) throws MongoException {
        if (dbObject == null) {
            return null;
        }
        if (dbObject instanceof JacksonDBObject) {
            return (T) ((JacksonDBObject) dbObject).getObject();
        }
        try {
            return (T) objectMapper.readValue(new BsonObjectTraversingParser(this, dbObject), type);
        } catch (JsonMappingException e) {
            throw new MongoJsonMappingException(e);
        } catch (IOException e) {
            // This shouldn't happen
            throw new MongoException("Unknown error occurred converting BSON to object", e);
        }
    }

    <S> S convertFromDbObject(DBObject dbObject, Class<S> clazz) throws MongoException {
        if (dbObject == null) {
            return null;
        }
        if (dbObject instanceof JacksonDBObject) {
            return (S) ((JacksonDBObject) dbObject).getObject();
        }
        try {
            return objectMapper.readValue(new BsonObjectTraversingParser(this, dbObject), clazz);
        } catch (JsonMappingException e) {
            throw new MongoJsonMappingException(e);
        } catch (IOException e) {
            // This shouldn't happen
            throw new MongoException("Unknown error occurred converting BSON to object", e);
        }
    }

    @SuppressWarnings({"unchecked"})
    T[] convertFromDbObjects(DBObject... dbObjects) throws MongoException {
        T[] results = (T[]) new Object[dbObjects.length];
        for (int i = 0; i < dbObjects.length; i++) {
            results[i] = convertFromDbObject(dbObjects[i]);
        }
        return results;
    }

    DBObject serializeFields(DBObject value) {
        return SerializationUtils.serializeFields(objectMapper, value);
    }

    Object serializeField(Object value) {
        return SerializationUtils.serializeField(objectMapper, value);
    }

    ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
