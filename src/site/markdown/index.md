Mapping to POJOs couldn't be easier!
====================================

Since MongoDB uses BSON, a binary form of JSON, to store its documents, a JSON mapper is a perfect mechanism for mapping Java objects to MongoDB documents.  And the best Java JSON mapper is Jackson.  Jackson's parsing/generating interface fits serialising to MongoDBs documents like a glove.  Its plugins, custom creators, serialisers, views, pluggable annotators and so on give this mapping library a massive head start, making it powerful, performant, and robust.

Quick start
-----------

This is what using MongoJack looks like:

    MongoClient mongo = ...;
    JacksonMongoCollection<MyObject> collection = JacksonMongoCollection.builder()
        .build(mongo, "testDatabase", "testCollection", MyObject.class);
    MyObject myObject = ...
    coll.insertOne(myObject);
    String id = myObject.getId();
    MyObject savedObject = coll.findOneById(id);

The object stored by teh collection is strongly typed, and does not need to be reconstructed from Bson on retrieval.
If the id is generated, it will be written into the inserted documents.

Features
--------

* Deserialises queried objects *using the mongo driver's Codec architecture*, making it one of the (if not the) fastest object mappers for MongoDB out there.
* Uses Jackson for object mapping, so compatible with most Jackson features, including custom serialisers and deserialisers, creators, views, annotation introspectors, etc.
* Can be used purely as a codec for a MongoCollection, or provides a wrapper collection that implements all MongoCollection functionality.
* Provides all MongoDB MongoCollection features.
* Supports mapping ObjectIds to strings and byte arrays, using an `@ObjectID` annotation.
* Supports `@javax.persistance.Id` annotation for marking which property is the id (or just call it `_id`).
* Maps POJOs provided in query or update Bson documents
* Supports database reference conventions, with convenience functionality for fetching references and collections of references in one query.

Documentation
-------------

* [Installation](./installation.html)
* [Tutorial](./tutorial.html)
* Advanced
    * [Building Queries](./queries.html)
    * [ObjectId Handling](./object-ids.html)
    * [Database References](./dbrefs.html)
