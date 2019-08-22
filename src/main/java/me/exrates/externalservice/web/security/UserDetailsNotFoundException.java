package me.exrates.externalservice.web.security;

class UserDetailsNotFoundException extends Exception {

    UserDetailsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}