package com.itextpdf.demo.ndi.providers;

import com.itextpdf.adapters.impl.ndi.signing.services.ChainFromFileGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.IChainGenerator;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.security.cert.CertificateException;

@Singleton
public class ChainGeneratorProvider implements Provider<IChainGenerator> {

    @Override
    public IChainGenerator get() {
        try {
            return new ChainFromFileGenerator();
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}
