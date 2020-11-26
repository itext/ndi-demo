package com.itextpdf.adapters.ndi.helper.models;

public class FirstStepOutput {

    private String fieldName;

    private byte[] digest;

    private byte[] preparedContent;

    public void setDigest(byte[] digest) {
        this.digest =digest;
    }

    public void setPreparedContent(byte[] preparedContent) {
        this.preparedContent = preparedContent;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public byte[] getDigest() {
        return digest;
    }

    public byte[] getPreparedContent() {
        return preparedContent;
    }
}
