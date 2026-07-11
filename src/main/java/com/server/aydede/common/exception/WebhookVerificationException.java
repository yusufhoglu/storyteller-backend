package com.server.aydede.common.exception;

public class WebhookVerificationException extends RuntimeException {
    public WebhookVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}