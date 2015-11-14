/*
 * Copyright 2014 Christopher Exell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mongojack;

import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mongojack.DBProjection.ProjectionBuilder;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBSort.SortBuilder;
import org.mongojack.internal.query.QueryCondition;

/**
 * A Generic Aggregation object that allows the aggregation operations,
 * and the return type of the AggregationResult to be specified.
 * 
 * @param <T> The type of results to be produced by the aggregation results.
 * 
 * @author Christopher Exell
 * @since 2.1.0
 */
public class Aggregation<T> {
    private Class<T> resultType;
    private DBObject initialOp;
    private DBObject[] additionalOps;

    public Aggregation(Class<T> resultType, DBObject initialOp, DBObject... additionalOps)
    {
        this.resultType = resultType;
        this.initialOp = initialOp;
        this.additionalOps = additionalOps;
    }

    public Class<T> getResultType() {
        return resultType;
    }

    public DBObject getInitialOp() {
        return initialOp;
    }

    public DBObject[] getAdditionalOps() {
        return additionalOps;
    }

    public List<DBObject> getAllOps() {
        List<DBObject> allOps = new ArrayList<DBObject>();
        allOps.add(initialOp);
        allOps.addAll(Arrays.asList(additionalOps));
        return allOps;
    }

    public static Pipeline<Group.Accumulator> group(Expression<?> key, Map<String, Group.Accumulator> calculatedFields) {
        return new Pipeline<Group.Accumulator>(Group.by(key).set(calculatedFields));
    }

    public static Pipeline<Group.Accumulator> group(Expression<?> key) {
        return new Pipeline<Group.Accumulator>(Group.by(key));
    }

    public static Pipeline<Group.Accumulator> group(String key) {
        return new Pipeline<Group.Accumulator>(Group.by(Expression.path(key)));
    }

    public static Pipeline<Void> limit(int n) {
        return new Pipeline<Void>(new Limit(n));
    }

    public static Pipeline<Void> match(Query query) {
        return new Pipeline<Void>(new Match(query));
    }

    public static Pipeline<Expression<?>> project(ProjectionBuilder projection) {
        return new Pipeline<Expression<?>>(new Project(projection));
    }

    public static Pipeline<Expression<?>> project(String field) {
        return new Pipeline<Expression<?>>(Project.fields(field));
    }

    public static Pipeline<Expression<?>> project(String field, Expression<?> value) {
        return new Pipeline<Expression<?>>(Project.field(field, value));
    }

    public static Pipeline<Void> skip(int n) {
        return new Pipeline<Void>(new Skip(n));
    }

    public static Pipeline<Void> sort(SortBuilder builder) {
        return new Pipeline<Void>(new Sort(builder));
    }

    public static Pipeline<Void> unwind(String path) {
        return new Pipeline<Void>(new Unwind(path));
    }

    private static abstract class SimpleStage implements Pipeline.Stage<Void> {

        @Override
        public Pipeline.Stage<Void> set(String field, Void value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Pipeline.Stage<Void> set(Map<String, Void> fields) {
            throw new UnsupportedOperationException();
        }
    }

    public static class Group implements Pipeline.Stage<Group.Accumulator> {
        public enum Op { $addToSet, $avg, $first, $last, $max, $min, $push, $sum };

        private static final Accumulator COUNT = sum(Expression.literal(1));

        private final Expression<?> key;
        private final Map<String, Accumulator> calculatedFields = new LinkedHashMap<String, Accumulator>();

        private Group(Expression<?> key) {
            this.key = key;
        }

        public static Group by(Expression<?> key) {
            return new Group(key);
        }

        public static Group by(String key) {
            return new Group(Expression.path(key));
        }

        public static Accumulator distinct(Expression<?> expression) {
            return new Accumulator(Op.$addToSet, expression);
        }

        public static Accumulator distinct(String... path) {
            return new Accumulator(Op.$addToSet, Expression.path(path));
        }

        public static Accumulator average(Expression<?> expression) {
            return new Accumulator(Op.$avg, expression);
        }

        public static Accumulator average(String... path) {
            return new Accumulator(Op.$avg, Expression.path(path));
        }

        public static Accumulator first(Expression<?> expression) {
            return new Accumulator(Op.$first, expression);
        }

        public static Accumulator first(String... path) {
            return new Accumulator(Op.$first, Expression.path(path));
        }

        public static Accumulator last(Expression<?> expression) {
            return new Accumulator(Op.$last, expression);
        }

        public static Accumulator last(String... path) {
            return new Accumulator(Op.$last, Expression.path(path));
        }

        public static Accumulator max(Expression<?> expression) {
            return new Accumulator(Op.$max, expression);
        }

        public static Accumulator max(String... path) {
            return new Accumulator(Op.$max, Expression.path(path));
        }

        public static Accumulator min(Expression<?> expression) {
            return new Accumulator(Op.$min, expression);
        }

        public static Accumulator min(String... path) {
            return new Accumulator(Op.$min, Expression.path(path));
        }

        public static Accumulator list(Expression<?> expression) {
            return new Accumulator(Op.$push, expression);
        }

        public static Accumulator list(String... path) {
            return new Accumulator(Op.$push, Expression.path(path));
        }

        public static Accumulator sum(Expression<?> expression) {
            return new Accumulator(Op.$sum, expression);
        }

        public static Accumulator sum(String... path) {
            return new Accumulator(Op.$sum, Expression.path(path));
        }

        public static Accumulator count() {
            return COUNT;
        }

        /** Immutable pair of accumulator operation and expression. */
        public static class Accumulator {
            public final Op operator;
            public final Expression<?> expression;

            private Accumulator(Op operator, Expression<?> expression) {
                this.operator = operator;
                this.expression = expression;
            }
        }

        @Override
        public Group set(String field, Accumulator value) {
            calculatedFields.put(field, value);
            return this;
        }

        @Override
        public Group set(Map<String, Accumulator> calculatedFields) {
            this.calculatedFields.putAll(calculatedFields);
            return this;
        }

        public Expression<?> key() {
            return key;
        }

        public Set<Entry<String, Accumulator>> calculatedFields() {
            return calculatedFields.entrySet();
        }
    }

    public static class Limit extends SimpleStage implements Pipeline.Stage<Void> {
        private final int n;

        private Limit(int n) {
            this.n = n;
        }

        public int limit() {
            return n;
        }
    }

    public static class Match extends DBQuery.AbstractBuilder<Match> implements Pipeline.Stage<Void> {
        private final Query query;

        private Match() {
            this(DBQuery.empty());
        }

        private Match(Query query) {
            this.query = query;
        }

        @Override
        protected Match put(String op, QueryCondition value) {
            query.put(op, value);
            return this;
        }

        @Override
        protected Match put(String field, String op, QueryCondition value) {
            query.put(field, op, value);
            return this;
        }

        @Override
        protected Match putGroup(String op, Query... expressions) {
            query.putGroup(op, expressions);
            return this;
        }

        @Override
        public Pipeline.Stage<Void> set(String field, Void value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Pipeline.Stage<Void> set(Map<String, Void> fields) {
            throw new UnsupportedOperationException();
        }

        public Query query() {
            return query;
        }
    }

    public static class Project implements Pipeline.Stage<Expression<?>> {
        private final ProjectionBuilder builder;

        private Project(ProjectionBuilder builder) {
            this.builder = builder;
        }

        private Project(String field, Expression<?> value) {
            this.builder = DBProjection.include();
            set(field, value);
        }

        public static Project fields(String... fields) {
            return new Project(DBProjection.include(fields));
        }

        public static Project fields(Collection<String> fields) {
            return new Project(DBProjection.include(fields.toArray(new String[fields.size()])));
        }

        public static Project field(String field, Expression<?> value) {
            return new Project(field, value);
        }

        public static Project field(String field, String... path) {
            return new Project(field, Expression.path(path));
        }

        public Project excludeId() {
            builder.exclude("_id");
            return this;
        }

        @Override
        public Project set(String field, Expression<?> value) {
            builder.append(field, value);
            return this;
        }

        @Override
        public Project set(Map<String, Expression<?>> fields) {
            builder.putAll(fields);
            return this;
        }

        public ProjectionBuilder builder() {
            return builder;
        }
    }

    public static class Skip extends SimpleStage implements Pipeline.Stage<Void> {
        private final int n;

        private Skip(int n) {
            this.n = n;
        }

        public int skip() {
            return n;
        }
    }

    public static class Sort extends SimpleStage implements Pipeline.Stage<Void> {
        private final SortBuilder builder;

        private Sort(SortBuilder builder) {
            this.builder = builder;
        }

        public Sort asc(String field) {
            builder.asc(field);
            return this;
        }

        public Sort desc(String field) {
            builder.desc(field);
            return this;
        }

        public SortBuilder builder() {
            return builder;
        }
    }

    public static class Unwind extends SimpleStage implements Pipeline.Stage<Void> {
        private final String[] path;

        private Unwind(String... path) {
            this.path = path;
        }

        public FieldPath<Object> path() {
            return new FieldPath<Object>(path);
        }
    }

    /**
     * A fluent Aggregation builder.
     *
     * Type parameter S is the type of value that can be passed to set(String, S), given current latest stage.
     */
    public static class Pipeline<S> {
        public static interface Stage<S> {
            Stage<S> set(String field, S value);
            Stage<S> set(Map<String, S> fields);
        }

        private Stage<S> latestStage;
        private final List<Stage<?>> precedingStages;

        private Pipeline(Stage<S> latestStage, List<Stage<?>> precedingStages) {
            this.latestStage = latestStage;
            this.precedingStages = precedingStages;
        }

        public Pipeline(Stage<S> stage) {
            this(stage, new ArrayList<Stage<?>>());
        }

        public <X> Pipeline<X> then(Stage<X> stage) {
            Pipeline<X> result = (Pipeline<X>) this;
            result.precedingStages.add(latestStage);
            result.latestStage = stage;
            return result;
        }

        public Pipeline<Group.Accumulator> group(Expression<?> key, Map<String, Group.Accumulator> calculatedFields) {
            return then(Group.by(key).set(calculatedFields));
        }

        public Pipeline<Group.Accumulator> group(Expression<?> key) {
            return then(Group.by(key));
        }

        public Pipeline<Group.Accumulator> group(String... key) {
            return then(Group.by(Expression.path(key)));
        }

        public Pipeline<S> set(String field, S value) {
            latestStage.set(field, value);
            return this;
        }

        public Pipeline<Void> limit(int n) {
            return then(new Limit(n));
        }

        public Pipeline<Void> match(Query query) {
            return then(new Match(query));
        }

        public Pipeline<Expression<?>> project(ProjectionBuilder projection) {
            return then(new Project(projection));
        }

        public Pipeline<Expression<?>> project(String field) {
            return then(Project.field(field));
        }

        public Pipeline<Expression<?>> project(String field, Expression<?> value) {
            return then(new Project(field, value));
        }

        public Pipeline<Expression<?>> project(Collection<String> fields) {
            return then(Project.fields(fields));
        }

        public Pipeline<Expression<?>> projectFields(String... fields) {
            return then(Project.fields(fields));
        }

        public Pipeline<Expression<?>> projectField(String field, String... value) {
            return then(Project.field(field, value));
        }

        public Pipeline<Void> skip(int n) {
            return then(new Skip(n));
        }

        public Pipeline<Void> sort(SortBuilder builder) {
            return then(new Sort(builder));
        }

        public Pipeline<Void> unwind(String... path) {
            return then(new Unwind(path));
        }

        public Iterable<Stage<?>> stages() {
            ArrayList<Stage<?>> stages = new ArrayList<Stage<?>>(precedingStages.size() + 1);
            stages.addAll(precedingStages);
            stages.add(latestStage);
            return stages;
        }
    }

    /** Expression builder class. */
    public static abstract class Expression<T> {

        public static Expression<Object> path(String... path) {
            return new FieldPath<Object>(path);
        }

        public static Expression<Boolean> bool(String... path) {
            return new FieldPath<Boolean>(path);
        }

        public static Expression<Date> date(String... path) {
            return new FieldPath<Date>(path);
        }

        public static Expression<Integer> integer(String... path) {
            return new FieldPath<Integer>(path);
        }

        public static Expression<List<?>> list(String... path) {
            return new FieldPath<List<?>>(path);
        }

        public static Expression<Number> number(String... path) {
            return new FieldPath<Number>(path);
        }

        public static Expression<String> string(String... path) {
            return new FieldPath<String>(path);
        }

        public static <T> Expression<T> literal(T value) {
            return new Literal<T>(value);
        }

        public static Expression<Object> object(Map<String, Expression<?>> properties) {
            return new ExpressionObject(properties);
        }

        // Boolean Operator Expressions
        
        public static Expression<Boolean> and(Expression<?>... operands) {
            return new OperatorExpression<Boolean>("$and", operands);
        }

        public static Expression<Boolean> not(Expression<?> operand) {
            return new OperatorExpression<Boolean>("$not", operand);
        }

        public static Expression<Boolean> or(Expression<?>... operands) {
            return new OperatorExpression<Boolean>("$or", operands);
        }

        // Set Operator Expressions
        
        public static Expression<Boolean> allElementsTrue(Expression<List<?>> set) {
            return new OperatorExpression<Boolean>("$allElementsTrue", set);
        }

        public static Expression<Boolean> anyElementTrue(Expression<List<?>> set) {
            return new OperatorExpression<Boolean>("$anyElementTrue", set);
        }

        public static Expression<List<?>> setDifference(Expression<List<?>> set1, Expression<List<?>> set2) {
            return new OperatorExpression<List<?>>("$setDifference", set1, set2);
        }

        public static Expression<Boolean> setEquals(Expression<List<?>>... sets) {
            return new OperatorExpression<Boolean>("$setEquals", sets);
        }

        public static Expression<List<?>> setIntersection(Expression<List<?>>... sets) {
            return new OperatorExpression<List<?>>("$setIntersection", sets);
        }

        public static Expression<Boolean> setIsSubset(Expression<List<?>> set1, Expression<List<?>> set2) {
            return new OperatorExpression<Boolean>("$setIsSubset", set1, set2);
        }

        public static Expression<List<?>> setUnion(Expression<List<?>>... sets) {
            return new OperatorExpression<List<?>>("$setUnion", sets);
        }

        // Comparison Operator Expressions
        
        public static Expression<Integer> compareTo(Expression<?> value1, Expression<?> value2) {
            return new OperatorExpression<Integer>("$cmp", value1, value2);
        }

        public static Expression<Boolean> equals(Expression<?> value1, Expression<?> value2) {
            return new OperatorExpression<Boolean>("$eq", value1, value2);
        }

        public static Expression<Boolean> greaterThan(Expression<?> value1, Expression<?> value2) {
            return new OperatorExpression<Boolean>("$gt", value1, value2);
        }

        public static Expression<Boolean> greaterThanOrEquals(Expression<?> value1, Expression<?> value2) {
            return new OperatorExpression<Boolean>("$gte", value1, value2);
        }

        public static Expression<Boolean> lessThan(Expression<?> value1, Expression<?> value2) {
            return new OperatorExpression<Boolean>("$lt", value1, value2);
        }

        public static Expression<Boolean> lessThanOrEquals(Expression<?> value1, Expression<?> value2) {
            return new OperatorExpression<Boolean>("$lte", value1, value2);
        }

        public static Expression<Boolean> notEquals(Expression<?> value1, Expression<?> value2) {
            return new OperatorExpression<Boolean>("$ne", value1, value2);
        }

        // Arithmetic Operator Expressions

        public static Expression<Number> add(Expression<Number>... numbers) {
            return new OperatorExpression<Number>("$add", numbers);
        }

        public static Expression<Number> divide(Expression<Number> number1, Expression<Number> number2) {
            return new OperatorExpression<Number>("$divide", number1, number2);
        }

        public static Expression<Number> mod(Expression<Number> number1, Expression<Number> number2) {
            return new OperatorExpression<Number>("$mod", number1, number2);
        }

        public static Expression<Number> multiply(Expression<Number>... numbers) {
            return new OperatorExpression<Number>("$multiply", numbers);
        }

        public static Expression<Number> subtract(Expression<Number> number1, Expression<Number> number2) {
            return new OperatorExpression<Number>("$subtract", number1, number2);
        }

        // String Operator Expressions

        public static Expression<String> concat(Expression<String>... strings) {
            return new OperatorExpression<String>("$concat", strings);
        }

        public static Expression<Integer> compareToIgnoreCase(Expression<String> string1, Expression<String> string2) {
            return new OperatorExpression<Integer>("$strcasecmp", string1, string2);
        }

        public static Expression<String> substring(Expression<String> string, Expression<Integer> start, Expression<Integer> length) {
            return new OperatorExpression<String>("$substr", string, start, length);
        }

        public static Expression<String> toLowerCase(Expression<String> string) {
            return new OperatorExpression<String>("$toLower", string);
        }

        public static Expression<String> toUpperCase(Expression<String> string) {
            return new OperatorExpression<String>("$toUpper", string);
        }

        // Array Operator Expressions

        public static Expression<Integer> size(Expression<List<?>> array) {
            return new OperatorExpression<Integer>("$size", array);
        }

        // Date Operator Expressions

        public static Expression<Integer> dayOfMonth(Expression<Date> date) {
            return new OperatorExpression<Integer>("$dayOfMonth", date);
        }

        public static Expression<Integer> dayOfWeek(Expression<Date> date) {
            return new OperatorExpression<Integer>("$dayOfWeek", date);
        }

        public static Expression<Integer> hour(Expression<Date> date) {
            return new OperatorExpression<Integer>("$hour", date);
        }

        public static Expression<Integer> millisecond(Expression<Date> date) {
            return new OperatorExpression<Integer>("$millisecond", date);
        }

        public static Expression<Integer> minute(Expression<Date> date) {
            return new OperatorExpression<Integer>("$minute", date);
        }

        public static Expression<Integer> month(Expression<Date> date) {
            return new OperatorExpression<Integer>("$month", date);
        }

        public static Expression<Integer> second(Expression<Date> date) {
            return new OperatorExpression<Integer>("$second", date);
        }

        public static Expression<Integer> week(Expression<Date> date) {
            return new OperatorExpression<Integer>("$week", date);
        }

        public static Expression<Integer> year(Expression<Date> date) {
            return new OperatorExpression<Integer>("$year", date);
        }

        // Conditional Operator Expressions

        public static <T> Expression<T> cond(Expression<Boolean> condition,
                Expression<? extends T> consequent, Expression<? extends T> alternative) {
            return new OperatorExpression<T>("$cond", condition, consequent, alternative);
        }

        public static <T> Expression<T> ifNull(Expression<? extends T> expression, Expression<? extends T> replacement) {
            return new OperatorExpression<T>("$ifNull", expression, replacement);
        }
    }

    public static final class FieldPath<T> extends Expression<T> {
        private final String[] path;

        private FieldPath(String... path) {
            this.path = path;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("$").append(path[0]);
            for (int i = 1; i < path.length; ++i) {
                sb.append('.').append(path[i]);
            }
            return sb.toString();
        }
    }

    public static class ExpressionObject extends Expression<Object> {
        private final Map<String, Expression<?>> properties;

        private ExpressionObject(Map<String, Expression<?>> properties) {
            this.properties = properties;
        }

        public Set<Map.Entry<String, Expression<?>>> properties() {
            return properties.entrySet();
        }
    }

    public static class Literal<T> extends Expression<T> {
        private final T value;

        private Literal(T value) {
            this.value = value;
        }

        public T value() {
            return value;
        }
    }

    public static class OperatorExpression<T> extends Expression<T> {
        private final String operator;
        private final Expression<?>[] operands;

        private OperatorExpression(String operator, Expression<?>... operands) {
            this.operator = operator;
            this.operands = operands;
        }

        public String operator() {
            return operator;
        }

        public Iterable<Expression<?>> operands() {
            return Arrays.asList(operands);
        }
    }
}
