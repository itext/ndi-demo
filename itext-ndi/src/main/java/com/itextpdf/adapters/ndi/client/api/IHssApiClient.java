package com.itextpdf.adapters.ndi.client.api;

import com.itextpdf.adapters.ndi.client.models.HashSigningRequest;
import com.itextpdf.adapters.ndi.client.models.InitCallParams;
import com.itextpdf.adapters.ndi.client.models.InitCallResult;

import java.util.concurrent.CompletionStage;

/** Interface caller  for NDI hss api.**/

public interface IHssApiClient {

    String HSS_DOMAIN = "https://api.sandbox.ndi.gov.sg/api/v1/hss/signatures";


    /**
     * HSS PN Trigger Endpoint. The client shall invoke this endpoint to retrieve a signature reference ID that shall
     * be kept as reference for the span of the digital-signing session. This endpoint is the start of the
     * Client-Intitated Backchannel Authetication (CIBA) Push Mode flow. The NDI mobile App returns the public
     * certificate of the user from the mobile soft token (via the client's callback uri). The client may then
     * optionally proceed with the creation of a PAdES document hash, which may require OCSP responses and timestamp
     * tokens.
     */
    String PN_TRIGGER_ENDPOINT = HSS_DOMAIN + "/push-notification/consent";

    /**
     * HSS QR Authentication Endpoint. The client shall invoke this endpoint to retrieve a signature reference ID
     * that shall be displayed as a QR-encoded image. This authentication endpoint is the start of the
     * Client-Intitated Backchannel Authetication (CIBA) Push Mode flow. The NDI mobile App returns the public
     * certificate of the user from the mobile soft token (via the client's callback uri). The client may then
     * optionally proceed with the creation of a PAdES document hash, which may require OCSP responses and timestamp
     * tokens.
     */
    String QR_AUTH_ENDPOINT = HSS_DOMAIN + "/sign-ref";
    /**
     * This is the endpoint called by RP/DSP to trigger notification on the user's form-factor to solicit user
     * consent for signing on a document hash. This endpoint is applicable to both QR and PN flow.
     */
    String HASH_SIGNING_ENPOINT = HSS_DOMAIN + "/sign-hash";

    /**
     * Initialises a signing process, using PN.
     * @param aParams request payload
     * @return
     */
    CompletionStage<InitCallResult> firstLeg(InitCallParams aParams);

     /**
     * Initialises a signing process, using Push Notification.
     * @param aParams query parameters
     * @return
     */
    CompletionStage<InitCallResult> firstLegQr(InitCallParams aParams);

    /**
     * Sends a document hash to API for signing.
     * @param request payload
     * @return
     */
    CompletionStage<Void> secondLeg(HashSigningRequest request);
}
