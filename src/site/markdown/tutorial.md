Tutorial
========

Creating a Jackson collection is done by calling one of the `wrap()` factory methods on `JacksonDBCollection`:

    JacksonDBCollection<MyObject, String> coll = JacksonDBCollection.wrap(dbCollection, MyObject.class,
            String.class);

Inserting looks very similar to using the MongoDB Java driver:

    MyObject myObject = ...
    WriteResult<MyObject, String> result = coll.insert(myObject);

The returned `WriteResult` has the same methods as the MongoDB Java driver, but also gives you access to the ID the object was saved under, useful for when you are letting the database generate an ID for you:

    String id = result.getSavedId();

You can now load the object by that id:

    MyObject foundObject = coll.findOneById(id);

Querying can be done using DBObjects:

    DBCursor<MyObject> cursor = coll.find(BasicDBObject("prop", "value"));
    if (cursor.hasNext()) {
        MyObject firstObject = cursor.next();
    }

Or, if your object contains no primitive types, and you only want basic equals comparison to values other than null, you can pass your object into the query as a template:

    MyObject query = ...
    DBCursor<MyObject> cursor = coll.find(query);
    if (cursor.hasNext()) {
        MyObject firstObject = cursor.next();
    }

In fact, you can do the same for specifying which fields to return, just pass in a template of your object with any fields you want returned set not to null.

The collection, cursor and write result interfaces are very similar to the standard Java MongoDB driver. Most methods have been copied across, with generic typing added where appropriate, and overloading to use the generic type where sometimes the generic type is not powerful enough, such as for queries and specifying fields for partial objects.

When it comes to mapping your objects, generally all you need to use is the Jackson annotations, such as `@JsonProperty` and `@JsonCreator`.  If you want a type of `ObjectId`, you have two options, either make your field be of type `ObjectId`, or you can also use `String`, as long as you annotate *both* the serialising and deserialising properties with `@net.vz.mongodb.jackson.ObjectId`.  For example:

    public class MyObject {
      private String id;
      @ObjectId
      @JsonProperty("_id")
      public String getId() {
        return id;
      }
      @ObjectId
      @JsonProperty("_id")
      public void setId(String id) {
        this.id = id;
      }
    }

Now your id property will be stored in the database as an object ID, and you can let MongoDB generate it for you.  More information about ObjectIds can be found [here](./object-ids.html).  You might not like annotating your ids with `@JsonProperty("_id")`, the mapper supports `@javax.persistence.Id` as a short hand for this:

    public class MyObject {
      @Id
      public Long id;
    }

Another useful implication of this is if you want to use the same object for database objects and objects to return on the web, you can name the id whatever you want for the web, and you don't need to use Jackson views to to specify which property gets mapped to what name for the database and for the web.

The only limitation to using the id annotation is if you are using `@Creator` annotated constructors or factory methods, because `@javax.persistence.Id` is not supported on method parameters.  For this reason, the mapper provides the annotation `@net.vz.mongodb.jackson.Id`, and it can be used like so:

    public class MyObject {
      private final String id;
      @JsonCreator
      public MyObject(@Id @ObjectId id) {
        this.id = id;
      }
      @Id
      @ObjectId
      public String getId() {
        return id;
      }
    }

As you can see, immutable objects are also supported because Jackson supports them, something that most other frameworks don't support.

The mapper also provides an update builder for running updates using the MongoDB [modifier operations](http://www.mongodb.org/display/DOCS/Updating#Updating-ModifierOperations).  This supports serialisation of embedded objects, and can be used like this:

    coll.updateById("someid", DBUpdate.inc("someIntField").push(new EmebbedObject()));

If you're using references, the mapper makes it easy to work with them.  You can declare a property to be of type `net.vz.mongodb.jackson.DBRef`, which can be instantiated by supplying the ID and the collection name for the reference.  Alternatively, you can supply the class of that reference, if that class is annotated with `@MongoCollection`.  Having loaded the saved object with the reference, you can load the reference by calling `fetch()`:

    @MongoCollection("comments")
    public class Comment {
        @Id
        public String id;
        public String text;
    }

    public class Post {
        @Id
        public String id;
        @ObjectId
        public List<DBRef<Comment, String>> comments;
    }

    Post post = coll.findOneById(someId);
    for (DBRef<Comment, String> comment : post.comments) {
        System.out.println(comment.fetch().text);
    }

To avoid the *n+1 selects* issue, you can use the more efficient `fetch()` method on `JacksonDBCollection`:

    List<Comment> comments = coll.fetch(post.comments);

More information about DBRefs can be found [here](./dbrefs.html).

If you're using your data objects for both storage and web views, you might want to take advantage of Jacksons views feature, so that generated/transient properties aren't persisted, and properties that you don't want leaked and serialised to the web.  The mapper supports this easily, by letting you pass in a view to the wrap method:

    JacksonDBCollection<MyObject, String> coll = JacksonDBCollection.wrap(DBCollection dbCollection, MyObject.class,
            String.class, DatabaseView.class);

Of course, if you really want to control things and Jackson's annotations aren't enough, the wrap method is also overloaded to accept an `ObjectMapper`.  For convenience, you should add the object ID module in order to get the object id and id annotation mapping features:

    ObjectMapper myObjectMapper = ...
    myObjectMapper.withModule(net.vz.mongodb.jackson.internal.ObjectIdModule.INSTANCE);
    JacksonDBCollection<MyObject, String> coll = JacksonDBCollection.wrap(DBCollection dbCollection, MyObject.class,
            String.class, myObjectMapper);



