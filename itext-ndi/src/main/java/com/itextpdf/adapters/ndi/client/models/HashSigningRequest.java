package com.itextpdf.adapters.ndi.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for the hash signing request (HSS second leg).
 *
 * https://www.ndi-api.gov.sg/assets/lib/trusted-services/ds/specs/hsv2.0.0.yaml.html#operation/hashSigningService
 * v2.0
 * {
 * "doc_name": "string",
 * "doc_hash": "string",
 * "challenge_code": 0,
 * "sign_ref": "string",
 * "nonce": "string"
 * }
 */
public class HashSigningRequest {

    /** The name of the document that the user is signing on. */
    private String docName;

    /** The hash of the document`s attributes bytes*/
    private String docHash;

    /** MAX 6 Digit challenge code meant for display on the user's form factor for verification.*/
    //    for now, we can not use a challenge code with  leading zeros
    private Integer challengeCode;

    /**
     * The identifier of the Document Signing Session.
     * This is only applicable if the first-leg of the PN has been executed previously.
     */
    private String signRef;

    /**
     * A client-side nonce generated which cannot be predicted, to ensure the challenge is always unique and not
     * subjected to replay attacks. This is to be generated using methods compliant with NIST SP 800-90A Rev 1,
     * specifically with SHA-256. This value shall be echo-ed back to the DSP/RP via the client notification
     * endpoint.
     */
    private String nonce;

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getDocHash() {
        return docHash;
    }

    public void setDocHash(String docHash) {
        this.docHash = docHash;
    }

    public Integer getChallengeCode() {
        return challengeCode;
    }

    public void setChallengeCode(Integer challengeCode) {
        this.challengeCode = challengeCode;
    }

    public String getSignRef() {
        return signRef;
    }

    public void setSignRef(String signRef) {
        this.signRef = signRef;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}
