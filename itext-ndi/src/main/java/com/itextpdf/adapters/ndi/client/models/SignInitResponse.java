package com.itextpdf.adapters.ndi.client.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * A signing process initialization. Response.
 * <p>
 * <p>
 * https://stg-id.singpass.gov.sg/docs/doc-signing#_request_and_response_structure_response_body
 * {"sign_ref":"7d6c67e6-b803-42a2-8dce-ecd93263d759",
 * "expires_at":1609907001,
 * "qr_code":{
 * "logo":"https://id.singpass.gov.sg/static/public/images/singpass_logo.svg",
 * "payload":"https://singpassmobile.sg/docsign?sign_ref=7d6c67e6-b803-42a2-8dce-ecd93263d759"}
 * }
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SignInitResponse {

    /**
     * Unix timestamp in seconds that indicates when the Document Signing Session expires
     */
    private Long expiresAt;

    /**
     * The identifier of the Document Signing Session in the format of a UUIDv4.  Should be QR-encoded.
     */
    private String signRef;

    private NdiQRCode qrCode;

    public NdiQRCode getQrCode() {
        return qrCode;
    }

    public void setQrCode(NdiQRCode qrCode) {
        this.qrCode = qrCode;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getSignRef() {
        return signRef;
    }

    public void setSignRef(String signRef) {
        this.signRef = signRef;
    }

}
