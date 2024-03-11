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

5.x Usage
---------

This attempts to support MqlValues.  See some documentation (in the context of aggregation) [here](https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/aggregation-expression-operations/).

Ex from that documentation:
```java
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.mql.MqlValues.*;

class Foo {
    public void doSomething() {
        coll.aggregate(
            List.of(
                match(expr(
                    current()
                        .getArray("visitDates")
                        .size()
                        .gt(of(0))
                        .and(current()
                            .getString("state")
                            .eq(of("New Mexico")))
                )),
                group(current().getString("string"), min("integer", current().getInteger("integer")))
            ),
            MockObjectAggregationResult.class
        );
    }
}
```
