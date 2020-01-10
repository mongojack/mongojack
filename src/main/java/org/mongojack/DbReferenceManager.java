package org.mongojack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;
import org.mongojack.internal.JacksonCollectionKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DbReferenceManager {

    private final Map<JacksonCollectionKey<?>, JacksonMongoCollection<?>> referencedCollectionCache = new ConcurrentHashMap<>();

    private final MongoClient mongoClient;
    private final ObjectMapper objectMapper;
    private final String defaultDatabaseName;

    public DbReferenceManager(
        final MongoClient mongoClient,
        final ObjectMapper objectMapper,
        final String defaultDatabaseName
    ) {
        this.mongoClient = mongoClient;
        this.objectMapper = objectMapper;
        this.defaultDatabaseName = defaultDatabaseName;
    }

    public DbReferenceManager(
        final MongoClient mongoClient,
        final String defaultDatabaseName
    ) {
        this(mongoClient, null, defaultDatabaseName);
    }

    /**
     * Get a collection for loading a reference of the given type
     *
     * @param databaseName   Name of the DB that holds the collection
     * @param collectionName The name of the collection
     * @param valueClass     The type of the values in the collection
     * @param <CT>           The type of the values in the collection
     * @return The collection
     */
    @SuppressWarnings("unused")
    public <CT> JacksonMongoCollection<CT> getReferenceCollection(
        String databaseName, String collectionName, Class<CT> valueClass
    ) {
        return getReferenceCollection(new JacksonCollectionKey<>(databaseName, collectionName, valueClass));
    }

    /**
     * Get a collection for loading a reference of the given type
     *
     * @param collectionKey The key for the collection
     * @param <CT>          The type of values in the collection
     * @return The collection
     */
    @SuppressWarnings("unchecked")
    public <CT> JacksonMongoCollection<CT> getReferenceCollection(
        JacksonCollectionKey<CT> collectionKey
    ) {
        return (JacksonMongoCollection<CT>) referencedCollectionCache.computeIfAbsent(
            collectionKey,
            (k) -> {
                final String databaseName = Optional.ofNullable(k.getDatabaseName()).orElse(defaultDatabaseName);
                return JacksonMongoCollection.builder()
                    .withObjectMapper(objectMapper)
                    .build(
                        (MongoCollection<CT>) mongoClient.getDatabase(databaseName).getCollection(k.getCollectionName()).withDocumentClass(k.getValueType()),
                        (Class<CT>) k.getValueType()
                    );
            }
        );
    }

    /**
     * Fetches the underlying value for a single DBRef.
     *
     * @param ref  The reference
     * @param <R>  The type the  ref points to.
     * @param <RK> The type of ID the ref points to
     * @return A ref, or null if the underlying value is nto found
     */
    public <R, RK> R fetch(DBRef<R, RK> ref) {
        return fetch(ref, null);
    }

    /**
     * Fetches the underlying value for a single DBRef.
     *
     * @param ref    The reference
     * @param <R>    The type the  ref points to.
     * @param <RK>   The type of ID the ref points to
     * @param fields A Bson representing the projection to be used.
     * @return A ref, or null if the underlying value is nto found
     */
    public <R, RK> R fetch(DBRef<R, RK> ref, Bson fields) {
        final JacksonMongoCollection<R> collection = getReferenceCollection(ref.getCollectionKey());
        return collection.find(collection.createIdQuery(ref.getId())).projection(fields).first();
    }

    /**
     * Fetch a refs of dbrefs. This is more efficient than fetching one at
     * a time.
     *
     * @param refs the refs to fetch
     * @param <R>  The type of the reference
     * @param <RK> The identifier type
     * @return The refs of referenced objcets
     */
    public <R, RK> List<R> fetch(
        Collection<DBRef<R, RK>> refs
    ) {
        return fetch(refs, null);
    }

    /**
     * Fetch a refs of dbrefs. This is more efficient than fetching one at
     * a time.
     *
     * @param refs   the refs to fetch
     * @param fields The fields to retrieve for each of the documents
     * @param <R>    The type of the reference
     * @param <RK>   The identifier type
     * @return The refs of referenced objects
     */
    @SuppressWarnings("unchecked")
    public <R, RK> List<R> fetch(
        Collection<org.mongojack.DBRef<R, RK>> refs,
        Bson fields
    ) {
        final Map<JacksonCollectionKey<?>, List<RK>> groupedIdentifiers = refs.stream()
            .collect(Collectors.groupingBy(org.mongojack.DBRef::getCollectionKey, Collectors.mapping(DBRef::getId, Collectors.toList())));

        return (List<R>) groupedIdentifiers.entrySet().stream()
            .map((entry) -> {
                final JacksonMongoCollection<?> collection = getReferenceCollection(entry.getKey());
                return collection.find(collection.createIdInQuery(entry.getValue())).projection(fields).into(new ArrayList<>());
            })
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    /**
     * Register a collection so that it will be used (as opposed to an internally built one) when retrieving references.
     *
     * @param collection The collection to register
     */
    @SuppressWarnings("unused")
    public void registerCollection(JacksonMongoCollection<?> collection) {
        referencedCollectionCache.put(collection.getCollectionKey(), collection);
    }

}
