package com.bank.migration.exception;

public class MigrationApiException extends RuntimeException {
    
    public MigrationApiException(String message) {
        super(message);
    }
    
    public MigrationApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

