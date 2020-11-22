package com.itextpdf.demo.ndi.providers;

import com.itextpdf.adapters.ndi.signing.ChainFromFileGenerator;
import com.itextpdf.adapters.ndi.signing.api.IChainGenerator;

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
