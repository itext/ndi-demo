package com.itextpdf.adapters.impl.ndi.client.models;


/**
 * A signing process initialization (hss first leg).
 * Query params for QR-Authentication-Endpoint request
 * <p>
 * https://www.ndi-api.gov.sg/assets/lib/trusted-services/ds/specs/hsv2.0.0.yaml.html#tag/QR-Authentication-Endpoint
 * {
 * "client_id": "string",
 * "nonce": "string",
 * "client_notification_token": "string",
 * "login_hint": "string",
 * "response_type": "string"
 * }
 */

public class QRTriggerQueryParms {

    /**
     * urn:openid:params:grant-type:ciba
     */
    private final String responseType = "urn:openid:params:grant-type:ciba";

    /**
     * application clientId
     */

    private String clientId;

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

    private String clientNotificationToken;

    /**
     * The id_token of the authenticated NDI user. This id_token can be retrieved by performing an OIDC handshake
     * with NDI's ASP
     */
    private String loginHint;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

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

    public String getLoginHint() {
        return loginHint;
    }

    public void setLoginHint(String loginHint) {
        this.loginHint = loginHint;
    }

    public String getResponseType() {
        return responseType;
    }
}
