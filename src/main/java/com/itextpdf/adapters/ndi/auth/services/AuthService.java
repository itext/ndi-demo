package com.itextpdf.adapters.ndi.auth.services;

import com.itextpdf.adapters.ndi.auth.TokenConverter;
import com.itextpdf.adapters.ndi.auth.TokenRepository;
import com.itextpdf.adapters.ndi.auth.models.DILoginRequest;
import com.itextpdf.adapters.ndi.auth.models.Token;
import com.itextpdf.adapters.ndi.client.ITokenClient;
import com.itextpdf.adapters.ndi.config.INDIConfig;
import com.itextpdf.adapters.ndi.signing.services.api.INonceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

import static play.mvc.Controller.session;

@Singleton
public class AuthService implements IAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final ReentrantLock tokenRequestLock = new ReentrantLock();

    private final ITokenClient tokenClient;

    private final INDIConfig ndiConfig;

    private final INonceGenerator nonceGenerator;


    private final TokenRepository tokenRepository;

    private final TokenConverter tokenConverter;


    @Inject
    public AuthService(ITokenClient tokenClient, INonceGenerator nonceGenerator, TokenRepository tokenRepository,
                       TokenConverter tokenConverter, INDIConfig ndiConfig) {
        this.tokenClient = tokenClient;
        this.nonceGenerator = nonceGenerator;
        this.tokenRepository = tokenRepository;
        this.tokenConverter = tokenConverter;
        this.ndiConfig = ndiConfig;
    }


    @Override
    public Token getToken(String aNdiId) {
        if (!isAuthorized(aNdiId)) {
            tokenRequestLock.lock();
            try {
                if (!isAuthorized(aNdiId)) {
                    auth(aNdiId);
                    session("connected", aNdiId);
                }
            } finally {
                tokenRequestLock.unlock();
            }

        }
        return tokenRepository.get(aNdiId);
    }

    private void auth(String aNdiId) {
        DILoginRequest request = createLoginRequest(aNdiId);
        tokenClient.loginDi(request)
                   .thenApply(tokenConverter::fromResponse)
                   .thenAccept(t -> tokenRepository.save(aNdiId, t))
                   .toCompletableFuture()
                   .join();
    }

    private DILoginRequest createLoginRequest(String aNdiId) {
        DILoginRequest request = new DILoginRequest();
        request.setClientId(ndiConfig.getClientId());
        request.setClientSecret(ndiConfig.getClientSecret());
        request.setNonce(nonceGenerator.newNonce());
        request.setScope("openid");
        request.setLoginHint(aNdiId);
        return request;
    }

    private boolean isExpired(Token token) {
        logger.info("token date: " + DateTimeFormatter.ofPattern("hh:mm:ss").format(token.getExpiresAt()));
        return token.getExpiresAt().isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isAuthorized() {

             return    isAuthorized(getSessionUserId());
    }

    public boolean isAuthorized(String aNdiId) {

        return tokenRepository.isExist(aNdiId) && !isExpired(tokenRepository.get(aNdiId));
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
        if (!isAuthorized(aNdiId)) {
            getToken(aNdiId);
        }
    }
}
