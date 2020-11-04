package com.itextpdf.adapters.ndi.client.api;

import com.itextpdf.adapters.ndi.impl.client.models.HashSigningRequest;
import com.itextpdf.adapters.ndi.impl.client.models.InitCallQrResult;

import java.util.concurrent.CompletionStage;

/** Interface caller  for NDI hss api.**/

public interface IHssApiClient {

    String HSS_DOMAIN = "https://api.sandbox.ndi.gov.sg/api/v1/hss/signatures";



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
     * Initialises a signing process, using Push Notification.
     *
      * @param aNonce @return
      * @return
      */
    CompletionStage<InitCallQrResult> firstLegQr(String aNonce);

    /**
     * Sends a document hash to API for signing.
     * @param request payload
     * @return
     */
    CompletionStage<Void> secondLeg(HashSigningRequest request);
}
