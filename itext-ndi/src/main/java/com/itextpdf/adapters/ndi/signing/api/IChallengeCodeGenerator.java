package com.itextpdf.adapters.ndi.signing.api;

public interface IChallengeCodeGenerator {

    /**
     * Generates new 4 digit challenge code.
     *
     * @return
     */
    Integer generate();
}
