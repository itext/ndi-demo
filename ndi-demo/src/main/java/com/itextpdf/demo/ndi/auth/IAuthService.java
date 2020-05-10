package com.itextpdf.demo.ndi.auth;


import com.itextpdf.adapters.ndi.client.models.Token;

public interface IAuthService {

    Token getToken(String aNdiId);

    boolean isAuthorized();

    boolean isAuthorized(String aNdiId);

    String getSessionUserId();

    void logoff();

    void login(String aNdiId);
}
