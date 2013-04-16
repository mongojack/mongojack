Mapping to POJOs couldn't be easier!
====================================

Since MongoDB uses BSON, a binary form of JSON, to store its documents, a JSON mapper is a perfect mechanism for mapping Java objects to MongoDB documents.  And the best Java JSON mapper is Jackson.  Jackson's parsing/generating interface fits serialising to MongoDBs documents like a glove.  Its plugins, custom creators, serialisers, views, pluggable annotators and so on give this mapping library a massive head start, making it powerful, performant, and robust.

Quick start
-----------

This is what using MongoJack looks like:

    JacksonDBCollection<MyObject, String> coll = JacksonDBCollection.wrap(dbCollection, MyObject.class,
            String.class);
    MyObject myObject = ...
    WriteResult<MyObject, String> result = coll.insert(myObject);
    String id = result.getSavedId();
    MyObject savedObject = coll.findOneById(id);

Both the object itself and the id of the object are strongly typed.  If the id is generated, you can easily obtain it from the write result.

Features
--------

* Deserialises queried objects *directly from the MongoDB stream*, making it one of the (if not the) fastest object mappers for MongoDB out there.
* Uses Jackson for object mapping, so compatible with most Jackson features, including custom serialisers and deserialisers, creators, views, annotation introspectors, etc.
* Wraps the MongoDB driver DBCollection, providing most of the original methods, plus strongly typed versions.
* Gives low level access to advanced MongoDB driver features.
* Supports querying using objects as templates, and selecting fields to return using objects as templates.
* Supports mapping ObjectIds to strings and byte arrays, using an `@ObjectID` annotation.
* Supports `@javax.persistance.Id` annotation for marking which property is the id (or just call it `_id`).
* Provides interface to building update commands with update modifiers, which supports POJOs which will be serialised by Jackson.
* Provides terse chained query builders
* Supports database reference conventions, with convenience methods for fetching references and collections of references in one query.

Documentation
-------------

* [Installation](./installation.html)
* [Tutorial](./tutorial.html)
* Advanced
    * [Building Queries](./queries.html)
    * [ObjectId Handling](./object-ids.html)
    * [Database References](./dbrefs.html)
