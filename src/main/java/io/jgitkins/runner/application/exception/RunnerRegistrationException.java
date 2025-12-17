package io.jgitkins.runner.application.exception;

import org.springframework.http.HttpStatus;

/**
 * Signals that runner activation/registration with the server failed.
 */
public class RunnerRegistrationException extends RuntimeException {

    private final HttpStatus status;

    public RunnerRegistrationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public RunnerRegistrationException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
