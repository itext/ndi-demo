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

public class NDIApiClientTest {

    NDIApiClient ndiClient ;

    @Before
    public void configure(){

        INDIInstanceConfig config = new NDIInstanceConfig(" cb948602f1c4", "UcY4a_perchjVTFSVQ8kUaFY" );
        ClientNotificationTokenGenerator tokenGenerator = new ClientNotificationTokenGenerator();
        IHttpClient httpClient = new SimpleHttpClient();
        ndiClient = new NDIApiClient(config, tokenGenerator, httpClient);
    }
    @Test
    public void firstLeg() {
        NonceGenerator nonceGenerator = new NonceGenerator();
        String nonce = nonceGenerator.generate();
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

}