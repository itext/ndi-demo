package com.itextpdf.demo.ndi.auth;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static play.mvc.Controller.session;

@Singleton
public class LocalAuthService implements IAuthService {

    private static final Logger logger = LoggerFactory.getLogger(LocalAuthService.class);


    @Inject
    public LocalAuthService() {
    }


    @Override
    public boolean isAuthorized() {
        return Strings.isNotBlank(getSessionUserId());
    }

    public boolean isAuthorized(String aUserName) {
        return Strings.isNotBlank(getSessionUserId()) && getSessionUserId().equals(aUserName);
    }

    public String getSessionUserId() {
        return session("connected");
    }

    @Override
    public void logoff() {
        session("connected", null);
    }

    @Override
    public void login(String aNdiId) {
        session("connected", aNdiId);
    }
}
