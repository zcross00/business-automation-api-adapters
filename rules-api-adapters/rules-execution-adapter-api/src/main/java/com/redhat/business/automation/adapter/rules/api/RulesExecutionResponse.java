package com.redhat.business.automation.adapter.rules.api;

import java.util.HashMap;
import java.util.Map;

public class RulesExecutionResponse {

    private RulesExecutionRequestStatus status;
    private String statusMessage;
    private Map<String, Object> output = new HashMap<String, Object>();

    public RulesExecutionRequestStatus getStatus() {
        return this.status;
    }

    public void setStatus( RulesExecutionRequestStatus status ) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage( String statusMessage ) {
        this.statusMessage = statusMessage;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public void setOutput( Map<String, Object> output ) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "RulesResponse [status=" + status + ", statusMessage=" + statusMessage + ", output=" + output + "]";
    }
}
