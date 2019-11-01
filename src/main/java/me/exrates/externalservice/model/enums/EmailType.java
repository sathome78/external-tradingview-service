package me.exrates.externalservice.model.enums;

import lombok.Getter;

@Getter
public enum EmailType {

    VERIFICATION("verification.ftl", "Successful Registration");

    private final String template;
    private final String title;

    EmailType(String template, String title) {
        this.template = template;
        this.title = title;
    }
}