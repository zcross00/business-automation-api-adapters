package com.redhat.business.automation.adapter.embedded.common;

public class EmbeddedKieScannerException extends Exception {

    private static final long serialVersionUID = 1L;

    public EmbeddedKieScannerException( String message, Throwable cause ) {
        super( message, cause );
    }

    public EmbeddedKieScannerException( String message ) {
        super( message );
    }

}