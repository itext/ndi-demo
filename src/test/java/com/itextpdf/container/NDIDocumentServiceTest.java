package com.itextpdf.container;

import com.itextpdf.adapters.ndi.auth.services.FakeAuthService;
import com.itextpdf.adapters.ndi.auth.services.IAuthService;
import org.junit.Assert;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static play.inject.Bindings.bind;

public class NDIDocumentServiceTest extends WithApplication {

    @Override
    protected Application provideApplication() {

        return new GuiceApplicationBuilder().overrides(bind(IAuthService.class).to(FakeAuthService.class))
                                            .build();
    }

    @Test
    public void chainParsingTest() {
        try {
            ClassLoader        classloader = Thread.currentThread().getContextClassLoader();
            InputStream        is          = classloader.getResourceAsStream("cert-chain.crt");
            CertificateFactory fact        = null;
            fact = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> certificate = fact.generateCertificates(is);
            Assert.assertNotNull(certificate);
            Assert.assertEquals(2, certificate.size());
        } catch (CertificateException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createChain() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = classloader.getResourceAsStream("cert")) {

            CertificateFactory fact            = CertificateFactory.getInstance("X.509");
            Certificate        userCertificate = fact.generateCertificate(in);

            List<Certificate> certificates = getCachedChain();
            certificates.add(0, userCertificate);

            Certificate[] chain = new Certificate[certificates.size()];
            certificates.toArray(chain);
            Assert.assertEquals(3, certificates.size());
        } catch (IOException | CertificateException ex) {
            throw new RuntimeException(ex);
        }

    }

    public List<Certificate> getCachedChain() {
        try {
            ClassLoader        classloader = Thread.currentThread().getContextClassLoader();
            InputStream        is          = classloader.getResourceAsStream("cert-chain.crt");
            CertificateFactory fact        = CertificateFactory.getInstance("X.509");
            return new ArrayList<>(fact.generateCertificates(is));
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}