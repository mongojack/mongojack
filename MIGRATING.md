# Migration Guide
## From 2.x to 3.x

This guide is under construction and incomplete.  Feel free to give us some pull requests to help out.

Two primary things happened with the 3.0.0 release of Mongojack: the driver dependency to `mongodb-driver-legacy` was replaced by `mongodb-driver-sync`, and the `JacksonDBCollection`
and related classes were removed and the `JacksonMongoCollection` implementation was improved in order to provide complete functionality.

### Mongo changes

Underneath the covers (or maybe on top of the covers, since `JacksonMongoCollection` is based directly on the `MongoCollection` interface), all of your direct interaction with
Mongo will likely change.  So where before you would do:

```java
MongoClient client = new MongoClient("localhost", 27017);
```

Now you will need to do something like:

```java
MongoClient client = MongoClients.create(new ConnectionString("mongodb://localhost:27017"));
```

We refer you to [the official documentation of the MongoDB Java Driver](https://mongodb.github.io/mongo-java-driver/3.12/) for information on how you would interact with
the newer 3.x series of Mongo drivers.  It is important to note that, because `JacksonMongoCollection` _is a_ `MongoCollection`, you can interact with it using all the
APIs, features, and guides listed in the official documentation.  The only difference is that `JacksonMongoCollection` is using a codec that maps your POJOs using Jackson,
so you don't have to read and write things using the Document class.

### Most Prominent Changes

When using the legacy `JacksonDBCollection` support, you would do something like this to get a handle on a collection and insert/update/query an object:

```java
DBCollection collection = client.getDB("databaseName").getCollection("CollectionName");
JacksonDBCollection<PojoClass, IdentifierClass> jacksonCollection = JacksonDBCollection.wrap(collection, PojoClass.class, IdentiferClass.class);
PojoClass pojo = new PojoClass();
pojo.setIntegerProp(3);
jacksonCollection.insert(pojo);
jacksonCollection.updateById(pojo.getId(), DBUpdate.inc("integerProp"));
PojoClass foundPojo = jacksonCollectioncoll.findOneById(pojo.getId());
```

Using `JacksonMongoCollection` isn't much different.  The main difference is that the code looks more like accessing a plain MongoCollection that is configured
to hold your POJO.  This means that many of the method names are different, and that they can accept the base Mongo implementations of things.  Here's the same thing
as above, using the new collection:

```java
JacksonMongoCollection<PojoClass> jacksonCollection = JacksonMongoCollection.builder().build(client, "databaseName", "CollectionName", PojoClass.class);
PojoClass pojo = new PojoClass();
pojo.setIntegerProp(3);
jacksonCollection.insert(pojo);
jacksonCollection.updateById(pojo.getId(), DBUpdate.inc("integerProp"));
PojoClass foundPojo = jacksonCollectioncoll.findOneById(pojo.getId());
```

Note that it's mostly identical, with the exception of constructing the collection itself and the class used.

One thing to note, though, is that all the methods called on JacksonMongoCollection above are methods designed to ease the transition by mimicking the basic
methods on JacksonDBCollection.  It would be preferable in many ways to treat this as a pure MongoCollection:

```java
MongoCollection<PojoClass> jacksonCollection = JacksonMongoCollection.builder().build(client, "databaseName", "CollectionName", PojoClass.class);
PojoClass pojo = new PojoClass();
pojo.setIntegerProp(3);
jacksonCollection.insertOne(pojo);
jacksonCollection.updateOne(Filters.eq(pojo.getId()), DBUpdate.inc("integerProp"));
PojoClass foundPojo = jacksonCollectioncoll.find(Filters.eq(pojo.getId())).first();
```

This makes your code fundamentally compatible with the underlying Mongo classes, and as long as you avoid deprecated methods, you should be able to fee
secure that the methods you rely on won't disappear.

Note that the new class doesn't include a generic key type; this also makes it more interoperable with the underlying Mongo implementations, at the cost
of some type-safety.

A major change you will note is that many methods on the older class accepted and returned custom classes (DBUpdate, DBCursor), the methods in `JacksonMongoCollection` 
accept standard Mongo objects (e.g. Bson), and return standard mongo objects (e.g. FindIterable).  You will find that the functionality of these things are roughly
the same, and Mongo includes builders that can be used to replace all the (now deprecated) builder classes (e.g. Filters, Updates, Aggregates vs DBQuery, DBUpdate, Aggregation).
Again this should improve current and future interoperability with the base Mongo driver.

### Method / Feature Comparison

#### Construction
##### JacksonDBCollection:
`.wrap()`
##### JacksonMongoCollection:
`.builder()....build()`  Note that you can use the builder to provide the underlying collection, a custom ObjectMapper, and view class.  You can
also call the final build() method several ways, depending on the way you want to specify the metadata for the collection.  If you call
one of the methods that doesn't supply a collection or collection name, the builder will use the `@MongoCollection` annotation to get the
collection name.

#### Resolution of DBRefs / fetch()
##### JacksonDBCollection:
`org.mongojack.DBRef.fetch()` or `JacksonDBCollection.fetch()`
##### JacksonMongoCollection:
Since mongo discourages DBRefs, and this functionality involved pushing a lot of references to the underlying collections around, it's been modified.
You can use `org.mongojack.DbReferenceManager` to fetch DBRefs, grouping them together for efficiency.  This replaces the fetch() methods on
the `JacksonDBCollection` class as well
  
#### Direct conversion of objects
##### JacksonDBCollection:
convertTo/FromDbObject
##### JacksonMongoCollection:
Direct access to this feature was basically removed.  You could in theory get the same thing from `jacksonMongoCollection.getCodecRegistry().get(PojoClass.class).decode()`
 
#### ReadPreference, WriteConcern, etc.
##### JacksonDBCollection:
`setReadPreference/setWriteConcern`
##### JacksonMongoCollection:
`withReadPreference/withWriteConcern`  The collection is treated as immutable, so these methods will return a new collection with the appropriate setting.

#### Features 
##### JacksonDBCollection:
* USE_STREAM_DESERIALIZATION/USE_STREAM_SERIALIZATION
* WRITE_DATES_AS_TIMESTAMPS
##### JacksonMongoCollection:
* USE_STREAM_DESERIALIZATION/USE_STREAM_SERIALIZATION: no longer necessary, because all serialization is done on the fly through Mongo's codec mechanism.
* WRITE_DATES_AS_TIMESTAMPS: this needs to be set / changed in the underlying ObjectMapper.

#### Get underlying collection 
##### JacksonDBCollection:
```java
    public DBCollection getDbCollection() {
```
##### JacksonMongoCollection:
TODO: Write documentation

#### Insert
##### JacksonDBCollection:
```java
    public WriteResult<T, K> insert(T object) throws MongoException {
    public WriteResult<T, K> insert(T object, WriteConcern concern)
    public WriteResult<T, K> insert(T... objects) throws MongoException {
    public WriteResult<T, K> insert(WriteConcern concern, T... objects)
    public WriteResult<T, K> insert(List<T> list) throws MongoException {
    public WriteResult<T, K> insert(List<T> list, WriteConcern concern)
```
##### JacksonMongoCollection:
TODO: Write documentation

#### Update 
##### JacksonDBCollection:
```java
    public WriteResult<T, K> update(DBObject query, DBObject object,
            boolean upsert, boolean multi, WriteConcern concern)
    public WriteResult<T, K> update(DBQuery.Query query,
            DBUpdate.Builder update, boolean upsert, boolean multi,
            WriteConcern concern) throws MongoException {
    public WriteResult<T, K> update(DBQuery.Query query, T object,
            boolean upsert, boolean multi, WriteConcern concern)
    public WriteResult<T, K> update(DBObject query, DBObject object,
            boolean upsert, boolean multi) throws MongoException {
    public WriteResult<T, K> update(DBQuery.Query query,
            DBUpdate.Builder update, boolean upsert, boolean multi)
    public WriteResult<T, K> update(DBQuery.Query query, T object,
            boolean upsert, boolean multi) throws MongoException {
    public WriteResult<T, K> update(DBObject query, DBObject object)
    public WriteResult<T, K> update(DBQuery.Query query, DBUpdate.Builder update)
    public WriteResult<T, K> update(DBQuery.Query query, T object)
    public WriteResult<T, K> updateById(K id, T object) throws MongoException {
    public WriteResult<T, K> updateById(K id, DBUpdate.Builder update)
```
##### JacksonMongoCollection:
TODO: Write documentation

#### UpdateMulti
##### JacksonDBCollection:
```java
    public WriteResult<T, K> updateMulti(DBObject query, DBObject object)
    public WriteResult<T, K> updateMulti(DBQuery.Query query,
    public WriteResult<T, K> updateMulti(DBQuery.Query query, T object)
```
##### JacksonMongoCollection:
TODO: Write documentation

#### Remove / Delete
##### JacksonDBCollection:
```java
    public WriteResult<T, K> remove(DBObject query, WriteConcern concern)
    public WriteResult<T, K> remove(DBQuery.Query query, WriteConcern concern)
    public WriteResult<T, K> remove(DBObject query) throws MongoException {
    public WriteResult<T, K> remove(DBQuery.Query query) throws MongoException {
    public WriteResult<T, K> removeById(K id) throws MongoException {
    public T findAndRemove(DBObject query) {
    public T findAndRemove(DBQuery.Query query) {
```
##### JacksonMongoCollection:
TODO: Write documentation

#### findAndModify
##### JacksonDBCollection:
```java
    public T findAndModify(DBObject query, DBObject fields, DBObject sort,
            boolean remove, DBObject update, boolean returnNew, boolean upsert) {
    public T findAndModify(DBObject query, DBObject fields, DBObject sort,
            boolean remove, T update, boolean returnNew, boolean upsert) {
    public T findAndModify(DBQuery.Query query, DBObject fields, DBObject sort,
            boolean remove, T update, boolean returnNew, boolean upsert) {
    public T findAndModify(DBQuery.Query query, DBObject fields, DBObject sort,
            boolean remove, DBUpdate.Builder update, boolean returnNew,
            boolean upsert) {
    public T findAndModify(DBObject query, DBObject fields, DBObject sort,
            boolean remove, DBUpdate.Builder update, boolean returnNew,
            boolean upsert) {
    public T findAndModify(DBObject query, DBObject sort, DBObject update) {
    public T findAndModify(DBObject query, DBObject sort,
            DBUpdate.Builder update) {
    public T findAndModify(DBQuery.Query query, DBObject sort,
            DBUpdate.Builder update) {
    public T findAndModify(DBObject query, DBObject update) {
    public T findAndModify(DBObject query, DBUpdate.Builder update) {
    public T findAndModify(DBQuery.Query query, DBUpdate.Builder update) {
```
##### JacksonMongoCollection:
TODO: Write documentation

#### Index management
##### JacksonDBCollection:
```java
    public void createIndex(DBObject keys) throws MongoException {
    public void createIndex(DBObject keys, DBObject options)
    public void ensureIndex(String name) {
    public void ensureIndex(DBObject keys) throws MongoException {
    public void ensureIndex(DBObject keys, String name) throws MongoException {
    public void ensureIndex(DBObject keys, String name, boolean unique)
    public void ensureIndex(DBObject keys, DBObject optionsIN)
    public void dropIndexes() throws MongoException {
    public void dropIndexes(String name) throws MongoException {
```
##### JacksonMongoCollection:
TODO: Write documentation

#### Hint
##### JacksonDBCollection:
```java
    public void setHintFields(List<DBObject> lst) {
```
##### JacksonMongoCollection:
TODO: Write documentation

#### Basic Query
##### JacksonDBCollection:
```java
    public org.mongojack.DBCursor<T> find(DBObject query) throws MongoException {
    public org.mongojack.DBCursor<T> find(DBQuery.Query query)
    public org.mongojack.DBCursor<T> find(DBObject query, DBObject keys) {
    public org.mongojack.DBCursor<T> find(DBQuery.Query query,
    public org.mongojack.DBCursor<T> find() throws MongoException {
```
##### JacksonMongoCollection:
TODO: Write documentation

#### Query One / Find One
##### JacksonDBCollection:
```java
    public T findOne() throws MongoException {
    public T findOneById(K id) throws MongoException {
    public T findOneById(K id, DBObject fields) throws MongoException {
    public T findOneById(K id, T fields) throws MongoException {
    public T findOne(DBObject query) throws MongoException {
    public T findOne(DBQuery.Query query) throws MongoException {
    public T findOne(DBObject query, DBObject fields) {
    public T findOne(DBQuery.Query query, DBObject fields) {
    public T findOne(DBObject query, DBObject fields, ReadPreference readPref) {
    public T findOne(DBQuery.Query query, DBObject fields,
```
##### JacksonMongoCollection:
TODO: Write documentation

#### Save
##### JacksonDBCollection:
```java
    public WriteResult<T, K> save(T object) {
    public WriteResult<T, K> save(T object, WriteConcern concern)
```
##### JacksonMongoCollection:
TODO: Write documentation

#### Collection Management 
##### JacksonDBCollection:
```java
    public void drop() throws MongoException {
    public JacksonDBCollection<T, K> rename(String newName)
    public JacksonDBCollection<T, K> rename(String newName, boolean dropTarget)
```
##### JacksonMongoCollection:
TODO: Write documentation

#### Counting
##### JacksonDBCollection:
```java
    public long count() throws MongoException {
    public long count(DBObject query) throws MongoException {
    public long getCount() throws MongoException {
    public long getCount(DBObject query) throws MongoException {
    public long getCount(DBQuery.Query query) throws MongoException {
    public long getCount(DBObject query, DBObject fields) throws MongoException {
    public long getCount(DBQuery.Query query, DBObject fields)
    public long getCount(DBObject query, DBObject fields, long limit, long skip)
    public long getCount(DBQuery.Query query, DBObject fields, long limit,
```
##### JacksonMongoCollection:
TODO: Write documentation

#### Group 
##### JacksonDBCollection:
```java
    public DBObject group(DBObject key, DBObject cond, DBObject initial,
    public DBObject group(DBObject key, DBObject cond, DBObject initial,
    public DBObject group(GroupCommand cmd) {
```
##### JacksonMongoCollection:
TODO: Write documentation

#### 
##### JacksonDBCollection:
```java
    public List distinct(String key) {
    public List distinct(String key, DBObject query) {
```
##### JacksonMongoCollection:
TODO: Write documentation

#### MapReduce 
##### JacksonDBCollection:
```java
    public com.mongodb.MapReduceOutput mapReduce(String map, String reduce,
    public com.mongodb.MapReduceOutput mapReduce(String map, String reduce,
    public com.mongodb.MapReduceOutput mapReduce(MapReduceCommand command)
    public <S, L> MapReduceOutput<S, L> mapReduce(
            MapReduce.MapReduceCommand<S, L> command) throws MongoException {
```
##### JacksonMongoCollection:
TODO: Write documentation

#### Aggregate 
##### JacksonDBCollection:
```java
    public <S> AggregationResult<S> aggregate(Aggregation<S> aggregation)
```
##### JacksonMongoCollection:
