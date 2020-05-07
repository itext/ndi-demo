package com.itextpdf.adapters.ndi.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.adapters.ndi.auth.models.DILoginRequest;
import com.itextpdf.adapters.ndi.auth.models.TokenResponse;
import com.itextpdf.adapters.ndi.client.INDIClient;
import com.itextpdf.adapters.ndi.client.ITokenClient;
import com.itextpdf.adapters.ndi.client.NDIServiceException;
import com.itextpdf.adapters.ndi.client.converters.FirstLegConverter;
import com.itextpdf.adapters.ndi.client.models.*;
import com.itextpdf.adapters.ndi.config.INDIConfig;
import com.itextpdf.adapters.ndi.signing.services.api.ITokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.CompletionStage;

@Singleton
public class NDIClientWSImpl implements INDIClient, ITokenClient {

    private static final Logger logger = LoggerFactory.getLogger(NDIClientWSImpl.class);

    /**
     * Json conten type
     */
    private static final String JsonContentType = "application/json";

    private final FirstLegConverter converter = new FirstLegConverter();

    private final ITokenProvider tokenProvider;

    /**
     * configuration of the ndi services
     */
    private INDIConfig ndiConfig;

    /**
     * Client for web services
     */
    private WSClient client;

    @Inject
    public NDIClientWSImpl(INDIConfig config, WSClient client,
                           ITokenProvider tokenProvider) {
        this.ndiConfig = config;
        this.client = client;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public CompletionStage<InitCallResult> firstLeg(InitCallParams params) {

        PNTriggerRequest request = convertToRequest(params);
        JsonNode         node    = Json.toJson(request);
        logger.debug("first leg: url - " + INDIClient.PN_TRIGGER_ENDPOINT);
        logger.debug("data: " + node.toString());

        return post(PN_TRIGGER_ENDPOINT, node)
                .thenApply(r -> {
                    if (this.hasErrors(r)) {
                        logger.error(String.format("First leg. Error message received. Code %d info: %s",
                                                   r.getStatus(),
                                                   r.getBody()));
                        throw new NDIServiceException("First leg error " + r.toString());
                    }
                    return Json.fromJson(r.asJson(), PNTriggerResponse.class);
                })
                .thenApply(converter::toResult)
                .toCompletableFuture();
    }

    private PNTriggerRequest convertToRequest(InitCallParams params) {
        PNTriggerRequest request = new PNTriggerRequest();
        request.setIdTokenHint(params.getUserNdiId());
        request.setNonce(params.getNonce());
        request.setClientNotificationToken(tokenProvider.getToken());
        return request;
    }

    private QRTriggerQueryParms convertToQRQueryParam(InitCallParams params) {
        QRTriggerQueryParms request = new QRTriggerQueryParms();
        request.setLoginHint(params.getUserNdiId());
        request.setNonce(params.getNonce());
        request.setClientId(ndiConfig.getClientId());
        request.setClientNotificationToken(tokenProvider.getToken());
        return request;
    }

    @Override
    public CompletionStage<InitCallResult> firstLegQr(InitCallParams params) {

        QRTriggerQueryParms requestParams = convertToQRQueryParam(params);
        WSRequest wsRequest = client.url(QR_AUTH_ENDPOINT)
                                    .setAuth(ndiConfig.getClientId(), ndiConfig.getClientSecret())
                                    .setQueryParameter("client_id", requestParams.getClientId())
                                    .setQueryParameter("client_notification_token",
                                                       requestParams.getClientNotificationToken())
                                    .setQueryParameter("response_type", requestParams.getResponseType())
                                    .setQueryParameter("nonce", requestParams.getNonce());

        return wsRequest.get()
                        .thenApply(r -> {
                            if (this.hasErrors(r)) {
                                logger.error(QR_AUTH_ENDPOINT);
                                logger.error(String.format("First leg. Error message received. Code %d info: %s",
                                                           r.getStatus(),
                                                           r.getBody()));
                                throw new NDIServiceException("First leg error " + r.toString());
                            }
                            QRTriggerResponse tr     = Json.fromJson(r.asJson(), QRTriggerResponse.class);
                            InitCallResult    result = converter.toResult(tr);
                            result.setQrCodeData(r.asJson().toString());
                            return result;
                        });
    }

    @Override
    public CompletionStage<Void> secondLeg(HashSigningRequest request) {
        JsonNode node = Json.toJson(request);
        logger.info("second leg: url -" + HASH_SIGNING_ENPOINT);
        logger.info("data: " + node.toString());
        return post(HASH_SIGNING_ENPOINT, node)
                .thenAccept((r) -> {
                    if (hasErrors(r)) {
                        logger.error(String.format("Second leg. Error message received. Code %d info: %s",
                                                   r.getStatus(),
                                                   r.getBody()));
                        throw new NDIServiceException("Second leg. Error: " + r.toString());
                    }
                    logger.info("success");
                });
    }

    private boolean hasErrors(WSResponse data) {
        return Http.Status.OK != data.getStatus() && Http.Status.CREATED != data.getStatus();
    }

    //todo check if id is needed;
    private String getSignedToken() {
        //  String secret = ndiConfig.getString("play.http.secret.key");

        Key    key    = ndiConfig.getKey();//Keys.secretKeyFor(SignatureAlgorithm.HS256);
        Claims claims = new DefaultClaims().setSubject("itextNdiSign").setIssuedAt(new Date());
        return Jwts.builder()
                   .setClaims(claims)
                   .signWith(SignatureAlgorithm.RS256, key)
                   .compact();
    }


    //parse error
    //{"error":"invalid_request","error_description":"Error: Failed to obtain OC
    // SP response: 502"}
    @Override
    public CompletionStage<TokenResponse> loginDi(DILoginRequest request) {
        String   url  = DI_LOGIN_URL;
        JsonNode json = Json.toJson(request);
        logger.info("di-auth: url - " + url);
        logger.info("data - " + json.toString());
        return post(url, json)
                .thenApply(r -> {
                    logger.info(r.getBody());
                    logger.info("st:" + r.getStatus());
                    if (r.getStatus() != 200 && r.getStatus() != 201) {
                        throw new RuntimeException("Incorrect response from NDI server.");
                    }
                    return Json.fromJson(r.asJson(), TokenResponse.class);
                });
    }


    private CompletionStage<WSResponse> post(String url, JsonNode json) {
        return client.url(url)
                     .setAuth(ndiConfig.getClientId(), ndiConfig.getClientSecret())
                     .setContentType(JsonContentType)
                     .post(json);
    }


}
