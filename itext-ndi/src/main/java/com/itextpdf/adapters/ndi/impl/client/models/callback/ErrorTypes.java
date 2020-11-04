package com.itextpdf.adapters.ndi.impl.client.models.callback;

import java.util.Arrays;

public enum ErrorTypes {

    INVALID_REQUEST("invalid_request"),
    INVALID_CLIENT("invalid_client"),
    INVALID_GRANT("invalid_grant"),
    UNAUTHORIZED_CLIENT("unauthorized_client"),
    UNSUPPORTED_GRANT_TYPE("unsupported_grant_type"),
    INVALID_SCOPE("invalid_scope"),
    UNRECOGNIZED_REASON("unrecognized");

    private final String value;

    ErrorTypes(String value) {
        this.value = value;
    }

    public static ErrorTypes findByValue(String value) {
        return Arrays.stream(ErrorTypes.values())
                     .filter(a -> a.value.equals(value))
                     .findFirst()
                     .orElse(UNRECOGNIZED_REASON);
    }

    public String getValue() {
        return value;
    }
}
