package com.itextpdf.adapters.ndi.client.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *  Payload for signing process initialization (hss first leg).
 *  When a push notification is being used.
 *
 * https://www.ndi-api.gov.sg/assets/lib/trusted-services/ds/specs/hsv2.0.0.yaml.html#tag/PN-Trigger-Endpoint
 * {
 * "nonce": "string",
 * "client_notification_token": "string",
 * "id_token_hint": "string",
 * "response_type": "string"
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PNTriggerRequest {

    /**
     * A client-side nonce generated which cannot be predicted, to ensure the challenge is always unique and not
     * subjected to replay attacks. This is to be generated using methods compliant with NIST SP 800-90A Rev 1,
     * specifically with SHA-256. This value shall be echo-ed back to the DSP/RP via the client notification
     * endpoint.
     */
    private String nonce;

    /**
     * it is a bearer token provided by the Client that will be used by the OpenID Provider to authenticate the
     * notificqtion request to the Client. The length of the token MUST NOT exceed 1024 characters and it MUST
     * conform to the syntax for Bearer credentials as defined in Section 2.1 of [RFC6750]. Clients MUST ensure that
     * it contains sufficient entropy (a minimum of 128 bits while 160 bits is recommended) to make brute force
     * guessing or forgery of a valid token computationally infeasible - the means of achieving this are
     * implementation specific, with possible approaches including secure pseudorandom number generation or
     * cryptographically secured self-contained tokens
     */
    @JsonProperty("client_notification_token")
    private String clientNotificationToken;

    /**
     * The id_token of the authenticated NDI user. This id_token can be retrieved by performing an OIDC handshake
     * with NDI's ASP
     */
    @JsonProperty("id_token_hint")
    private String idTokenHint;

    /**
     * urn:openid:params:grant-type:ciba
     */
    @JsonProperty("response_type")
    private final String responseType = "urn:openid:params:grant-type:ciba";


    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getClientNotificationToken() {
        return clientNotificationToken;
    }

    public void setClientNotificationToken(String clientNotificationToken) {
        this.clientNotificationToken = clientNotificationToken;
    }

    public String getIdTokenHint() {
        return idTokenHint;
    }

    public void setIdTokenHint(String idTokenHint) {
        this.idTokenHint = idTokenHint;
    }

    public String getResponseType() {
        return responseType;
    }

}
