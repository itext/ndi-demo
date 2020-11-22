package com.itextpdf.adapters.ndi.impl.client.models;


import java.time.LocalDateTime;

public class InitCallResult {

    /**
     * The identifier of the Document Signing Session in the format of a UUIDv4.
     */
    private String signRef;

    private LocalDateTime expiresAt;

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getSignRef() {
        return signRef;
    }

    public void setSignRef(String signRef) {
        this.signRef = signRef;
    }

}
