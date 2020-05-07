package com.itextpdf.adapters.ndi.auth.services;


import com.itextpdf.adapters.ndi.auth.models.Token;

public interface IAuthService {

    Token getToken(String aNdiId);

    boolean isAuthorized();

    boolean isAuthorized(String aNdiId);

    String getSessionUserId();

    void logoff();

    void login(String aNdiId);
}
