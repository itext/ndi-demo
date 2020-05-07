package com.itextpdf.adapters.ndi.auth;

import com.itextpdf.adapters.ndi.auth.models.Token;

import javax.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;

public class TokenRepository {

    private final ConcurrentHashMap<String, Token> tokens;

    @Inject
    public TokenRepository() {
        this.tokens = new ConcurrentHashMap<>();
    }

    public boolean isExist(String ndiId) {
        return this.tokens.containsKey(ndiId);
    }

    public Token get(String ndiId) {
        return tokens.get(ndiId);
    }

    public void delete(String ndiId) {
        tokens.remove(ndiId);
    }

    public void save(String ndiId, Token token) {
        tokens.put(ndiId, token);
    }

}
