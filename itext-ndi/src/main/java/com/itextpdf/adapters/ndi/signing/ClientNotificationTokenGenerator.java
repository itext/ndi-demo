package com.itextpdf.adapters.ndi.signing;

import com.itextpdf.adapters.ndi.signing.api.INotificationTokenGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.crypto.RsaProvider;

import java.security.Key;
import java.security.KeyPair;
import java.util.Date;


public class ClientNotificationTokenGenerator implements INotificationTokenGenerator {

    private static volatile String clientNotificationToken;

    @Override
    public synchronized String getToken() {
        if (clientNotificationToken == null || clientNotificationToken.isEmpty()) {
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
