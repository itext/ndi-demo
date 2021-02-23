package com.itextpdf.adapters.ndi.config;

/**
 * Basic instance config
 */
public class NDIInstanceConfig implements INDIInstanceConfig {


    final private String clientId;

    final private String clientSecret;

    final  private String keystorePsw;

    final private String keyStorePath;


    public NDIInstanceConfig(String clientId, String clientSecret, String keyStorePath, String keystorePsw) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.keyStorePath = keyStorePath;
        this.keystorePsw = keystorePsw;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public String getKeyPassword() {
        return keystorePsw;
    }

    @Override
    public String sslKeyStorePath() {
        return keyStorePath;
    }
}
