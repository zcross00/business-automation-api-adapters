package com.redhat.business.automation.adapter.decisionserver.common;

public class DecisionServerClientException extends Exception {

    private static final long serialVersionUID = 1L;

    public DecisionServerClientException( String message, Throwable cause ) {
        super( message, cause );
    }

    public DecisionServerClientException( String message ) {
        super( message );
    }

}