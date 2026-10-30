package com.assurauto.contrat.application.exception;

/** Conflit d'unicité (HTTP 409). */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
