package com.itextpdf.container.models;

import java.security.cert.Certificate;

public class NDISessionUser {

    /** ndi id*/
    private String ndiId;

    private String firstName;

    private String lastName;

    /** token hint -  id of receiving ndi token */
    private String tokenHint;

    /** certificate chain for user */
    private Certificate[] certificates;


    public String getNdiId() {
        return ndiId;
    }

    public void setNdiId(String ndiId) {
        this.ndiId = ndiId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Certificate[] getCertificates() {
        return certificates;
    }

    public void setCertificates(Certificate[] certificates) {
        this.certificates = certificates;
    }

    public String getTokenHint() {
        return tokenHint;
    }

    public void setTokenHint(String tokenHint) {
        this.tokenHint = tokenHint;
    }
}
