package com.itextpdf.adapters.ndi.signing.services;

import com.itextpdf.adapters.ndi.signing.services.api.ITokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.crypto.RsaProvider;

import javax.inject.Inject;
import java.security.Key;
import java.security.KeyPair;
import java.util.Date;


public class ClientNotificationTokenProvider implements ITokenProvider {

    private static volatile String clientNotificationToken;

    @Inject
    public ClientNotificationTokenProvider() {
    }

    @Override
    public synchronized String getToken() {
        if(clientNotificationToken==null||clientNotificationToken.isEmpty()){
            KeyPair pair   = RsaProvider.generateKeyPair();
            Key     key    = pair.getPrivate();
            Claims  claims = new DefaultClaims().setSubject("itextNdiSign").setIssuedAt(new Date());
            clientNotificationToken = Jwts.builder()
                       .setClaims(claims)
                       .signWith(SignatureAlgorithm.RS256, key)
                       .compact();
        }
        return clientNotificationToken;

    }
}
