package com.itextpdf.adapters.ndi.client.models.callback;

/**
 * Second Leg callback.
 * Document Hash Signature Notification Claims
 *
 * https://stg-id.singpass.gov.sg/docs/doc-signing#doc_hash_signature_notification_claims
 * {
 *   "doc_hash" : "6ac7637da92c76385f95a92c7617e591a8f6df8f74f37ef8db7e25e648e1db7e",
 *   "sign_ref" : "8f458462-9c3e-4fda-b489-0e0b4850d132",
 *   "doc_hash_signature" :
 *   "3066023100b9f018223c3c8ff90cb14ad0c57beb7c2b9f76f4ff9ab0d15112550af9fb66d8becd27264da5ef02a253d03ea19c0763023100a415c69c5c25aafda340aff3d4bceca92026db93a441e23ae6ac8f6de30e4eef311c89b9a6680e7350b14b1596cbebd5",
 *   "request_type" : "signed_doc_hash",
 *   "exp" : 1609907095,
 *   "iat" : 1609906975
 * }
 */
public final class CallbackSecondLegMessage extends NdiCallbackMessage {

    /** Signed hash */
    private String docHashSignature;

    public String getDocHashSignature() {
        return docHashSignature;
    }

    public void setDocHashSignature(String docHashSignature) {
        this.docHashSignature = docHashSignature;
    }

}
