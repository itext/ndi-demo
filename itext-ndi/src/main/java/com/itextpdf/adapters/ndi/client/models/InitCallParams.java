package com.itextpdf.adapters.ndi.client.models;

import com.itextpdf.adapters.ndi.signing.models.Type;

public class InitCallParams {

    /**
     * The id_token of the authenticated NDI user
     */
    private final String userNdiId;

    private final String nonce;

    private final Type type;

    public InitCallParams(String userNdiId,
                          String nonce,
                          Type type) {
        this.userNdiId = userNdiId;
        this.nonce = nonce;
        this.type = type;
    }


    public String getUserNdiId() {
        return userNdiId;
    }

    public String getNonce() {
        return nonce;
    }
}
