package org.mongojack.aggregation;

import java.util.ArrayList;
import java.util.List;

import org.mongojack.Aggregation;

import com.mongodb.DBObject;

/**
 * The AggregationBuilder provides a fluent builder for creating mongo aggergation queries.
 * It works in conjunction with sub-builders that implement fluent builders for each of the
 * aggregation pipeline operations (match, group, project, sort, unwind)
 * 
 * This class is an attempt to simplify the usage of the mongojack
 * Aggregation feature which raps native Mongo Java Driver's aggergation.
 * 
 * See the mongodocs for how aggregation pipelines work
 * 
 * @see <a href="http://docs.mongodb.org/manual/core/aggregation-pipeline/">Aggregation Pipeline</a>
 * 
 * @param <T> The type of aggregation result object
 */
public class AggregationBuilder<T> {

    private final List<AggregatePipelineOperation> pipeline;

    public AggregationBuilder() {
        pipeline = new ArrayList<AggregatePipelineOperation>();
    }

    /**
     * Adds a match operation to the pipeline
     * 
     * @see MatchAggregation
     * 
     * @param match the Match operation
     * @return the builder
     */
    public AggregationBuilder<T> match(MatchAggregation match) {
        pipeline.add(match);
        return this;
    }

    /**
     * Adds an unwind operation to the pipeline
     * 
     * @see UnwindAggregation
     * 
     * @param unwind the Unwind operation
     * @return the builder
     */
    public AggregationBuilder<T> unwind(UnwindAggregation unwind) {
        pipeline.add(unwind);
        return this;
    }

    /**
     * Adds a group operation to the pipeline
     * 
     * @see GroupAggregation
     * 
     * @param group the Group operation
     * @return the builder
     */
    public AggregationBuilder<T> group(GroupAggregation group) {
        pipeline.add(group);
        return this;
    }

    /**
     * Adds a project operation to the pipeline
     * 
     * @see ProjectAggregation
     * 
     * @param project the Project operation
     * @return the builder
     */
    public AggregationBuilder<T> project(ProjectAggregation project) {
        pipeline.add(project);
        return this;
    }

    /**
     * Adds a sort operation to the pipeline
     * 
     * @see SortAggregation
     * 
     * @param sort the Sort operation
     * @return the builder
     */
    public AggregationBuilder<T> sort(SortAggregation sort) {
        pipeline.add(sort);
        return this;
    }

    /**
     * Adds a limit operation to the pipeline
     * 
     * @see LimitAggregation
     * 
     * @param limit the Limit operation
     * @return the builder
     */
    public AggregationBuilder<T> limit(LimitAggregation limit) {
        pipeline.add(limit);
        return this;
    }

    /**
     * Build the query. The
     * 
     * @param resultType The class of type T for which we create the typed Aggregation
     * @see org.mongojack.Aggregation
     * @return the typed aggregation query which can be passed to collection.aggregate()
     */
    public Aggregation<T> build(Class<T> resultType) {
        DBObject initialOp;
        DBObject[] additionalOpsArray;
        List<DBObject> additionalOpsList = new ArrayList<DBObject>();

        initialOp = pipeline.remove(0).apply();

        for (AggregatePipelineOperation op : pipeline) {
            additionalOpsList.add(op.apply());
        }

        additionalOpsArray = additionalOpsList.toArray(new DBObject[0]);
        return new Aggregation<T>(resultType, initialOp, additionalOpsArray);
    }

    @Override
    public String toString() {
        String output = "";
        for (AggregatePipelineOperation operation : pipeline) {
            output += operation + "\n";
        }
        return output;
    }

}
