package me.exrates.externalservice.exceptions.notfound;

import me.exrates.externalservice.exceptions.ServiceException;

public abstract class NotFoundException extends ServiceException {

    public NotFoundException() {
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }
}