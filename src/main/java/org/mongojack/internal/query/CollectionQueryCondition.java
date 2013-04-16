package org.mongojack.internal.query;

import java.util.ArrayList;
import java.util.Collection;

public class CollectionQueryCondition implements QueryCondition {
    private final Collection<QueryCondition> values;
    private final boolean targetIsCollection;

    public CollectionQueryCondition(Collection<QueryCondition> values, boolean targetIsCollection) {
        this.values = values;
        this.targetIsCollection = targetIsCollection;
    }

    public CollectionQueryCondition() {
        values = new ArrayList<QueryCondition>();
        targetIsCollection = false;
    }

    public Collection<QueryCondition> getValues() {
        return values;
    }

    public boolean targetIsCollection() {
        return targetIsCollection;
    }

    public void add(QueryCondition value) {
        values.add(value);
    }

    public void addAll(Collection<QueryCondition> values) {
        this.values.addAll(values);
    }
}
