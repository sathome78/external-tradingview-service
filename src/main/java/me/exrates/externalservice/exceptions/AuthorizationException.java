package me.exrates.externalservice.exceptions;

public class AuthorizationException extends ServiceException {

    public AuthorizationException() {
    }

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}