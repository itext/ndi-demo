package com.itextpdf.demo.ndi.providers;

import com.itextpdf.signatures.IOcspClient;
import com.itextpdf.signatures.OcspClientBouncyCastle;

import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class OSCPClientProvider implements Provider<IOcspClient> {

    @Override
    public IOcspClient get() {
        return new OcspClientBouncyCastle(null);
    }
}
