package com.itextpdf.adapters.ndi.helper.models;

import java.security.cert.Certificate;

public class SecondStepInput {

    private final byte[] preparedContent;

    private final String fieldName;

    private final byte[] documentDigest;

    private final Certificate[] certificateChain;

    private final byte[] signedHash;

    public SecondStepInput(byte[] preparedContent, String fieldName, byte[] documentDigest,
                           Certificate[] certificateChain, byte[] signedHash) {
        this.preparedContent = preparedContent;
        this.fieldName = fieldName;
        this.documentDigest = documentDigest;
        this.certificateChain = certificateChain;
        this.signedHash = signedHash;
    }

    public byte[] getPreparedContent() {
        return preparedContent;
    }

    public String getFieldName() {
        return fieldName;
    }

    public byte[] getDocumentDigest() {
        return documentDigest;
    }

    public Certificate[] getCertificateChain() {
        return certificateChain;
    }

    public byte[] getSignedHash() {
        return signedHash;
    }
}
