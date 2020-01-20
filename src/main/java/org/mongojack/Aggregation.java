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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.BsonDocument;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.mongojack.DBProjection.ProjectionBuilder;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBSort.SortBuilder;
import org.mongojack.internal.util.DocumentSerializationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * A Generic Aggregation object that allows the aggregation operations,
 * and the return type of the AggregationResult to be specified.
 * <p>
 * The Pipeline is a List&lt;Stage&gt;, and Stage is a Bson which makes it compatible with the methods in the JacksonMongoCollection that accept
 * Bson-list aggregate pipeline objects, and makes those methods interoperable with Mongo's internal Aggregates class.  But be warned
 * that Pipeline.initialize has to be called before Stage.toBsonDocument, or exceptions will result; similarly the list operations shouldn't be called
 * before initialize is called.  JacksonMongoCollection takes care of calling initialize for you.
 * </p>
 *
 * @param <T> The type of results to be produced by the aggregation results.
 * @author Christopher Exell
 * @since 2.1.0
 *
 * @deprecated Use com.mongodb.client.model.Aggregates
 */
@SuppressWarnings("unused")
@Deprecated
public class Aggregation<T> {

    public static Pipeline<Group.Accumulator> group(Expression<?> key, Map<String, Group.Accumulator> calculatedFields) {
        return new Pipeline<>(Group.by(key).set(calculatedFields));
    }

    public static Pipeline<Group.Accumulator> group(Expression<?> key) {
        return new Pipeline<>(Group.by(key));
    }

    public static Pipeline<Group.Accumulator> group(String key) {
        return new Pipeline<>(Group.by(Expression.path(key)));
    }

    public static Pipeline<Void> limit(int n) {
        return new Pipeline<>(new Limit(n));
    }

    public static Pipeline<Void> match(Query query) {
        return new Pipeline<>(new Match(query));
    }

    public static Pipeline<Expression<?>> project(ProjectionBuilder projection) {
        return new Pipeline<>(new Project(projection));
    }

    public static Pipeline<Expression<?>> project(String field) {
        return new Pipeline<>(Project.fields(field));
    }

    public static Pipeline<Expression<?>> project(String field, Expression<?> value) {
        return new Pipeline<>(Project.field(field, value));
    }

    public static Pipeline<Void> skip(int n) {
        return new Pipeline<>(new Skip(n));
    }

    public static Pipeline<Void> sort(SortBuilder builder) {
        return new Pipeline<>(new Sort(builder));
    }

    public static Pipeline<Void> unwind(String path) {
        return new Pipeline<>(new Unwind(path));
    }

    public interface Stage<S> extends Bson, InitializationRequiredForTransformation {

        Stage<S> set(String field, S value);

        Stage<S> set(Map<String, S> fields);

    }

    private static abstract class AbstractStage<S> implements Stage<S> {

        private ObjectMapper objectMapper;
        private JavaType type;

        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            return DocumentSerializationUtils.serializePipelineStage(objectMapper, type, this).toBsonDocument(tDocumentClass, codecRegistry);
        }

        @Override
        public void initialize(final ObjectMapper objectMapper, final JavaType type, final JacksonCodecRegistry codecRegistry) {
            this.objectMapper = objectMapper;
            this.type = type;
        }
    }

    private static abstract class SimpleStage extends AbstractStage<Void> implements Stage<Void> {

        @Override
        public Stage<Void> set(String field, Void value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Stage<Void> set(Map<String, Void> fields) {
            throw new UnsupportedOperationException();
        }
    }

    public static class Group extends AbstractStage<Group.Accumulator> implements Stage<Group.Accumulator> {
        public enum Op {$addToSet, $avg, $first, $last, $max, $min, $push, $sum}

        ;

        private static final Accumulator COUNT = sum(Expression.literal(1));

        private final Expression<?> key;
        private final Map<String, Accumulator> calculatedFields = new LinkedHashMap<>();

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

        /**
         * Immutable pair of accumulator operation and expression.
         */
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

    public static class Limit extends SimpleStage implements Stage<Void> {
        private final int n;

        private Limit(int n) {
            this.n = n;
        }

        public int limit() {
            return n;
        }
    }

    public static class Match extends DBQuery.AbstractBuilder<Match> implements Stage<Void> {
        private final Query query;
        private ObjectMapper objectMapper;
        private JavaType type;

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
        public Stage<Void> set(String field, Void value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Stage<Void> set(Map<String, Void> fields) {
            throw new UnsupportedOperationException();
        }

        public Query query() {
            return query;
        }

        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            return DocumentSerializationUtils.serializePipelineStage(objectMapper, type, this).toBsonDocument(tDocumentClass, codecRegistry);
        }

        @Override
        public void initialize(final ObjectMapper objectMapper, final JavaType type, final JacksonCodecRegistry codecRegistry) {
            this.objectMapper = objectMapper;
            this.type = type;
        }
    }

    public static class Project extends AbstractStage<Expression<?>> implements Stage<Expression<?>> {
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

    public static class Skip extends SimpleStage implements Stage<Void> {
        private final int n;

        private Skip(int n) {
            this.n = n;
        }

        public int skip() {
            return n;
        }
    }

    public static class Sort extends SimpleStage implements Stage<Void> {
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

    public static class Unwind extends SimpleStage implements Stage<Void> {
        private final String[] path;

        private Unwind(String... path) {
            this.path = path;
        }

        public FieldPath<Object> path() {
            return new FieldPath<>(path);
        }
    }

    public static class Out extends SimpleStage implements Stage<Void> {
        private final String collectionName;

        public Out(String collectionName) {
            this.collectionName = collectionName;
        }

        public String collectionName() {
            return collectionName;
        }
    }

    /**
     * A fluent Aggregation builder.
     * <p>
     * Type parameter S is the type of value that can be passed to set(String, S), given current latest stage.
     */
    public static class Pipeline<S> extends AbstractListDecorator<Stage<Object>> implements List<Stage<Object>>, InitializationRequiredForTransformation {

        private final List<Stage<Object>> allStages;

        private ObjectMapper objectMapper;
        private JavaType type;
        private JacksonCodecRegistry codecRegistry;

        @SuppressWarnings("unchecked")
        public Pipeline(Stage<S> stage) {
            allStages = new ArrayList<>();
            allStages.add((Stage<Object>) stage);
        }

        @SuppressWarnings("unchecked")
        public <X> Pipeline<X> then(Stage<X> stage) {
            Pipeline<X> result = (Pipeline<X>) this;
            result.allStages.add((Stage<Object>) stage);
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
            allStages.get(allStages.size() - 1).set(field, value);
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

        public Pipeline<Void> out(String collectionName) {
            return then(new Out(collectionName));
        }

        public List<Stage<Object>> stages() {
            ArrayList<Stage<Object>> stages = new ArrayList<>(allStages.size() + 1);
            stages.addAll(allStages);
            stages.forEach((stage) -> stage.initialize(objectMapper, type, codecRegistry));
            return stages;
        }

        @Override
        public void initialize(final ObjectMapper objectMapper, final JavaType type, final JacksonCodecRegistry codecRegistry) {
            this.objectMapper = objectMapper;
            this.type = type;
            this.codecRegistry = codecRegistry;
        }

        @Override
        protected List<Stage<Object>> delegate() {
            return stages();
        }

        @Override
        public Stage<Object> set(int index, Stage<Object> element) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void add(int index, Stage<Object> element) {
            throw new UnsupportedOperationException();
        }
        @Override
        public Stage<Object> remove(int index) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean addAll(int index, Collection<? extends Stage<Object>> c) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void replaceAll(UnaryOperator<Stage<Object>> operator) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void sort(Comparator<? super Stage<Object>> c) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Expression builder class.
     */
    public static abstract class Expression<T> {

        public static Expression<Object> path(String... path) {
            return new FieldPath<>(path);
        }

        public static Expression<Boolean> bool(String... path) {
            return new FieldPath<>(path);
        }

        public static Expression<Date> date(String... path) {
            return new FieldPath<>(path);
        }

        public static Expression<Integer> integer(String... path) {
            return new FieldPath<>(path);
        }

        public static Expression<List<?>> list(String... path) {
            return new FieldPath<>(path);
        }

        public static Expression<Number> number(String... path) {
            return new FieldPath<>(path);
        }

        public static Expression<String> string(String... path) {
            return new FieldPath<>(path);
        }

        public static <T> Expression<T> literal(T value) {
            return new Literal<>(value);
        }

        public static Expression<Object> object(Map<String, Expression<?>> properties) {
            return new ExpressionObject(properties);
        }

        // Boolean Operator Expressions

        public static Expression<Boolean> and(Expression<?>... operands) {
            return new OperatorExpression<>("$and", operands);
        }

        public static Expression<Boolean> not(Expression<?> operand) {
            return new OperatorExpression<>("$not", operand);
        }

        public static Expression<Boolean> or(Expression<?>... operands) {
            return new OperatorExpression<>("$or", operands);
        }

        // Set Operator Expressions

        public static Expression<Boolean> allElementsTrue(Expression<List<?>> set) {
            return new OperatorExpression<>("$allElementsTrue", set);
        }

        public static Expression<Boolean> anyElementTrue(Expression<List<?>> set) {
            return new OperatorExpression<>("$anyElementTrue", set);
        }

        public static Expression<List<?>> setDifference(Expression<List<?>> set1, Expression<List<?>> set2) {
            return new OperatorExpression<>("$setDifference", set1, set2);
        }

        public static Expression<Boolean> setEquals(Expression<List<?>>... sets) {
            return new OperatorExpression<>("$setEquals", sets);
        }

        public static Expression<List<?>> setIntersection(Expression<List<?>>... sets) {
            return new OperatorExpression<>("$setIntersection", sets);
        }

        public static Expression<Boolean> setIsSubset(Expression<List<?>> set1, Expression<List<?>> set2) {
            return new OperatorExpression<>("$setIsSubset", set1, set2);
        }

        public static Expression<List<?>> setUnion(Expression<List<?>>... sets) {
            return new OperatorExpression<>("$setUnion", sets);
        }

        // Comparison Operator Expressions

        public static Expression<Integer> compareTo(Expression<?> value1, Expression<?> value2) {
            return new OperatorExpression<>("$cmp", value1, value2);
        }

        public static Expression<Boolean> equals(Expression<?> value1, Expression<?> value2) {
            return new OperatorExpression<>("$eq", value1, value2);
        }

        public static Expression<Boolean> greaterThan(Expression<?> value1, Expression<?> value2) {
            return new OperatorExpression<>("$gt", value1, value2);
        }

        public static Expression<Boolean> greaterThanOrEquals(Expression<?> value1, Expression<?> value2) {
            return new OperatorExpression<>("$gte", value1, value2);
        }

        public static Expression<Boolean> lessThan(Expression<?> value1, Expression<?> value2) {
            return new OperatorExpression<>("$lt", value1, value2);
        }

        public static Expression<Boolean> lessThanOrEquals(Expression<?> value1, Expression<?> value2) {
            return new OperatorExpression<>("$lte", value1, value2);
        }

        public static Expression<Boolean> notEquals(Expression<?> value1, Expression<?> value2) {
            return new OperatorExpression<>("$ne", value1, value2);
        }

        // Arithmetic Operator Expressions

        public static Expression<Number> add(Expression<Number>... numbers) {
            return new OperatorExpression<>("$add", numbers);
        }

        public static Expression<Number> divide(Expression<Number> number1, Expression<Number> number2) {
            return new OperatorExpression<>("$divide", number1, number2);
        }

        public static Expression<Number> mod(Expression<Number> number1, Expression<Number> number2) {
            return new OperatorExpression<>("$mod", number1, number2);
        }

        public static Expression<Number> multiply(Expression<Number>... numbers) {
            return new OperatorExpression<>("$multiply", numbers);
        }

        public static Expression<Number> subtract(Expression<Number> number1, Expression<Number> number2) {
            return new OperatorExpression<>("$subtract", number1, number2);
        }

        // String Operator Expressions

        public static Expression<String> concat(Expression<String>... strings) {
            return new OperatorExpression<>("$concat", strings);
        }

        public static Expression<Integer> compareToIgnoreCase(Expression<String> string1, Expression<String> string2) {
            return new OperatorExpression<>("$strcasecmp", string1, string2);
        }

        public static Expression<String> substring(Expression<String> string, Expression<Integer> start, Expression<Integer> length) {
            return new OperatorExpression<>("$substr", string, start, length);
        }

        public static Expression<String> toLowerCase(Expression<String> string) {
            return new OperatorExpression<>("$toLower", string);
        }

        public static Expression<String> toUpperCase(Expression<String> string) {
            return new OperatorExpression<>("$toUpper", string);
        }

        // Array Operator Expressions

        public static Expression<Integer> size(Expression<List<?>> array) {
            return new OperatorExpression<>("$size", array);
        }

        public static <T> Expression<T> arrayElemAt(Expression<List<?>> expression, Expression<Integer> index) {
            return new OperatorExpression<>("$arrayElemAt", expression, index);
        }

        // Date Operator Expressions

        public static Expression<Integer> dayOfMonth(Expression<Date> date) {
            return new OperatorExpression<>("$dayOfMonth", date);
        }

        public static Expression<Integer> dayOfWeek(Expression<Date> date) {
            return new OperatorExpression<>("$dayOfWeek", date);
        }

        public static Expression<Integer> hour(Expression<Date> date) {
            return new OperatorExpression<>("$hour", date);
        }

        public static Expression<Integer> millisecond(Expression<Date> date) {
            return new OperatorExpression<>("$millisecond", date);
        }

        public static Expression<Integer> minute(Expression<Date> date) {
            return new OperatorExpression<>("$minute", date);
        }

        public static Expression<Integer> month(Expression<Date> date) {
            return new OperatorExpression<>("$month", date);
        }

        public static Expression<Integer> second(Expression<Date> date) {
            return new OperatorExpression<>("$second", date);
        }

        public static Expression<Integer> week(Expression<Date> date) {
            return new OperatorExpression<>("$week", date);
        }

        public static Expression<Integer> year(Expression<Date> date) {
            return new OperatorExpression<>("$year", date);
        }

        // Conditional Operator Expressions

        public static <T> Expression<T> cond(
            Expression<Boolean> condition,
            Expression<? extends T> consequent, Expression<? extends T> alternative
        ) {
            return new OperatorExpression<>("$cond", condition, consequent, alternative);
        }

        public static <T> Expression<T> ifNull(Expression<? extends T> expression, Expression<? extends T> replacement) {
            return new OperatorExpression<>("$ifNull", expression, replacement);
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
