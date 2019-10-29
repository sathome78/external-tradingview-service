package me.exrates.externalservice.exceptions;

public class VerificationException extends ServiceException {

    public VerificationException() {
    }

    public VerificationException(String message) {
        super(message);
    }

    public VerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}