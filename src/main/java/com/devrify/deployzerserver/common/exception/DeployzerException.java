package com.devrify.deployzerserver.common.exception;

public class DeployzerException extends Exception {
    // Parameterless Constructor
    public DeployzerException() {
    }

    // Constructor that accepts a message
    public DeployzerException(String message) {
        super(message);
    }

    public DeployzerException(String message, Exception e) {
        super(message, e);
    }
}
