Querying
========

MongoJack allows you to query MongoDB in exactly the same way that you query using the Java MongoDB driver, with a few extra features for convenience.

DBQuery
-------

`org.mongojack.DBQuery` is a utility class for building queries, which provides methods that implement the [query operators](http://www.mongodb.org/display/DOCS/Advanced+Queries) that MongoDB provides.  It is similar to `com.mongodb.QueryBuilder`, however it allows a slightly more terse syntax.  The most simple element it supports is `is`, for example:

    coll.find(DBQuery.is("username", "jsmith"));

Multiple operators can be chained together:

    coll.find(DBQuery.greaterThan("age", 21).exists("parent"));

DBCursor
--------

`org.mongojack.DBCursor` also implements the same `DBQuery` interface, allowing even simpler chaining of commands:

    List<BlogPost> posts = coll.find().in("tags", "mongodb", "java", "jackson")
            .is("published", true).limit(10).toArray();

Sorting
-------

MongoJack provides a convenient helper utility for building sort specifications called `org.mongojack.DBSort`:

    List<BlogPost> posts = coll.find().sort(DBSort.desc("date")).toArray();

Projections
-----------

If you don't want to load the entire object, you can use projections to either include or exclude fields.  MongoJack provides a helper utility for building these called `org.mongojack.DBProjection`:

    List<BlogPost> posts = coll.find(DBQuery.is("published", true),
            DBProjection.include("title", "author")).toArray();

Serialization
-------------

MongoJack will attempt to serialise values according to the serialisation configuration of the bean.  This is, however, only done on a best effort basis.  In some cases it may not be possible to serialise all values, for example, when using polymorphic types or ungenercised collections.
