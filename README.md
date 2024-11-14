Mapping to POJOs couldn't be easier!
====================================

Since MongoDB uses BSON, a binary form of JSON, to store its documents, a JSON mapper is a perfect mechanism for mapping Java objects to MongoDB documents.  And the best Java JSON mapper is Jackson. 
Jackson's parsing/generating interface fits serialising to MongoDBs documents like a glove.  Its plugins, custom creators, serialisers, views, pluggable annotators and so on give this mapping library
a massive head start, making it powerful, performant, and robust.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.mongojack/mongojack/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.mongojack/mongojack)

Snapshots, when appropriate, are available in Sonatype's snapshot repositories.

5.x Updates
-----------

In the 5.x update, deprecated objects (Aggregation, DBProjection, DBQuery, DBSort, DBUpdate) have been removed
in favor of native mongo java driver constructs.  This is a work in progress.  For examples of how to replace
the deprecated constructs, check out the tests as they differ between 4.x and 5.x versions.

Also note that this moves the minimum supported JVM to 11.  Though it _should_ work up to 21.

Some "gotchas" and issues:
- Using `Updates` doesn't provide custom mapping, so if you have a custom serializer on a field in the object
  on which the collection is based, that won't take effect, and you will have to do the custom mapping yourself.

This attempts to support MqlValues.  See some documentation (in the context of aggregation) [here](https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/aggregation-expression-operations/).

Ex from that documentation:
```java
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.mql.MqlValues.*;

class Foo {
    public void doSomething() {
        coll.aggregate(
            List.of(
                match(expr(
                    current()
                        .getArray("visitDates")
                        .size()
                        .gt(of(0))
                        .and(current()
                            .getString("state")
                            .eq(of("New Mexico")))
                )),
                group(current().getString("string"), min("integer", current().getInteger("integer")))
            ),
            MockObjectAggregationResult.class
        );
    }
}
```

Project documentation
---------------------

The official documentation for this project lives [here](http://mongojack.org).

Mailing lists
-------------

The MongoDB Jackson Mapper users mailing list is hosted [here](http://groups.google.com/group/mongo-jackson-mapper).

Issues
------

The MongoDB Jackson Mapper issues are hosted [here](https://github.com/mongojack/mongojack/issues).

Quick start
-----------

### Mongo driver compatibility

- Version 2.3.0 and earlier are compatible only with the 2.x series mongo-java-driver.
- Version 2.5.0 to 2.10.0 are compatible with the 3.x series mongodb-driver using the legacy 3.x series APIs.
- Version 3.0.0 and later are compatible with versions 3.12.x and later of the mongo driver using mongodb-driver-sync without the legacy APIs.
- Version 4.N.x+, 5.N.x are compatible with the _matching_ mongo-java-driver driver, although patch versions are not kept in sync 

### Installation

#### Using a Java dependency manager

The quickest and easiest way to start using MongoJack is to use one of the standard Java build tools, such as Maven or Gradle.  For example:

Maven:
```xml
    <dependency>
      <groupId>org.mongojack</groupId>
      <artifactId>mongojack</artifactId>
      <version>5.0.0</version>
    </dependency>
```

Gradle:
```groovy
    implementation 'org.mongojack:mongojack:5.0.0'
```
or
```kotlin
    implementation("org.mongojack:mongojack:5.0.0")
```

### Writing code

*Note*: The 3.0 release of MongoJack removes all the functions of the older JacksonDBCollection, and all references to the legacy Mongo APIs.

MongoJack now supports _only_ usage of the java mongo driver's 3.x API.  There are two ways to use this feature.

1) Use the JacksonCodecRegistry class
2) Use the JacksonMongoCollection class

For a more detailed look at migration from 2.x to 3.x, see [the migration guide](MIGRATING.md)

### Using JacksonCodecRegistry
The java mongo 3.0 and higher driver supports the usage of codecs to map to specific types. MongoJack provides a Codec Registry which can be used for this purpose. Some example code making use of the JacksonCodecRegistry can be seen below:
    
    MongoClient mongo = new MongoClient();
    MongoDatabase mongoDatabase = mongo.getDatabase(testDatabaseName);
    JacksonCodecRegistry jacksonCodecRegistry = new JacksonCodecRegistry();
    jacksonCodecRegistry.addCodecForClass(MyObject.class);
    MongoCollection<?> coll = mongoDatabase.getCollection("testCollection");
    MongoCollection<MyObject> collection = coll.withDocumentClass(MyObject.class).withCodecRegistry(jacksonCodecRegistry);

The first two lines above get the database using the mongo driver. The third line constructs a new JacksonCodecRegistry. The fourth line tells the JacksonCodecRegistry to create a codec that will use Jackson for serialization/deserialization for the class MyObject. The fifth line gets a MongoCollection from the MongoDatabase, and the sixth tells the MongoCollection to use  the MyObject class and work with the JacksonCodecRegsitry setup on lines three and four. JacksonCodecRegistry includes the default Mongo codecs, so it will also be capable of serializing and deserializing the Document and other default classes.

### Using JacksonMongoCollection

JacksonMongoCollection is an implementation of MongoCollection which builds a JacksonCodecRegistry for you and adds some additional features such as mapping of queries and update documents.  As an implementation
of MongoCollection, it has all the features of the underlying driver, including map-reduce functionality, aggregation, transactions, etc.  To use a JacksonMongoCollection the user will first need to initialize it using the builder.

    MongoClient mongo = new MongoClient();
    JacksonMongoCollection<MyObject> collection = JacksonMongoCollection.builder()
        .withObjectMapper(customObjectMapper)
        .build(mongo, "testDatabase", "testCollection", MyObject.class);
    
The builder allows you to specify the collection in a number of different ways; see the code and JavaDoc for specific options.

Usage largely follow the same pattern as the JacksonDBCollection with a few exceptions.  In general, you use a JacksonMongoCollection as if it were a MongoCollection; it implements all methods and
delegates almost everything to an underlying collection.  The collection provides some additional helper methods, but in generally the interface is the same as `com.mongodb.client.MongoCollection`. 

Old JacksonMongoCollection:
    
    MockObject o1 = new MockObject("1", "ten", 10);
    MockObject o2 = new MockObject("2", "ten", 10);
    coll.insert(o1, o2, new MockObject("twenty", 20));
    List<MockObject> results = collection.find(new BasicDBObject("string", "ten")).toArray();

New:

    MockObject o1 = new MockObject("1", "ten", 10);
    MockObject o2 = new MockObject("2", "ten", 10);
    coll.insertMany(Arrays.asList(o1, o2, new MockObject("twenty", 20)));
    List<MockObject> results = collection.find(Filters.eq("string", "ten")).into(new ArrayList<>());
 
The biggest difference between the usage of `JacksonDBCollection` and `JacksonMongoCollection` is that is that most of the inputs to the methods in the API must implement the `org.bson.conversions.Bson` interface.  This
allows you to pass instances of `org.bson.Document`, or `com.mongodb.BasicDBObject`, or any of Mongo's helper/builder objects like `com.mongodb.client.model.Filters`, `com.mongodb.client.model.Aggregates`,
or `com.mongodb.client.model.Updates`.

MongoJack's older DBQuery, Updates, and Aggregation helpers should all still work with the new JacksonMongoCollection, but they have been deprecated as the Mongo driver provides a set of useful builders
for all of these things in the `com.mongodb.client.model` package.  The implementation attempts to do mapping on any `Bson` inputs.

### Using a custom ObjectMapper

If you want to use a custom ObjectMapper, you need to install MongoJackModule on
your ObjectMapper before using it.  This can be done with one of two mechanisms.  The first one installs
the MongoJackModule, and also installs JavaTimeModule and changes some other settings on the object mapper.

    ObjectMapper customObjectMapper = new ObjectMapper()
    // ... configure your object mapper
    ObjectMapperConfigurer.configureObjectMapper(customObjectMapper)
    // ...
    MongoClient mongo = new MongoClient();
    JacksonMongoCollection<MyObject> collection = JacksonMongoCollection.builder()
        .withObjectMapper(customObjectMapper)
        .build(mongo, "testDatabase", "testCollection", MyObject.class);

If you want to install _only_ the module itself, you can use the following, which installs the module
but makes no other changes:


    ObjectMapper customObjectMapper = new ObjectMapper()
    // ... configure your object mapper
    ObjectMapperConfigurer.addMongojackModuleOnly(customObjectMapper)
    // ...
    MongoClient mongo = new MongoClient();
    JacksonMongoCollection<MyObject> collection = JacksonMongoCollection.builder()
        .withObjectMapper(customObjectMapper)
        .build(mongo, "testDatabase", "testCollection", MyObject.class);

