package me.exrates.externalservice.exceptions.api;

public class ExratesApiException extends RuntimeException {

    public ExratesApiException() {
        super();
    }

    public ExratesApiException(String message) {
        super(message);
    }

    public ExratesApiException(String message, Throwable cause) {
        super(message, cause);
    }
}