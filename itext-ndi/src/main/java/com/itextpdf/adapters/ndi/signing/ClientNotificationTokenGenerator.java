package com.itextpdf.adapters.ndi.signing;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.itextpdf.adapters.ndi.signing.api.INotificationTokenGenerator;


import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyPair;
import java.util.Date;


public class ClientNotificationTokenGenerator implements INotificationTokenGenerator {

    private static volatile String clientNotificationToken;

    @Override
    public synchronized String getToken() {
        if (clientNotificationToken == null || clientNotificationToken.isEmpty()) {
            Algorithm algorithm = null;
            try {
                algorithm = Algorithm.HMAC256("secret");
                 clientNotificationToken = JWT.create()
                                  .withIssuer("itext")
                                  .withSubject("itextNdiSign")
                                  .withIssuedAt(new Date())
                                  .sign(algorithm);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }


        }
        return clientNotificationToken;

    }
}
