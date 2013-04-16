package org.mongojack.internal.query;

import org.mongojack.DBQuery;

public class CompoundQueryCondition implements QueryCondition {

    private final DBQuery.Query query;

    public CompoundQueryCondition(DBQuery.Query query) {
        this.query = query;
    }

    public DBQuery.Query getQuery() {
        return query;
    }
}
