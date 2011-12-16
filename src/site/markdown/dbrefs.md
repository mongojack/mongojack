Database References
===================

The [MongoDB documentation](http://www.mongodb.org/display/DOCS/Database+References) for database references recommends against using database references, because it is usually simpler to just use direct/manual references.  The author of this documentation is of the same opinion.  However, the Mongo Jackson Mapper does support this convention of storage.

Declaring
---------

A field can be declared to be a DBRef by using the type `net.vz.mongodb.jackson.DBRef`. For example:

    public class User {
        @Id
        public String name;
        public DBRef<City, String> city;
    }

The type parameters indicate the type of the object that is referenced and the type of the id of the referenced type.

Constructing
------------

Constructing the type can be done using a constructor:

    DBRef berlinRef = new DBRef<City, String>("Berlin", "cities");

The first argument is the id of the referenced document, and the second is the name of the collection the document lives in.  An alternative method for constructing DBRefs may be used, by annotating the referenced class with a `net.vz.mongodb.jackson.MongoCollection` annotation, which describes the name of the collection the type belongs to:

    @MongoCollection(name = "cities")
    public class City {
        @Id
        public String name;
        public String country;
        public int population;
    }

If the referenced class has this annotation, you can then construct a DBRef by passing in the type of the collection:

    DBRef berlinRef = new DBRef<City, String>("Berlin", City.class);

Using and fetching
------------------

The ID and name of the collection can be accessed using simple getters.  You may want to fetch the referenced object though, this can be done by using the convenient `fetch()` method on DBRef:

    User user = userCollection.findOneById("userId");
    City city = user.city.fetch();

The fetch method may only be used for DBRefs that have been returned by a `JacksonDBCollection`, calling `fetch()` on a DBRef that you constructed yourself will always return null.  You may also fetch only a subset of fields:

    City city = user.city.fetch(new BasicDbObject("population" : 1));

Collections of DBRefs
---------------------

You may use DBRefs in a collection or as values for a `Map`.  A common problem in object database mappers is the n+1 selects problem, where you want to get a collection of documents associated with another document, so you need to do one query to get the parent document, and then n queries to get each referenced document.  The mongo jackson mapper provides a means of avoiding this by supplying a `fetch()` method on `JacksonDBCollection`:

    public class BlogPost {
        @Id
        public String blogId;
        public List<DBRef<Comment, String>> comments;
    }

    BlogPost post = blogsCollection.findOneById(blogId);
    List<Comment> comments = blogsCollection.fetch(post.comments);

Using this method, only one query will be made to retrieve the saved comments.  If the references come from multiple different collections, then one query per collection will be made.  Simlarly to fetching a single reference, a list of fields to limit fetching to can be supplied.

ObjectIds
---------

DBRef ids will not automatically be stored as ObjectIds if the referenced object uses an ObjectId id.  To store a DBRef id as an ObjectId, either use `org.bson.types.ObjectId` as the type of its id, or annotate the reference with `@ObjectId`.  More details about ObjectIds can be read [here](./object-ids.html).

Deserialisation of references
-----------------------------

References are deserialised using the same `ObjectMapper` that the collection that the referring document came from uses.  This means any custom Jackson configuration required for the `ObjectMapper` of the reference needs to be also in the `ObjectMapper` of the referring collection.

