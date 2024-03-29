package com.itextpdf.adapters.ndi.signing;

import com.itextpdf.adapters.ndi.signing.models.ContainerError;
import com.itextpdf.adapters.ndi.signing.models.SigningStatus;
import com.itextpdf.adapters.ndi.signing.models.Type;

import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A parameter object for the ndi signing process
 */
public class NDIDocument {

    /**
     * Unique identifier of the document
     */
    private final String signatureRef;

    /**
     * Source document content
     */
    private final byte[] source;

    /**
     * Source document name
     */
    private final String docName;

    private final String userName;

    /**
     * the Base64 encoded Qr code  to be displayed. Null for the document of the PN {@link Type}
     */
    private String qrCode;

    /**
     * the signature field in the source document, that is being used  for the signature placement
     */
    private String fieldName;

    /**
     * deadline for the signing
     */
    private LocalDateTime expiresAt;

    /**
     * challenge code generated before the 2nd leg call
     */
    private Integer challengeCode;

    /**
     * The digest of the source document
     */
    private byte[] hash;

    /**
     * The certificate chain for the NDI user.
     * According to NDI guidelines a signing software shouldn`t store user certificates!
     */
    private Certificate[] certificatesChain;

    /**
     * OSCP responses for the certificateChain
     */
    private List<byte[]> ocsp;

    /**
     * The content that is ready for the placement of a PKCS#7 container
     */
    private byte[] preparedContent;

    /**
     * The content of the signed document
     */
    private byte[] result;

    private ContainerError error;

    private SigningStatus status;

    public NDIDocument(String signatureRef, byte[] source, String docName, String fieldName, String userId,
                       LocalDateTime expiresAt) {
        this.signatureRef = signatureRef;
        this.source = source;
        this.docName = docName;
        this.fieldName = fieldName;
        this.userName = userId;
        this.expiresAt = expiresAt;
    }



    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Certificate[] getCertificatesChain() {
        return certificatesChain;
    }

    void setCertificatesChain(Certificate[] certificatesChain) {
        this.certificatesChain = certificatesChain;
    }

    public byte[] getHash() {
        return hash;
    }

    void setHash(byte[] hash) {
        this.hash = hash;
    }


    public List<byte[]> getOcsp() {
        return ocsp;
    }

    void setOcsp(List<byte[]> ocsp) {
        this.ocsp = ocsp;
    }

    public byte[] getPreparedContent() {
        return preparedContent;
    }

    void setPreparedContent(byte[] preparedContent) {
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

    void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getUserName() {
        return userName;
    }

    public String getSignatureRef() {
        return signatureRef;
    }

    public Integer getChallengeCode() {
        return challengeCode;
    }

    void setChallengeCode(Integer challengeCode) {
        this.challengeCode = challengeCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public ContainerError getError() {
        return error;
    }

    void setError(ContainerError error) {
        this.error = error;
    }

    public SigningStatus getStatus() {
        return status;
    }

    void setStatus(SigningStatus status) {
        this.status = status;
    }

}

