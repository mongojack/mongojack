Map/reduce
==========

MongoJack supports deserialisation of map/reduce results into POJOs.  You should fully understand the fundamentals of MapReduce in mongo.  You can find more information
[here](https://docs.mongodb.com/manual/core/map-reduce/).

Usage
-----
A MapReduce operation with a JacksonMongoCollection works exactly the same as with a MongoCollection, except that Jackson is used to map the result to a POJO.  A simple example might look like:

    public class Complex {
        public String _id;
        public Value value;
    }
    
    coll.insert(new MockObject("foo", 10));
    coll.insert(new MockObject("foo", 15));
    coll.insert(new MockObject("bar", 5));
    
    final MapReduceIterable<Complex> mrIterable = coll.mapReduce(
        // language=JavaScript
        "function map() {emit(this.string, {sum: this.integer, product: this.integer});}",
        // language=JavaScript
        "function reduce(k, vals) {var sum=0,product=1;for(var i in vals){sum+=vals[i].sum;product*=vals[i].product;}return {sum:sum,product:product};}",
        Complex.class
    );

    // then you could:
    mrIterable.collectionName(collection);
    mrIterable.action(MapReduceAction.REPLACE);
    mrIterable.toCollection();
    
    // or
    for (Complex result : mrIterable) {
        //do something with your result
    }
