package com.itextpdf.adapters.ndi.signing.api;

public interface INonceGenerator {

    /**
     * Generates a nonce which cannot be predicted, to ensure the challenge is always
     * unique and not subjected to replay attacks
     *
     * @return new nonce
     */
    String generate();
}
