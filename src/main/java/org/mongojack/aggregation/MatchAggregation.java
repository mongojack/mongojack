package org.mongojack.aggregation;

import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Fluent builder for the mongo Match aggregation pipeline operation
 * 
 * @see <a href="http://docs.mongodb.org/manual/reference/operator/aggregation/match/#pipe._S_match">Mongo Aggregation -
 *      Match</a>
 */
public class MatchAggregation extends AggregatePipelineOperation {

    private DBObject match;

    /**
     * Create a new, empty match operation
     */
    public MatchAggregation() {
        match = new BasicDBObject("$match", new BasicDBObject());
    }

    /**
     * Create a new match operation matching on a field:value
     * 
     * @param field The field in the doc
     * @param value The value we want to match against
     */
    public MatchAggregation(String field, Object value) {
        match = new BasicDBObject("$match", new BasicDBObject(field, value));
    }

    /**
     * 
     * Add a match on field:value
     * 
     * @param field The field in the doc
     * @param value The value we want to match against
     * @return the builder
     */
    public MatchAggregation on(String field, Object value) {

        ((BasicDBObject) match.get("$match")).append(field, value);

        return this;
    }

    /**
     * Add an $exists check to the match
     * 
     * @param field The field we check for existance
     * @return the builder
     */
    public MatchAggregation exits(String field) {

        ((BasicDBObject) match.get("$match")).append(field, new BasicDBObject("$exists", true));

        return this;
    }

    /**
     * Add an NOT $exists check to the match
     * 
     * @param field The field we check for non-existance
     * @return the builder
     */
    public MatchAggregation notExits(String field) {

        ((BasicDBObject) match.get("$match")).append(field, new BasicDBObject("$exists", false));

        return this;
    }

    /**
     * Adds a greater than condition to the match
     * 
     * @param field the field in the doc we are comparing
     * @param value the value we are comparing against
     * @return the builder
     */
    public MatchAggregation greaterThan(String field, Object value) {
        return unaryComparison(field, "$gt", value);
    }

    /**
     * Adds a less than condition to the match
     * 
     * @param field the field in the doc we are comparing
     * @param value the value we are comparing against
     * @return the builder
     */
    public MatchAggregation lessThan(String field, Object value) {
        return unaryComparison(field, "$lt", value);
    }

    /**
     * Adds a greater than or equal to condition to the match
     * 
     * @param field the field in the doc we are comparing
     * @param value the value we are comparing against
     * @return the builder
     */
    public MatchAggregation greaterThanEqual(String field, Object value) {
        return unaryComparison(field, "$gte", value);
    }

    /**
     * Adds a less than or equal to condition to the match
     * 
     * @param field the field in the doc we are comparing
     * @param value the value we are comparing against
     * @return the builder
     */
    public MatchAggregation lessThanEqual(String field, Object value) {
        return unaryComparison(field, "$lte", value);
    }

    private MatchAggregation unaryComparison(String field, String operator, Object value) {
        ((BasicDBObject) match.get("$match")).append(field, new BasicDBObject(operator, value));
        return this;
    }

    /**
     * Add a two value comparison to the match
     * 
     * @param field the field in the doc we are comparing
     * @param greaterThanValue check to see if the field is greater than this value
     * @param lessThanValue check to see if the field is less than this value
     * @return A MatchAggregation
     */
    public MatchAggregation greaterThanOrLessThan(String field, Object greaterThanValue, Object lessThanValue) {
        BasicDBList list = new BasicDBList();
        list.add(new BasicDBObject(field, new BasicDBObject("$gt", greaterThanValue)));
        list.add(new BasicDBObject(field, new BasicDBObject("$lt", lessThanValue)));
        ((BasicDBObject) match.get("$match")).append("$or", list);
        return this;
    }

    /**
     * Add a two value comparison to the match
     * 
     * @param field the field in the doc we are comparing
     * @param greaterThanValue check to see if the field is greater or equal than this value
     * @param lessThanValue check to see if the field is less or equal than this value
     * @return A MatchAggregation
     */
    public MatchAggregation greaterThanEqualOrLessThanEqual(String field, Object greaterThanValue, Object lessThanValue) {
        BasicDBList list = new BasicDBList();
        list.add(new BasicDBObject(field, new BasicDBObject("$gte", greaterThanValue)));
        list.add(new BasicDBObject(field, new BasicDBObject("$lte", lessThanValue)));
        ((BasicDBObject) match.get("$match")).append("$or", list);
        return this;
    }

    /**
     * Add a two value comparison to the match
     * 
     * @param field the field in the doc we are comparing
     * @param greaterThanValue check to see if the field is greater than this value
     * @param lessThanValue check to see if the field is less than this value
     * @return A MatchAggregation
     */
    public MatchAggregation greaterThanAndLessThan(String field, Object greaterThanValue, Object lessThanValue) {
        return binaryComparison(field, "$gt", greaterThanValue, "$lt", lessThanValue);
    }

    /**
     * Add a two value comparison to the match
     * 
     * @param field the field in the doc we are comparing
     * @param greaterThanValue check to see if the field is greater or equal than this value
     * @param lessThanValue check to see if the field is less or equal than this value
     * @return A MatchAggregation
     */
    public MatchAggregation greaterThanEqualAndLessThanEqual(String field, Object greaterThanValue, Object lessThanValue) {
        return binaryComparison(field, "$gte", greaterThanValue, "$lte", lessThanValue);
    }

    /**
     * Add a two value comparison to the match
     * 
     * @param field the field in the doc we are comparing
     * @param greaterThanValue check to see if the field is greater than this value
     * @param lessThanValue check to see if the field is less or equal than this value
     * @return A MatchAggregation
     */
    public MatchAggregation greaterThanAndLessThanEqual(String field, Object greaterThanValue, Object lessThanValue) {
        return binaryComparison(field, "$gt", greaterThanValue, "$lte", lessThanValue);
    }

    /**
     * Add a two value comparison to the match
     * 
     * @param field the field in the doc we are comparing
     * @param greaterThanValue check to see if the field is greater than= this value
     * @param lessThanValue check to see if the field is less than this value
     * @return A MatchAggregation
     */
    public MatchAggregation greaterThanEqualAndLessThan(String field, Object greaterThanValue, Object lessThanValue) {
        return binaryComparison(field, "$gte", greaterThanValue, "$lt", lessThanValue);
    }

    private MatchAggregation binaryComparison(String field, String operator1, Object value1, String operator2, Object value2) {
        ((BasicDBObject) match.get("$match")).append(field, new BasicDBObject(operator1, value1).append(operator2, value2));
        return this;
    }

    /**
     * Add match criteria to check the field is not an empty object
     * Uses not-in operator. eg. field_name:{$nin:[{}]}
     * 
     * @param field The field we match as an non-empty object
     * @return the builder
     */
    public MatchAggregation notEmptyObject(String field) {
        BasicDBList listWithEmptyObject = new BasicDBList();
        listWithEmptyObject.add(new BasicDBObject());
        ((BasicDBObject) match.get("$match")).append(field, new BasicDBObject("$nin", listWithEmptyObject));

        return this;
    }

    /**
     * Add match criteria to check the field is an empty object
     * 
     * @param field The field we match as an empty object
     * @return the builder
     */
    public MatchAggregation emptyObject(String field) {
        BasicDBList listWithEmptyObject = new BasicDBList();
        listWithEmptyObject.add(new BasicDBObject());
        ((BasicDBObject) match.get("$match")).append(field, new BasicDBObject());

        return this;
    }

    /**
     * Add match criteria to check the field is one of a set of given values
     * Uses $in (in) operator. eg. field_name:{$in:["X", "Y", "Z"]}
     * 
     * @param field The field we match as an non-empty object
     * @return the builder
     */
    public MatchAggregation in(String field, String... values) {
        return arrayMatch("$in", field, values);
    }

    /**
     * Add match criteria to check the field is NOT one of a set of given values
     * Uses $nin (not-in) operator. eg. field_name:{$in:["X", "Y", "Z"]}
     * 
     * @param field The field we match as an non-empty object
     * @return the builder
     */
    public MatchAggregation nin(String field, String... values) {

        return arrayMatch("$nin", field, values);
    }

    private MatchAggregation arrayMatch(String inOrNotInOperator, String field, String... values) {
        BasicDBList matchArray = new BasicDBList();
        for (int i = 0; i < values.length; i++) {
            matchArray.add(values[i]);
        }
        ((BasicDBObject) match.get("$match")).append(field, new BasicDBObject(inOrNotInOperator, matchArray));

        return this;
    }

    /**
     * Add match condition that field value is one of the values in list.
     * e.g. {field: {$in: [x, y, z]}}
     * 
     * @param field
     * @param values
     * @return the builder
     */
    public MatchAggregation inList(String field, List<String> values) {
        return listMatch("$in", field, values);
    }

    /**
     * Add match condition that field value is not one of the values in list.
     * e.g. {field: {$nin: [x, y, z]}}
     * 
     * @param field
     * @param values
     * @return the builder
     */
    public MatchAggregation notInList(String field, List<String> values) {
        return listMatch("$nin", field, values);
    }

    private MatchAggregation listMatch(String operator, String field, List<String> values) {
        if (values != null) {
            BasicDBList matchArray = new BasicDBList();
            for (String s : values) {
                matchArray.add(s);
            }
            ((BasicDBObject) match.get("$match")).append(field, new BasicDBObject(operator, matchArray));
        }
        return this;
    }

    @Override
    public DBObject apply() {
        return match;
    }

    @Override
    public String toString() {
        return match.toString();
    }
}
