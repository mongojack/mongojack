package org.mongojack.aggregation;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Fluent builder for the mongo Project aggregation pipeline operation
 * 
 * @see <a href="http://docs.mongodb.org/manual/reference/operator/aggregation/project/">Mongo Aggregation - Project</a>
 */
public class ProjectAggregation extends AggregatePipelineOperation {

    private DBObject project;
    private String parentKey = "$project";

    /**
     * Create a new projection operation and add a field mapping
     * 
     * @param inputField
     * @param outputField
     */
    public ProjectAggregation(String inputField, String outputField) {
        project = new BasicDBObject(parentKey, new BasicDBObject(outputField, dollar(inputField)));

    }

    /**
     * Create a new, empty projection operation
     */
    public ProjectAggregation() {
        project = new BasicDBObject("$project", new BasicDBObject());

    }

    /**
     * Create a new projection for a nested document
     * 
     * @param childName the field underwhich to created the new nested document
     */
    public ProjectAggregation(String childName) {
        project = new BasicDBObject();
        this.parentKey = childName;
    }

    /**
     * Set a new field mapping for the projection
     * 
     * @param inputField
     * @param outputField
     * @return the builder
     */
    public ProjectAggregation set(String inputField, String outputField) {

        BasicDBObject target = (BasicDBObject) project.get(parentKey);
        if (target == null)
            target = (BasicDBObject) project;

        target.append(outputField, dollar(inputField));

        return this;
    }

    /**
     * Set a new field mapping for the projection using the same key for the
     * input and output fields.
     * 
     * @param inputAndOutputField
     * @return the builder
     */
    public ProjectAggregation set(String inputAndOutputField) {

        BasicDBObject target = (BasicDBObject) project.get(parentKey);
        if (target == null)
            target = (BasicDBObject) project;

        target.append(inputAndOutputField, dollar(inputAndOutputField));

        return this;
    }

    /**
     * Remove the _id from the doc
     * 
     * @return the builder
     */
    public ProjectAggregation supressID() {

        BasicDBObject target = (BasicDBObject) project.get(parentKey);
        if (target == null)
            target = (BasicDBObject) project;

        target.append("_id", 0);

        return this;
    }

    /**
     * Sets the _id for the doc
     * 
     * @param inputField the _id to this field
     * @return the builder
     */
    public ProjectAggregation setId(String inputField) {

        BasicDBObject target = (BasicDBObject) project.get(parentKey);
        if (target == null)
            target = (BasicDBObject) project;

        target.append(inputField, "$_id");

        return this;
    }

    /**
     * Project into a nested document
     * 
     * @param nestedChild the nested projection
     * @return the builder
     */
    public ProjectAggregation into(ProjectAggregation nestedChild) {

        BasicDBObject target = (BasicDBObject) project.get(parentKey);
        if (target == null)
            target = (BasicDBObject) project;

        target.append(nestedChild.parentKey, nestedChild.apply());

        return this;
    }

    /**
     * Computes the sum of a list of fields
     * 
     * @param outputfield the new field that contains the result
     * @param operands
     * @return the builder
     */
    public ProjectAggregation add(String outputfield, String... operands) {
        return projectOperation("$add", outputfield, operands);
    }

    private ProjectAggregation projectOperation(String operator, String outputfield, String... operands) {

        BasicDBList inputFieldList = new BasicDBList();
        for (int i = 0; i < operands.length; i++) {
            inputFieldList.add(dollar(operands[i]));
        }

        BasicDBObject target = (BasicDBObject) project.get(parentKey);
        if (target == null)
            target = (BasicDBObject) project;

        target.append(outputfield, new BasicDBObject(operator, inputFieldList));

        return this;
    }

    @Override
    public String toString() {
        return project.toString();
    }

    @Override
    public DBObject apply() {
        return project;
    }

}
