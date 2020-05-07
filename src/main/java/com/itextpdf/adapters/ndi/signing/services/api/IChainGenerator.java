package com.itextpdf.adapters.ndi.signing.services.api;

import java.security.cert.Certificate;

public interface IChainGenerator {

    Certificate[] getChain(String userCert);
}
