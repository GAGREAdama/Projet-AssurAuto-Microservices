package com.assurauto.client.application.exception;

/** Conflit d'unicité (HTTP 409). */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
