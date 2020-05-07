package com.itextpdf.adapters.ndi.client;

import java.security.cert.Certificate;

public interface IOCSPClient {

    Certificate getIssuerCertificate();
}
