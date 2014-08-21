package org.mongojack.aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Fluent builder for the mongo Group aggregation pipeline operation
 * 
 * @see <a href="http://docs.mongodb.org/manual/reference/operator/aggregation/group/#pipe._S_group">Mongo Aggregation -
 *      Group</a>
 */
public class GroupAggregation extends AggregatePipelineOperation {

    private DBObject group;

    /**
     * "Group-By" multiple fields
     * 
     * @param field the first field in the group-by
     * @param moreFields the other fields
     */
    public GroupAggregation(String field, String... moreFields) {
        DBObject groupFields;

        List<String> fieldList = new ArrayList<String>();
        fieldList.add(field);

        if (moreFields != null)
            fieldList.addAll(Arrays.asList(moreFields));

        if (fieldList.size() == 1) {
            groupFields = new BasicDBObject("_id", dollar(field));
        } else {
            BasicDBObject groupFieldList = new BasicDBObject();

            for (String groupByfield : fieldList) {
                groupFieldList.put(groupByfield, dollar(groupByfield));
            }
            groupFields = new BasicDBObject("_id", groupFieldList);
        }
        this.group = new BasicDBObject("$group", groupFields);
    }

    /**
     * "Group-By" a single field
     * 
     * @param field The field to group by
     */
    public GroupAggregation(String field) {
        this(field, (String[]) null);
    }

    /**
     * Group by an empty doc. Useful for counting everything
     */
    public GroupAggregation() {
        this.group = new BasicDBObject("$group", new BasicDBObject("_id", new BasicDBObject()));
    }

    /**
     * Add a sum operation to the grouping.
     * 
     * @param alias The name of the newly created sum field
     * @param fieldName The field in the original doc we wish to sum
     * @return the builder
     */
    public GroupAggregation withSum(String alias, String fieldName) {
        return withGroupAccumulator("$sum", alias, fieldName);
    }

    /**
     * Add an average operation to the grouping.
     * 
     * @param alias The name of the newly created average field
     * @param fieldName The field in the original doc we wish to average
     * @return the builder
     */
    public GroupAggregation withAvg(String alias, String fieldName) {
        return withGroupAccumulator("$avg", alias, fieldName);
    }

    /**
     * Add a min operation to the grouping.
     * 
     * @param alias The name of the newly created min field
     * @param fieldName The field in the original doc we wish find the min of
     * @return the builder
     */
    public GroupAggregation withMin(String alias, String fieldName) {
        return withGroupAccumulator("$min", alias, fieldName);
    }

    /**
     * Add a max operation to the grouping.
     * 
     * @param alias The name of the newly created max field
     * @param fieldName The field in the original doc we wish find the max of
     * @return the builder
     */
    public GroupAggregation withMax(String alias, String fieldName) {
        return withGroupAccumulator("$max", alias, fieldName);
    }

    private GroupAggregation withGroupAccumulator(String accumulator, String alias, String fieldName) {

        ((BasicDBObject) group.get("$group")).append(alias, new BasicDBObject(accumulator, dollar(fieldName)));

        return this;
    }

    /**
     * Add an document count operation to the grouping
     * 
     * @param alias The name of the newly created counter field
     * @return the builder
     */
    public GroupAggregation withCount(String alias) {
        ((BasicDBObject) group.get("$group")).append(alias, new BasicDBObject("$sum", 1));
        return this;
    }

    /**
     * Add the operation $addToSet to the grouping.
     * 
     * @param alias new name of field for array
     * @param fieldName in original document
     * @return the builder
     */
    public GroupAggregation withUniqueList(String alias, String fieldName) {
        return withGroupAccumulator("$addToSet", alias, fieldName);
    }

    @Override
    public String toString() {
        return group.toString();
    }

    @Override
    public DBObject apply() {
        return group;
    }

}
