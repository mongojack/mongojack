Tutorial
========

Creating a `JacksonMongoCollection` is done by calling one of the `JacksonMongoCollection.builder()`, configuring the builder as desired, and calling one of the build() methods:

    JacksonMongoCollection<BlogPost> coll = JacksonMongoCollection.builder().build(existingMongoClient, "databaseName", "collectionName", BlogPost.class);
    
or

    MongoCollection<BlogPost> coll = JacksonMongoCollection.builder().build(existingMongoClient, "databaseName", "collectionName", BlogPost.class);

You can then use the returned collection like any other MongoCollection instance, expecting it to accept and return mapped instances of your POJOs.

Inserting looks very similar to using the MongoDB Java driver:

    BlogPost blogPost = ...
    coll.insertOne(blogPost);

If the object id of the saved object was generated, you should be able to retrieve it from the object (assuming there's an accessor...):

    String id = blogPost.getId();

You can now load the object by that id:

    BlogPost foundBlogPost = coll.find(Filters.eq("_id", id)).first();

Querying can be done using general mongo methods:

    FindIterable<BlogPost> cursor = coll.find().filter(Filters.eq("published", true).in("tags", "mongodb", "java", "jackson"));
    for (BlogPost blogPost : cursor) {
        // do something with your post
    }

Most of the find methods take a query directly:

    FindITerable<BlogPost> cursor = coll.find(Filters.or(
            Filters.eq("author", "jsmith"), 
            Filters.size("comments", 3));

More information about building queries can be found [here](./queries.html).

Since JacksonMongoCollection _implements_ MongoCollection, all methods are present on the object, and they should all work as expected.  Returned iterables implement the underlying
mongo interfaces, so they should also all work as expected.

When it comes to mapping your objects, generally all you need to use is the Jackson annotations, such as `@JsonProperty` and `@JsonCreator`.  If you want a type of `ObjectId`, you have two options,
either make your field be of type `ObjectId`, or you can also use `String`, as long as you annotate *both* the serialising and deserialising properties with `@org.mongojack.ObjectId`.  For example:

    public class BlogPost {
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

Now your id property will be stored in the database as an object ID, and you can let MongoDB generate it for you.  More information about ObjectIds can be found [here](./object-ids.html).
You might not like annotating your ids with `@JsonProperty("_id")`, MongoJack supports `@javax.persistence.Id` as a short hand for this:

    public class BlogPost {
      @Id
      public Long id;
    }

Another useful implication of this is if you want to use the same object for database objects and objects to return on the web, you can name the id whatever you want for the web, and you don't need
to use Jackson views to to specify which property gets mapped to what name for the database and for the web.

The only limitation to using the id annotation is if you are using `@Creator` annotated constructors or factory methods, because `@javax.persistence.Id` is not supported on method parameters. 
For this reason, MongoJack provides the annotation `@org.mongojack.Id`, and it can be used like so:

    public class BlogPost {
      private final String id;
      @JsonCreator
      public BlogPost(@Id @ObjectId id) {
        this.id = id;
      }
      @Id
      @ObjectId
      public String getId() {
        return id;
      }
    }

As you can see, immutable objects are also supported because Jackson supports them, something that most other frameworks don't support.

You can use standard modify operations with your collection as well ([modifier operations](http://www.mongodb.org/display/DOCS/Updating#Updating-ModifierOperations)).  This should
support mapping objects on saving as well:

    coll.updateOne(Filters.eq("_id", "someid"), Updates.inc("numResponses").push("comments", new Comment("Great post!")));

If you're using references (which you probably shouldn't), MongoJack makes it easy to work with them.  You can declare a property to be of type `org.mongojack.DBRef`, which can be instantiated by
supplying the ID and the collection name for the reference.  Alternatively, you can supply the class of that reference, if that class is annotated with `@MongoCollection`.  Having loaded the saved
object with the reference, you can load the reference using `DBReferenceManager`:

    @MongoCollection(name = "comments")
    public class Comment {
        @Id
        public String id;
        public String text;
    }

    public class BlogPost {
        @Id
        public String id;
        @ObjectId
        public List<DBRef<Comment, String>> comments;
    }

    BlogPost post = coll.findOneById(someId);
    
    List<Comment> comments = dbReferenceManager.fetch(post.comments))
    

More information about DBRefs can be found [here](./dbrefs.html).

If you're using your data objects for both storage and web views, you might want to take advantage of Jacksons views feature, so that generated/transient properties aren't persisted, and properties
that you don't want leaked and serialised to the web.  MongoJack supports this easily, by letting you pass in a view to the wrap method:

    JacksonDBCollection<BlogPost, String> coll = JacksonMongoCollection.builder().view(DatabaseView.class).build(mongoClient, "foo", "bar", BlogPost.class);

Of course, if you really want to control things and Jackson's annotations aren't enough, the wrap method is also overloaded to accept an `ObjectMapper`.  When doing this, you must make sure to
configure the object mapper to use the mongo custom jackson configuration:

    ObjectMapper myObjectMapper = ...
    MongoJackModule.configure(myObjectMapper);
    JacksonMongoCollection<BlogPost> coll = JacksonMongoCollection.builder()
        .objectMapper(myObjectMapper)
        .build(existingMongoClient, "databaseName", "collectionName", BlogPost.class);
