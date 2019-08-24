package me.exrates.externalservice.exceptions;

public class UnsupportedResolutionException extends RuntimeException {

    public UnsupportedResolutionException(String resolution) {
        super(String.format("No such resolution: %s ", resolution));
    }
}