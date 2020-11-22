package com.itextpdf.adapters.ndi.impl.config;

import com.itextpdf.adapters.ndi.config.INDIInstanceConfig;

/**
 * Basic instance config
 */
public class NDIInstanceConfig implements INDIInstanceConfig {


    final private String clientId;

    final private String clientSecret;


    public NDIInstanceConfig(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }
}
