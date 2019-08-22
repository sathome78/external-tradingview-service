package me.exrates.externalservice.exceptions.conflict;

public class EmailExistException extends EntityExistException {

    public EmailExistException() {
        super();
    }

    public EmailExistException(String message) {
        super(message);
    }

    public EmailExistException(String message, Throwable cause) {
        super(message, cause);
    }
}