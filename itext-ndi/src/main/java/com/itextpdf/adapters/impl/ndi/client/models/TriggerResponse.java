package com.itextpdf.adapters.impl.ndi.client.models;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * {
 * "expires_in": 0,
 * "sign_ref": "string",
 * "nonce": "string"
 * }
 */
public class TriggerResponse {

    /** Unix timestamp in seconds that indicates when the Document Signing Session expires */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /** A unique identifier of the Document Signing Session in the format of a UUIDv4. */
    @JsonProperty("sign_ref")
    private String signRef;

    /** The same as in {@see PNRtiggerRequest} */
    private String nonce;

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getSignRef() {
        return signRef;
    }

    public void setSignRef(String signRef) {
        this.signRef = signRef;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return "PNTriggerResponse{" +
                "expiresIn=" + expiresIn +
                ", signRef='" + signRef + '\'' +
                ", nonce='" + nonce + '\'' +
                '}';
    }
}
