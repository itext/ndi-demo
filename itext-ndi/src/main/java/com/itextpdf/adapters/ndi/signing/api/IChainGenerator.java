package com.itextpdf.adapters.ndi.signing.api;

import java.security.cert.Certificate;

public interface IChainGenerator {

    /**
     * Returns complete chain for provided string representation of user certificate
     * @param userCert String representation of x.509 certificate
     * @return
     */
    Certificate[] getCompleteChain(String userCert);
}
