package com.itextpdf.adapters.ndi.signing.tsa;

import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.TSAClientBouncyCastle;

/**
 * Tsa client for free TSA. {@see "https://freetsa.org/tsr"}
 */
public class FreeTSAClient extends TSAClientBouncyCastle {

    private static final String url = "https://freetsa.org/tsr";

    private static final int tokenSize = 5470;

    private static final String digestAlgorithm = DigestAlgorithms.SHA256;

    public FreeTSAClient() {
        super(url, null, null, tokenSize, digestAlgorithm);
    }

}
