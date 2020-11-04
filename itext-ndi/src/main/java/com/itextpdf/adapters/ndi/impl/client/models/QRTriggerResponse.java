package com.itextpdf.adapters.ndi.impl.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * First leg.
 * Response when Qr Authentication Endpoint is being used.
 *
 * https://www.ndi-api.gov.sg/assets/lib/trusted-services/ds/specs/hsv2.0.0.yaml.html#tag/QR-Authentication-Endpoint
 * {
 * "expires_in": 0,
 * "sign_ref": "string",
 * "nonce": "string"
 * }
 */
public class QRTriggerResponse {

    /** Unix timestamp in seconds that indicates when the Document Signing Session expires */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /** The identifier of the Document Signing Session in the format of a UUIDv4.  Should be QR-encoded. */
    @JsonProperty("sign_ref")
    private String signRef;

    /** The same as in request {@see PNRtiggerRequest#nonce} */
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

}
