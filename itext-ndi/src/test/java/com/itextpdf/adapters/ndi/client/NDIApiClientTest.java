package com.itextpdf.adapters.ndi.client;

import com.itextpdf.adapters.ndi.client.http.IHttpClient;
import com.itextpdf.adapters.ndi.client.http.SimpleHttpClient;
import com.itextpdf.adapters.ndi.client.models.InitCallQrResult;
import com.itextpdf.adapters.ndi.config.INDIInstanceConfig;
import com.itextpdf.adapters.ndi.config.NDIInstanceConfig;
import com.itextpdf.adapters.ndi.signing.ClientNotificationTokenGenerator;
import com.itextpdf.adapters.ndi.signing.NonceGenerator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;

public class NDIApiClientTest {

    NDIApiClient ndiClient;

    @Before
    public void configure() {

        INDIInstanceConfig               config         = new NDIInstanceConfig(" cb948602f1c4",
                                                                                "UcY4a_perchjVTFSVQ8kUaFY");
        ClientNotificationTokenGenerator tokenGenerator = new ClientNotificationTokenGenerator();
        IHttpClient                      httpClient     = new SimpleHttpClient();
        ndiClient = new NDIApiClient(config, tokenGenerator, httpClient);
    }

    @Test
    @Ignore
    public void firstLeg() {
        NonceGenerator nonceGenerator = new NonceGenerator();
        String         nonce          = nonceGenerator.generate();
        InitCallQrResult result = ndiClient.firstLeg(nonce)
                                           .toCompletableFuture()
                                           .join();

        Assert.assertNotNull(result);
        Assert.assertEquals(nonce, result.getNonce());
        Assert.assertNotNull(result.getSignRef());
        Assert.assertNotNull(result.getQrCodeData());
        System.out.println(result.getSignRef());
        System.out.println(result.getQrCodeData());
    }

    @Test
    public void generateToken() {
        ClientNotificationTokenGenerator generator = new ClientNotificationTokenGenerator();
        String token = generator.getToken();
        System.out.println(token);
    }

    @Test
    public void token() {
        String publicKey = "{\"kty\":\"EC\",\"use\":\"sig\",\"crv\":\"P-256\",\"kid\":\"ndi_dss_stg_01\"," +
                "\"x\":\"zB-0qP88kYqIS-DhssBhRKivQXe4Dj19lmNGDeG3oI0\"," +
                "\"y\":\"8vLz0sqVFm4LMSpypCWTVeMDOAP2n-fz2JgSRTMRkOM\"}";
        String token  = "eyJraWQiOiJuZGlfZHNzX3NpZ25lciIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJzaWduX3JlZiI6ImFhMjg2NTM0LTUwNmEtNDI2OC1iOGI1LTU3ZTE5MzBlNjE2YiIsInJlcXVlc3RfdHlwZSI6InVzZXJfY2VydCIsImV4cCI6MTYwOTkwNzA5NSwidXNlcl9jZXJ0IjoiTUlJQnJUQ0NBVEtnQXdJQkFnSUNBK2N3Q2dZSUtvWkl6ajBFQXdNd05URUxNQWtHQTFVRUJoTUNVMGN4RERBS0JnTlZCQW9NQTA1RVNURVlNQllHQTFVRUF3d1BkR1Z6ZEVCdVpHa3VaMjkyTG5Obk1CNFhEVEl3TURZd016QTNNalF5TjFvWERUSXdNRFl3TXpBM01qWXdOMW93VERFdE1Dc0dBMVVFQlJNa09HRmtPREExWW1VdE56Z3pOQzAwWWpZNExXRmxZek10Tm1NNE5UUTNObVpsWWpFeE1Sc3dHUVlEVlFRRERCSlRNREF3TURBd01Ea2dTbTlvYmlCRWIyVXdkakFRQmdjcWhrak9QUUlCQmdVcmdRUUFJZ05pQUFSb2FRWUVTQWpaUzBISnJwY1g1bWpRZlFzT0RaQ0s1WW1ybFdJejFyaXp3dzRBWEQ5bzRkdFJVZHBNOStGQWtlM2NreFlpWmM5SzJoYXZZdVRLXC9cL2QzT0pHOUVUeXJnRWxVdGhXVzZHYUJkRnNXWmdEc1wvenMzRkhyMFJvTThYXC8wd0NnWUlLb1pJemowRUF3TURhUUF3WmdJeEFMOVlJdTN4WTJ2OWJ3YlwvY2hRZ083SnpicnE4Z3RpMk5WYWhzRDdKTmQ5QSs4UkpyZHlCVEZmWlIwMDR6VjM2T1FJeEFPNVlUcVhRZ3J3KzVQWldaNlJhZXdWREpuN0R5d1hRSmJleTdacnkyN2VkN2h4c1lhVDlCUEE0SW82WXkyaEdLUT09IiwiaWF0IjoxNjA5OTA2OTc1LCJub25jZSI6IjU3NTY0ZTIzLTQ2MzItNGI1Zi04MTY0LTBjNmQ5MjgzODIwNyJ9.GgnqH6Hp4WU_aZklwzooTwaJU5OH43snTa4Id6YSr0fdni6mDhA4Uz1YZvLZagLyM_ew6_hWPY-vwkt5ab8n62lozBQx5dZjioPw0nekW4Xvcc9PiXY3fFtwCkI-yJCW";


    }

    @Test
    public void loadChainFromFile() throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        InputStream        is      = getClass().getClassLoader().getResourceAsStream("NDIDSAPITEXT.cer");
        List<Certificate> cc= (List<Certificate>) factory.generateCertificates(is);

    }
}