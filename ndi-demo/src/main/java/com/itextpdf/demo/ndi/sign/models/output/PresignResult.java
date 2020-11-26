package com.itextpdf.demo.ndi.sign.models.output;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class PresignResult {

    private String challengeCode;

    private String signRef;

    public PresignResult(String signRef, String challengeCode) {
        this.challengeCode = challengeCode;
        this.signRef = signRef;
    }

    public String getChallengeCode() {
        return challengeCode;
    }

    public void setChallengeCode(String challengeCode) {
        this.challengeCode = challengeCode;
    }
}
