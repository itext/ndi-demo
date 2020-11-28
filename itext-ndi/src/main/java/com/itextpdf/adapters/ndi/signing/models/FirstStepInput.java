package com.itextpdf.adapters.ndi.signing.models;

public class FirstStepInput {


    private String fieldName;

    private byte[] source;

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setSource(byte[] source) {
        this.source = source;
    }

    public String getFieldName() {
        return fieldName;
    }

    public byte[] getSource() {
        return source;
    }
}
