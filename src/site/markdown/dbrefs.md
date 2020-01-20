Database References
===================

The [MongoDB documentation](http://www.mongodb.org/display/DOCS/Database+References) for database references recommends against using database references, because it is usually simpler to just use direct/manual references.
The author of this documentation is of the same opinion.  However, MongoJack does support this convention of storage.

Declaring
---------

A field can be declared to be a DBRef by using the type `org.mongojack.DBRef`. For example:

    public class User {
        @Id
        public String name;
        public DBRef<City, String> city;
    }

The type parameters indicate the type of the object that is referenced and the type of the id of the referenced type.

Constructing
------------

Constructing the type can be done using a constructor:

    DBRef berlinRef = new DBRef<City, String>("Berlin", City.class, "cities", null);

The arguments are the id of the referenced document, the document class, the collection name and database name of the collection.
 An alternative method for constructing DBRefs may be used, by annotating the referenced class with a `org.mongojack.MongoCollection` annotation, which describes the name of the collection the type belongs to:

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

The ID and name of the collection can be accessed using simple getters.

    User user = userCollection.findOneById("userId");

If you want to fetch the referenced object, you can use `DbReferenceManager`, which can fetch single or multiple references from different collections.  We recommend initializing `DbReferenceManager`
with specific constructed collections, but it is possible to have `DbReferenceManager` create the necessary collection references for you, if they need no special setup.

    DbReferenceManager manager = new DbReferenceManager(mongoClient, "locations") // where "locations" is the default default database name to be used by the manager if the ref's contain no DB Name.
    City city = manager.fetch(user.city);
