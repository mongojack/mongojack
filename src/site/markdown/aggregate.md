Aggregate
=========

MongoJack supports aggregation operations through the functionality provided by the underlying MongoCollection.  You can find more info about mongoDB Aggregation [here](http://docs.mongodb.org/manual/aggregation/).

Usage
-----

You can find information on using the `aggregate` methods of MongoCollection [here](http://mongodb.github.io/mongo-java-driver/3.12/driver/tutorials/aggregation/).  MongoJack works directly with
these methods to provide mapping to output POJO's.

The aggregate method you will probably want to use looks like this:

    public <TResult> AggregateIterable<TResult> aggregate(final List<? extends Bson> pipeline, final Class<TResult> tResultClass)
    
The returned iterable can be iterated over, collected into a list or other object, or saved to a collection.  Pretty much all the documentation linked to above applies, but a very simple example
drawn from the mongo documentation might look like:

    public class RestaurantsByStar {
        public String _id;
        public Integer count;
    }

    collection.aggregate(
        Arrays.asList(
            Aggregates.match(Filters.eq("categories", "Bakery")),
            Aggregates.group("$stars", Accumulators.sum("count", 1))
        ),
        RestaurantsByStar.class
    )

