package com.itextpdf.demo.ndi.sign.models.output;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
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
