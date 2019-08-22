package me.exrates.externalservice.exceptions.conflict;

import me.exrates.externalservice.exceptions.ServiceException;

public abstract class EntityExistException extends ServiceException {

    public EntityExistException() {
    }

    public EntityExistException(String message) {
        super(message);
    }

    public EntityExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityExistException(Throwable cause) {
        super(cause);
    }
}