Querying
========

MongoJack allows you to query MongoDB in exactly the same way that you query using the Java MongoDB driver, with a few extra features for convenience.

Usage
-----

It is recommended that you use the driver's query builders.  The JacksonMongoCollection will try and serialize the fields of your query correctly using the mapper.

Queries will always return a FindIterable of the type backing the collection, and you map call any methods on that iterable freely.

Examples
--------

Simple equality:

    coll.find(Filters.eq("username", "jsmith"));

Multiple operators can be chained together:

    coll.find(Filters.gte("age", 21).exists("parent"));

You can pass a filter to the returned iterable:

    List<BlogPost> posts = coll.find().filter(
        Filters
            .in("tags", "mongodb", "java", "jackson")
            .is("published", true)
    )
        .limt(10)
        .into(new ArrayList<>());

Sorting
-------

MongoJack works as with standard MongoCollection iterables:

    FindIterable<BlogPost> posts = coll.find().sort(Sorts.orderBy(Sorts.descending("field1"), Sorts.ascending("field2")));

Projections
-----------

If you don't want to load the entire object, you can use projections to either include or exclude fields.  Use them just as you would with FindIterable:

    List<BlogPost> posts = coll.find(DBQuery.is("published", true))
        .projection(Projections.include("title", "author"))
        .into(new ArrayList<>());

Serialization
-------------

MongoJack will attempt to serialise values according to the serialisation configuration of the bean.  This is, however, only done on a best effort basis.  In some cases it may not be possible to
serialise all values, for example, when using polymorphic types or ungenercised collections.
