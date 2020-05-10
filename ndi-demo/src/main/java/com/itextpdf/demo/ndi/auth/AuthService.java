package com.itextpdf.demo.ndi.auth;

import com.itextpdf.demo.ndi.auth.repository.TokenRepository;
import com.itextpdf.adapters.ndi.client.models.DILoginParams;
import com.itextpdf.adapters.ndi.client.models.Token;
import com.itextpdf.adapters.ndi.client.api.IAuthApi;
import com.itextpdf.adapters.ndi.signing.services.api.INonceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import static play.mvc.Controller.session;

@Singleton
public class AuthService implements IAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final ReentrantLock tokenRequestLock = new ReentrantLock();

    private final IAuthApi tokenClient;

    private final INonceGenerator nonceGenerator;

    private final TokenRepository tokenRepository;



    @Inject
    public AuthService(IAuthApi tokenClient,
                       INonceGenerator nonceGenerator,
                       TokenRepository tokenRepository) {
        this.tokenClient = tokenClient;
        this.nonceGenerator = nonceGenerator;
        this.tokenRepository = tokenRepository;
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
        DILoginParams request =  new DILoginParams(aNdiId, nonceGenerator.generate(), null);
        tokenClient.loginDi(request)
                   .thenAccept(t -> tokenRepository.save(aNdiId, t))
                   .toCompletableFuture()
                   .join();
    }

    private boolean isExpired(Token token) {
        logger.info("token date: " + DateTimeFormatter.ofPattern("hh:mm:ss").format(token.getExpiresAt()));
        return token.getExpiresAt().isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isAuthorized() {

        return isAuthorized(getSessionUserId());
    }

    public boolean isAuthorized(String aNdiId) {

        return Objects.nonNull(aNdiId) && tokenRepository.isExist(aNdiId) && !isExpired(tokenRepository.get(aNdiId));
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
