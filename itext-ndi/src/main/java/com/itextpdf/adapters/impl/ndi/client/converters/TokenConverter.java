package com.itextpdf.adapters.impl.ndi.client.converters;

import com.itextpdf.adapters.ndi.client.models.Token;
import com.itextpdf.adapters.ndi.client.models.TokenResponse;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TokenConverter {

    public Token fromResponse(TokenResponse from) {
        Token token = new Token();
        token.setAccessToken(from.getAccessToken());
        token.setId(from.getIdToken());
        token.setTokenType(from.getTokenType());
        LocalDateTime exp = LocalDateTime.now().plus(from.getExpiresIn(), ChronoUnit.SECONDS);
        token.setExpiresAt(exp);
        return token;
    }
}