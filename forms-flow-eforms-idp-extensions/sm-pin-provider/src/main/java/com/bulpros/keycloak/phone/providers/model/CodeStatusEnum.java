package com.bulpros.keycloak.phone.providers.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CodeStatusEnum {
    WAITING("waiting"), //
    CANCELLED("cancelled"), //
    CONFIRMED("confirmed"), //
    NONE("none"); //

    private final String status;

    public static CodeStatusEnum getByWaitingStatusName(String value) {
        return Arrays.stream(CodeStatusEnum.values()).filter(e -> e.status.equalsIgnoreCase(value)).findAny()
                .orElse(null);
    }

    CodeStatusEnum(String waitingStatusName) {
        this.status = waitingStatusName;
    }
}
