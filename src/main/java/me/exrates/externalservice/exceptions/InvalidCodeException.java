package me.exrates.externalservice.exceptions;

public class InvalidCodeException extends ServiceException {

    public InvalidCodeException() {
    }

    public InvalidCodeException(String message) {
        super(message);
    }

    public InvalidCodeException(Throwable cause) {
        super(cause);
    }
}