package com.itextpdf.adapters.ndi.client.models;

/**
 * Wrapper for qrCode info. Part of SignInitResponse
 * {
 *  * "logo":"https://id.singpass.gov.sg/static/public/images/singpass_logo.svg",
 *  * "payload":"https://singpassmobile.sg/docsign?sign_ref=7d6c67e6-b803-42a2-8dce-ecd93263d759"}
 *  * }
 */
public class NdiQRCode {

    private String payload;

    private String logo;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
