package com.itextpdf.adapters.ndi.client.models;

public class DILoginParams {

    /** the Id of NDi user to be authorized */
    private final String ndiId;

    private final String nonce;

    /** the custom message to be displayed on the user device */
    private final String message;

    public DILoginParams(String ndiId, String nonce, String message) {
        this.ndiId = ndiId;
        this.nonce = nonce;
        this.message = message;
    }

    public String getNdiId() {
        return ndiId;
    }

    public String getNonce() {
        return nonce;
    }

    public String getMessage() {
        return message;
    }
}
