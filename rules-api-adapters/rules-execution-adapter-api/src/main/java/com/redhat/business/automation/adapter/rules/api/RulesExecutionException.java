package com.redhat.business.automation.adapter.rules.api;

public class RulesExecutionException extends Exception {

    private static final long serialVersionUID = 5149464006092309308L;

    public RulesExecutionException( String message, Throwable cause ) {
        super( message, cause );
    }

}
