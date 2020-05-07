package com.itextpdf.adapters.ndi.signing.services;

import com.itextpdf.adapters.ndi.signing.services.api.IChainGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ChainProvider implements IChainGenerator {

    private final List<Certificate> chainCache;

    private final CertificateFactory factory;


    @Inject
    public ChainProvider() {

        this.chainCache = new ArrayList<>();
        try {
            this.factory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new IllegalStateException(String.format("Cannot construct %s.", this.getClass().getCanonicalName()),
                                            e);
        }

    }

    @Override
    public Certificate[] getChain(String userCert) {
        try (InputStream in = new ByteArrayInputStream(userCert.getBytes(StandardCharsets.UTF_8))) {

            Certificate userCertificate = factory.generateCertificate(in);

            List<Certificate> certificates = getCachedChain();
            certificates.add(0, userCertificate);

            Certificate[] chain = new Certificate[certificates.size()];


            return certificates.toArray(chain);
        } catch (IOException | CertificateException ex) {
            throw new RuntimeException(ex);
        }

    }

    private List<Certificate> getCachedChain() {
        if (chainCache.size() == 0) {
            synchronized (chainCache) {
                if (chainCache.size() == 0) {
                    try {
                        ClassLoader        classloader = Thread.currentThread().getContextClassLoader();
                        InputStream        is          = classloader.getResourceAsStream("cert-chain.crt");
                        chainCache.addAll(factory.generateCertificates(is));
                    } catch (CertificateException e) {
                        throw new RuntimeException("can`t update cache", e);
                    }
                }
            }
        }
        return chainCache;
    }
}