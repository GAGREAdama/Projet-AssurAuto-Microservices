package com.assurauto.sinistre.domain.exception;

/** Règle métier violée (HTTP 422). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
