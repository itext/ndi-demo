package com.itextpdf.adapters.ndi.helper.models;

import org.bouncycastle.util.encoders.Hex;

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

    public static FirstStepOutput createDummyOutput(byte[] content){
        FirstStepOutput fso = new FirstStepOutput();
        fso.setPreparedContent(content);
        fso.setFieldName("Signature1");
        fso.setDigest(Hex.decode("54af74d1a5d85608db2fa19aac06ed77aa2688b5892bac8e97ac31f8702c3a39"));
        return fso;
    }
}
