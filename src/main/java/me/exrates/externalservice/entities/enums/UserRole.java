package me.exrates.externalservice.entities.enums;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum UserRole {

    ADMINISTRATOR("ROLE_ADMINISTRATOR", 1),
    ACCOUNTANT("ROLE_ACCOUNTANT", 2),
    ADMIN_USER("ROLE_ADMIN_USER", 3),
    USER("ROLE_USER", 4),
    EXCHANGE("ROLE_EXCHANGE", 5),
    VIP_USER("ROLE_VIP_USER", 6),
    TRADER("ROLE_TRADER", 7),
    FIN_OPERATOR("ROLE_FIN_OPERATOR", 8),
    ICO_MARKET_MAKER("ROLE_ICO_MARKET_MAKER", 9);

    private final String authority;
    private final int role;

    UserRole(String authority, int role) {
        this.authority = authority;
        this.role = role;
    }

    public static UserRole of(int role) {
        return Stream.of(values())
                .filter(value -> value.role == role)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User role not found"));
    }
}