package me.exrates.externalservice.entities.enums;

import lombok.Getter;

@Getter
public enum ResStatus {

    OK("ok"), ERROR("error"), NO_DATA("no_data");

    private String status;

    ResStatus(String status) {
        this.status = status;
    }
}