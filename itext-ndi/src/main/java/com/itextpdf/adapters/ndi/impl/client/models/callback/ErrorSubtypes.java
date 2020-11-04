package com.itextpdf.adapters.ndi.impl.client.models.callback;

import java.util.Arrays;

public enum ErrorSubtypes {
    USER_CANCELLED("user_cancelled"),
    AUTH_FAILED("auth_failed"),
    UNRECOGNIZED("unrecognized");

    private final String type;

    ErrorSubtypes(String type) {
        this.type = type;
    }

    public static ErrorSubtypes findByValue(String value) {
        return Arrays.stream(ErrorSubtypes.values())
                     .filter(a -> a.type.equals(value))
                     .findFirst()
                     .orElse(UNRECOGNIZED);
    }

    public String getType() {
        return type;
    }
}
