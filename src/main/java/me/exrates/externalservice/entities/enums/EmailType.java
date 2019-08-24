package me.exrates.externalservice.entities.enums;

import lombok.Getter;

@Getter
public enum EmailType {

    AUTHORIZATION_2FA_CODE("authorization.ftl", "Two Factor Authorization Code");

    private final String template;
    private final String title;

    EmailType(String template, String title) {
        this.template = template;
        this.title = title;
    }
}