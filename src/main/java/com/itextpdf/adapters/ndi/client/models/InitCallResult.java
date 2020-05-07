package com.itextpdf.adapters.ndi.client.models;


import java.time.LocalDateTime;

public class InitCallResult {

    /** The identifier of the Document Signing Session in the format of a UUIDv4.      */
    private String signRef;

    private LocalDateTime expiresAt;

    private String qrCodeData;

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

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }
}
