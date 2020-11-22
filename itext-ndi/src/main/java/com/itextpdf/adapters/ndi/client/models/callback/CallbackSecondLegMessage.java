package com.itextpdf.adapters.ndi.client.models.callback;

/**
 * Second Leg. Callback message if successful.
 * Contains hash signed by a user key.
 *
 * https://www.ndi-api.gov.sg/assets/lib/trusted-services/ds/specs/hsv2.0.0.yaml.html#tag/Client-Notification-Endpoint
 * {
 * "signature": "string",
 * "sign_ref": "string",
 * "nonce": "string"
 * }
 */
public final class CallbackSecondLegMessage extends NdiCallbackMessage {

    /** Signed hash */
    private String signature;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

}
