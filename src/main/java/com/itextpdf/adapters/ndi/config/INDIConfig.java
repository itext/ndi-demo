package com.itextpdf.adapters.ndi.config;

import java.security.Key;

//load bal amazon
//Client ID       itextcloud
// App Name       itextcloud
// Domain Name    ndi-poc.itextpdf.com
//Redirect URI    https://ndi-poc.itextpdf.com/callback
//Client Secret   URqWGZL6zZbMovGlkymM_bMz
public interface INDIConfig {

    String getClientId();

    String getClientSecret();

    Key getKey();

    String redirectUri();

}
