Using Object IDs
================

The simplest way to use ObjectId elements, both for document IDs and also for references to other documents (or anything) is to declare the field to have a type of `org.bson.types.ObjectId`.
MongoJack will handle serialisation/deserialisation of these with no problems.  If you name the ID field "_id", no extra configuration is required.  If you want to name the primary ID field
something else, you will need to annotate it with `javax.persistence.Id`.

However, there are some use cases where you may not want to use the ObjectId type, for example, if the same objects get serialised to the web, or if you just prefer dealing with more basic types.
MongoJack provides *some* support for this, though there are a few areas that you need to be careful about.

Other Types
-----------

If you declare your "_id" field (or a field annotated with `@javax.persistence.Id`) to have any supported type (e.g. String, or Long), and generate or fill out the field value yourself,
MongoJack will generally save and retrieve the values adequately; it wont' use the built-in `org.bson.types.ObjectId`, but a Long or String id generated in some other way in the Java code should
be perfectly valid.

If you declare your "_id" of type String, Mongo's `insert` methods will generate an object-id value for the field, but it will be saved as a String, not an ObjectId, which probably isn't what you
want.

@ObjectId
---------

The `@org.mongojack.ObjectId` annotation can be added to any `String` or `byte[]` field to indicate that you want its value to be stored as an ObjectId.
It can also be added to `org.mongojack.DBRef` fields who's ID type paramater is `String` or `byte[]`, as well as collection/array fields and `Map` fields.
This annotation instructs MongoJack to serialise the given value into an ObjectId, and deserialise any encountered ObjectIds into the type of the field.  The following are all valid uses:

    public class MyClass {
        @ObjectId
        public String _id;
        @ObjectId
        public byte[] someArbitraryId;
        @ObjectId
        public List<String> arbitraryCollectionOfIds;
        @ObjectId
        public DBRef<AnotherClass, byte[]> anotherObject;
        @ObjectId
        public List<DBRef<AnnotherClass, String> collectionOfOtherObjects;
        @ObjectId
        public Map<String, String> mapOfStringsToObjectIds;
    }

What is not currently supported is multi dimensional arrays/collections.  Serialising map keys to ObjectIds are naturally not supported because MongoDB does not support ObjectIds as key values.

If using getters and setters, the annotation must be added to *both* the getter and the setter, for example:

    public class MyClass {
        private String someId;
        @ObjectId
        public String getSomeId() {
            return someId;
        }
        @ObjectId
        public void setSomeId(String someId) {
            this.someId = someId;
        }
    }

If using property based `@JsonCreator` methods, then it must be added to the parameter:

    public class MyClass {
        private final String someId;
        @JsonCreator
        public MyClass(@ObjectId @JsonProperty("someId") someId) {
            this.someId = someId;
        }
        @ObjectId
        public String getSomeId() {
            return someId;
        }
    }

If using custom (de)serialisers or delegate `@JsonCreator` methods, then you will need to handle the ObjectIds yourself.

If the setter for the `_id` field of the object is annotated with `@ObjectId`, then the `*ById()` methods on `JacksonMongoCollection` will use serialise the passed in parameter to an ObjectId for you, and `WriteResult.getSavedId()` will deserialise it from ObjectId.  If using a custom deserialiser for the bean, this will not work, even if the getter for the property is annotated with `@ObjectId`.  This may be fixed in future, and is dependent on support from Jackson to give better information about configured serialisers.
