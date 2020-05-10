package com.itextpdf.adapters.ndi.signing.models;

public class InitializationResult {

    private final String signRef;

    private final String qrCode;


    public InitializationResult(String signRef, String qrCode) {
        this.signRef = signRef;
        this.qrCode = qrCode;
    }

    public String getSignRef() {
        return signRef;
    }


    public String getQrCode() {
        return qrCode;
    }

}
