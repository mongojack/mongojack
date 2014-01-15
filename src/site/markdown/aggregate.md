Aggregate
=========

Mongojacks supports aggregation operations: it groups values from multiple documents and perform operations to return a single result. You can find more infos about mongoDB Aggregation [here](http://docs.mongodb.org/manual/aggregation/).

Output object
-------------
An aggregation always produces an object from the generic type `AggregationResult<T>`. This object contains a list of object of desired type T.

Input object
------------
An aggregation needs a parameter of type `Aggregation<T>` which defines the operations to be done and specifies the type of the result. The following example defines an aggregation object which operates a match and defines the desired type `Pojo.class`:

```
DBObject initialOperation = new BasicDBObject("$match", new BasicDBObject("booleans", true));
Aggretation<Pojo> aggregation = new Aggregation<Pojo>(Pojo.class, initialOperation);
```

The aggregation can define several operations:

```
DBObject initialOperation = new BasicDBObject("$match", new BasicDBObject("string", Pattern.compile(".*")));
DBObject aditionalOperation = new BasicDBObject("$match", new BasicDBObject("integer", new BasicDBObject("$gt", new Integer(1))));
Aggregation<MockObject> aggregation = new Aggregation<MockObject>(MockObject.class, initialOperation, aditionalOperation);
```

Aggregate command
-----------------
Aggregation will be done by calling the JacksonDBCollection aggregate method:

```
Aggregation<User> aggregation = new Aggregation<User>(User.class, new BasicDBObject("string", Pattern.compile("string1")));
AggregationResult<User> aggregationResult = collection.aggregate(aggregation);
```
Once you have the `AggregationResult` object, it's possible to access the aggregated `User` objects by calling `aggregationResult.results()`.
