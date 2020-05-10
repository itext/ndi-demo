package com.itextpdf.adapters.ndi.signing.services.api;

public interface IChallengeCodeGenerator {

    /**
     * Generates new 4 digit challenge code.
     *
     * @return
     */
    Integer generate();
}
