package com.itextpdf.adapters.ndi.client.models.callback;

/**
 * Parent for  a NDi callback payload
 * Each callback payload must contain both 'nonce' and 'signRef' parameters
 *
 */
public abstract class NdiCallbackMessage {

    private String signRef;

    private String nonce;


    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getSignRef() {
        return signRef;
    }

    public void setSignRef(String signRef) {
        this.signRef = signRef;
    }
}
