package com.itextpdf.adapters.ndi.client.models;


/**
 * A signing process initialization (DSS first leg).
 * Payload for QR-Authentication-Endpoint request
 * <p>
 * https://stg-id.singpass.gov.sg/docs/doc-signing#_post_doc_signing_sessions
 * {
 * "client_notification_token": "string",
 * }
 */

public class SignInitRequest {

    /**
     * A token used by NDI to invoke the DSAP’s webhook endpoint.
     */
    private final String clientNotificationToken;

    public SignInitRequest(String clientNotificationToken) {
        this.clientNotificationToken = clientNotificationToken;
    }

    public String getClientNotificationToken() {
        return clientNotificationToken;
    }
}
