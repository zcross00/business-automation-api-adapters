package com.redhat.business.automation.adapter.rules.api;

import java.util.Collection;

public class QueryResult {

    private final String queryName;
    private final Collection<Object> results;

    public QueryResult( String queryName, Collection<Object> results ) {
        super();
        this.queryName = queryName;
        this.results = results;
    }

    public String getQueryName() {
        return queryName;
    }

    public Collection<Object> getResults() {
        return results;
    }

}
