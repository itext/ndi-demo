package com.itextpdf.adapters.ndi.impl.signing.services.models;

import java.time.LocalDateTime;

public class ExpectedCallback {

    private String nonce;

    private String signRef;

    private LocalDateTime expiredAt;

    protected ExpectedCallback() {
    }

    public ExpectedCallback(String nonce, String signRef, LocalDateTime expiredAt) {
        this.nonce = nonce;
        this.signRef = signRef;
        this.expiredAt = expiredAt;
    }

    public String getNonce() {
        return nonce;
    }

    protected void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getSignRef() {
        return signRef;
    }

    protected void setSignRef(String signRef) {
        this.signRef = signRef;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }
}
