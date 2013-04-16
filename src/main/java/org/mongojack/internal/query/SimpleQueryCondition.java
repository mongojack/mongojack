package org.mongojack.internal.query;

public class SimpleQueryCondition implements QueryCondition {

    private final Object value;
    private final boolean requiresSerialization;

    public SimpleQueryCondition(Object value) {
        this.value = value;
        this.requiresSerialization = true;
    }

    public SimpleQueryCondition(Object value, boolean requiresSerialization) {
        this.value = value;
        this.requiresSerialization = requiresSerialization;
    }

    public boolean requiresSerialization() {
        return requiresSerialization;
    }

    public Object getValue() {
        return value;
    }
}
