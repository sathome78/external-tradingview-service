package me.exrates.externalservice.entities.enums;

import lombok.Getter;

@Getter
public enum EmailType {

    AUTHORIZATION("authorization.ftl", "Two Factor Authorization Code"),
    VERIFICATION("verification.ftl", "Successful Registration");

    private final String template;
    private final String title;

    EmailType(String template, String title) {
        this.template = template;
        this.title = title;
    }
}