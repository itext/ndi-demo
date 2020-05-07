package com.itextpdf.adapters.ndi.signing;

import com.itextpdf.adapters.ndi.client.models.ContainerError;
import com.itextpdf.file.models.PdfFile;

import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.util.List;

public class NDIDocument {

    private final String signatureRef;

    private final byte[] source;

    private final String docName;

    private final String ndiHint;

    private String qrCodeData;

    private String fieldName;

    private LocalDateTime expiresAt;

    private Integer challengeCode;

    private byte[] preparedContent;

    private byte[] hash;

    private byte[] result;

    private Certificate[] certificates;

    private List<byte[]> oscp;

    private ContainerError error;



    public NDIDocument(String signatureRef, byte[] source, String docName, String fieldName, String userId,
                       LocalDateTime expiresAt) {
        this.signatureRef = signatureRef;
        this.source = source;
        this.docName = docName;
        this.fieldName = fieldName;
        this.ndiHint = userId;
        this.expiresAt = expiresAt;
    }


    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Certificate[] getCertificates() {
        return certificates;
    }

    public void setCertificates(Certificate[] certificates) {
        this.certificates = certificates;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }


    public List<byte[]> getOscp() {
        return oscp;
    }

    public void setOscp(List<byte[]> oscp) {
        this.oscp = oscp;
    }

    public byte[] getPreparedContent() {
        return preparedContent;
    }

    public void setPreparedContent(byte[] preparedContent) {
        this.preparedContent = preparedContent;
    }


    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
    }

    public byte[] getSource() {
        return source;
    }


    public String getDocName() {
        return docName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getNdiHint() {
        return ndiHint;
    }

    public String getSignatureRef() {
        return signatureRef;
    }

    public Integer getChallengeCode() {
        return challengeCode;
    }

    public void setChallengeCode(Integer challengeCode) {
        this.challengeCode = challengeCode;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public ContainerError getError() {
        return error;
    }

    public void setError(ContainerError error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "NDIDocument{" +
                "signatureRef='" + signatureRef + '\'' +
                ", source=" + source.length +
                ", preparedContent=" + preparedContent.length +
                ", result=" + result.length +
                ", docName='" + docName + '\'' +
                ", ndiHint='" + ndiHint + '\'' +
                ", qrCodeData='" + qrCodeData + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", expiresAt=" + expiresAt +
                ", challengeCode='" + challengeCode + '\'' +
                ", hash length=" + hash.length +
                ", certificate chain  =" + certificates.length +
                ", error=" + error +
                '}';
    }


}

