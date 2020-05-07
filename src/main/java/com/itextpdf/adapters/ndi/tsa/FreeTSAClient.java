package com.itextpdf.adapters.ndi.tsa;

import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.TSAClientBouncyCastle;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * Tsa client for free TSA. {@see "https://freetsa.org/tsr"}
 */
@Singleton
public class FreeTSAClient extends TSAClientBouncyCastle {

    private static final String url = "https://freetsa.org/tsr";

    private static final int tokenSize = 5470;

    private static final String digestAlgorithm = DigestAlgorithms.SHA256;
@Inject
    public FreeTSAClient() {
        super(url, null, null, tokenSize, digestAlgorithm);
    }
}
