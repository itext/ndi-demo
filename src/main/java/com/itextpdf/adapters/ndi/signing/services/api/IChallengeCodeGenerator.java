package com.itextpdf.adapters.ndi.signing.services.api;

public interface IChallengeCodeGenerator {

    /**
     * Generates new 6 digit challenge code.
     * @return
     */
    Integer newCode();
}
