package com.itextpdf.adapters.ndi.client.api;

import java.security.cert.Certificate;

public interface IOCSPApi {

    String CERTIFICATE_CHAIN_URL = "http://ocsp.sandbox.ndi.gov.sg/api/v1/va/download/cert-chain.crt";

    Certificate downloadChain();
}
