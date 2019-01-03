package com.redhat.business.automation.adapter.rules.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RulesExecutionResponse {

    private RulesExecutionRequestStatus status;
    private String message;
    private Map<String, Collection<Object>> output = new HashMap<>();

    public Collection<Object> getQueryOutput( String queryName ) {
        return this.output.get( queryName );
    }

    public RulesExecutionRequestStatus getStatus() {
        return this.status;
    }

    public void setStatus( RulesExecutionRequestStatus status ) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage( String message ) {
        this.message = message;
    }

    public Map<String, Collection<Object>> getOutput() {
        return output;
    }

    public void setOutput( Map<String, Collection<Object>> output ) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "RulesResponse [status=" + status + ", statusMessage=" + message + ", output=" + output + "]";
    }
}