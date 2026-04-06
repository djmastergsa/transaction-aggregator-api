package com.capitec.aggregator.exception;

/**
 * Thrown when a sync is requested while one is already running.
 * Maps to HTTP 409 Conflict in GlobalExceptionHandler.
 */
public class SyncInProgressException extends RuntimeException {

    public SyncInProgressException(String message) {
        super(message);
    }
}
