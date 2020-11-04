package com.itextpdf.adapters.ndi.impl.client.models.callback;

/**
 * First Leg. Callback message if successful.
 *
 * Contains the user certificate.
 *
 * * https://www.ndi-api.gov.sg/assets/lib/trusted-services/ds/specs/hsv2.0.0.yaml.html#tag/Client-Notification-Endpoint
 * {
 * "usr_cert": "string",
 * "sign_ref": "string",
 * "nonce": "string"
 * }
 */
public final class CallbackFirstLegMessage extends NdiCallbackMessage {

    private String usrCert;

    public String getUsrCert() {
        return usrCert;
    }

    public void setUsrCert(String usrCert) {
        this.usrCert = usrCert;
    }

}
