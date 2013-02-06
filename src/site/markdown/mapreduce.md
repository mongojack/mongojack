Map/reduce
==========

The Mongo Jackson Mapper supports deserialisation of map/reduce results into POJOs.  If you don't understand map/reduce, this is not the guide for you.  A good place to start would be in the [MongoDB documentation](http://www.mongodb.org/display/DOCS/MapReduce).

Output objects
--------------

The output of a map reduce command always produces an object with two keys.  One is ``_id``, this is the key that was emitted by the map function, and the other is ``value``, this is the reduced value for that key.  The Mongo Jackson Mapper expects you to provide a class that contains these two properties, with the correct types to deserialise to.  For example:

    public class UserComments {
        @Id
        public String user;
        @JsonProperty("value")
        public int count;
    }

The value class could be a more complex data structure if necessary.

MongoDB has two general approaches to where it outputs map/reduce results, one is to store them in RAM, this is known as inline.  The other is to store the results in a collection.

Map/reduce collection output
----------------------------

This can be done by using the output types ``REPLACE``, ``MERGE`` or ``REDUCE``.  For example:

    MapReduceOutput<UserComments, String> output = coll.mapReduce(MapReduce.build(
            "function() { emit(this.userId, 1);}",
            "function(k, vals) {var sum=0;for(var i in vals) sum += vals[i];return sum;}",
            MapReduce.OutputType.REPLACE, "userComments", UserComments.class, String.class));

This outputs the result of the given map and reduce functions to a collection called userComments.  The resulting output object contains a reference to a ``UserComments`` collection, with key type of ``String``.  It can be accessed by calling ``getOutputCollection()``:

    JacksonDBCollection<UserComments, String> outputColl = output.getOutputCollection();

If you just want to work with the full set of results now, you can iterate through them using the ``results()`` iterable:

    for (UserComments userComments : output.results()) {
        System.out.println("User " + userComments.user + " has written " + userComments.count + " comments.");
    }

If your collection is only for temporary use, you can drop the collection by calling ``drop()`` on the output object.

Map/reduce inline output
------------------------

When using inline output, ``getOutputCollection()`` will return ``null``.  Additionally, the output collection name and key type parameters to building the ``MapReduceCommand`` object may be ``null``.

Other map reduce parameters
---------------------------

Map/reduce can be combined with queries and other parameters by using the setters on the ``MapReduceCommand``.
