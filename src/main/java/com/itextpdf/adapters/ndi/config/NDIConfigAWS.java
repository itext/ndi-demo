package com.itextpdf.adapters.ndi.config;

import io.jsonwebtoken.impl.crypto.RsaProvider;

import javax.inject.Singleton;
import java.security.Key;
import java.security.KeyPair;

//Client ID       itextndi
//App Name       Itext test app
//Domain Name    ndi-poc.itextpdf.com
//Redirect URI    https://ndi-poc.itextpdf.com/callback
//client notification endpoint:  https://ndi-poc.itextpdf.com/api/ndi/callback
//Client Secret   hH_5cnmUaXMlnTbMw4W_DRhN
//hss callback url is always /ndi/callback
@Singleton
public class NDIConfigAWS implements INDIConfig {


    @Override
    public String getClientId() {
        return "itextndi";
    }

    @Override
    public String getClientSecret() {
        return "hH_5cnmUaXMlnTbMw4W_DRhN";
    }

    @Override
    public Key getKey() {
        KeyPair pair = RsaProvider.generateKeyPair();
        return pair.getPrivate();
    }

    @Override
    public String redirectUri() {
        return "https://ndi-poc.itextpdf.com/callback";
    }
}
