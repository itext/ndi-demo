package com.itextpdf.adapters.ndi.signing;

import com.itextpdf.adapters.ndi.signing.api.IChainGenerator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * The default chain generator that just load NDI certificates from local crt file.
 * It does not validate the loaded certificates.
 */
public class ChainFromFileGenerator implements IChainGenerator {

    private final List<Certificate> chainCache;

    private final CertificateFactory factory;


    public ChainFromFileGenerator() throws CertificateException {
        this.factory = CertificateFactory.getInstance("X.509");
        this.chainCache = loadChainFromFile();
    }

    @Override
    public Certificate[] getCompleteChain(String userCert) {
        try (InputStream in = new ByteArrayInputStream(userCert.getBytes(StandardCharsets.UTF_8))) {

            Certificate userCertificate = factory.generateCertificate(in);

            List<Certificate> certificates = new ArrayList<>(chainCache);
            certificates.add(0, userCertificate);

            Certificate[] chain = new Certificate[certificates.size()];


            return certificates.toArray(chain);
        } catch (IOException | CertificateException ex) {
            throw new RuntimeException(ex);
        }

    }

    private List<Certificate> loadChainFromFile() throws CertificateException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("cert-chain.crt");
        return (List<Certificate>) factory.generateCertificates(is);

    }
}