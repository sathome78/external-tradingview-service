package me.exrates.externalservice.entities.enums;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum UserStatus {

    REGISTERED(1),
    ACTIVATED(2),
    BLOCKED(3);

    private final int status;

    UserStatus(int status) {
        this.status = status;
    }

    public static UserStatus of(int status) {
        return Stream.of(values())
                .filter(value -> value.status == status)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User status not found"));
    }
}