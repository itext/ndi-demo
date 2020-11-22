package com.itextpdf.demo.ndi.auth;



public interface IAuthService {

    boolean isAuthorized();

    boolean isAuthorized(String aUserName);

    String getSessionUserId();

    void logoff();

    void login(String aNdiId);
}
