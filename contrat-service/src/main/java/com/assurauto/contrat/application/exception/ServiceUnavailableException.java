package com.assurauto.contrat.application.exception;

/** Un microservice distant est injoignable (HTTP 503). */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
