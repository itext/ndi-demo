package com.itextpdf.adapters.impl.ndi.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.itextpdf.adapters.impl.ndi.client.converters.ApiModelsConverter;
import com.itextpdf.adapters.impl.ndi.client.converters.TokenConverter;
import com.itextpdf.adapters.impl.ndi.client.models.PNTriggerRequest;
import com.itextpdf.adapters.impl.ndi.client.models.PNTriggerResponse;
import com.itextpdf.adapters.impl.ndi.client.models.QRTriggerQueryParms;
import com.itextpdf.adapters.impl.ndi.client.models.QRTriggerResponse;
import com.itextpdf.adapters.ndi.client.IWebClient;
import com.itextpdf.adapters.ndi.client.api.IAuthApi;

import com.itextpdf.adapters.ndi.client.api.IHssApiClient;
import com.itextpdf.adapters.ndi.client.exceptions.NDIServiceException;
import com.itextpdf.adapters.ndi.client.models.*;
import com.itextpdf.adapters.ndi.config.INDIInstanceConfig;
import com.itextpdf.adapters.ndi.signing.services.api.INotificationTokenGenerator;
import com.itextpdf.kernel.xmp.impl.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;


/**
 * API service, supports
 */
public class NDIApiClientService implements IHssApiClient, IAuthApi {

    private static final Logger logger = LoggerFactory.getLogger(NDIApiClientService.class);

    /**
     * Json content type
     */
    private static final String jsonContentType = "application/json";

    private final ApiModelsConverter converter;

    private final TokenConverter tokenConverter;

    private final ObjectMapper objectMapper;

    /**
     * The configuration of the ndi instance
     */
    private final INDIInstanceConfig ndiConfig;

    private final INotificationTokenGenerator tokenProvider;

    private final IWebClient webClient;

    public NDIApiClientService(INDIInstanceConfig ndiConfig,
                               INotificationTokenGenerator tokenProvider, IWebClient webClient) {
        this.ndiConfig = ndiConfig;
        this.tokenProvider = tokenProvider;
        this.webClient = webClient;
        converter = new ApiModelsConverter();
        tokenConverter = new TokenConverter();
        objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    private String getAuthHeader() {
        return "Basic " + Base64.encode(String.format("%s:%s", ndiConfig.getClientId(), ndiConfig.getClientSecret()));
    }

    private Map<String, String> headers(String jsonString) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", jsonContentType);
        headers.put("Content-Length", String.valueOf(jsonString.length()));
        return headers;
    }

    private boolean hasErrors(HttpResponse response) {
        return response.getStatus() != HTTP_OK && response.getStatus() != HTTP_CREATED;
    }

    @Override
    public CompletionStage<InitCallResult> firstLeg(InitCallParams aParams) {

        PNTriggerRequest request    = converter.toPNRequest(aParams, tokenProvider.getToken());
        String           jsonString = toJsonString(request);

        logger.debug("first leg: url - " + IHssApiClient.PN_TRIGGER_ENDPOINT);
        logger.debug("data: " + jsonString);


        return CompletableFuture.supplyAsync(() ->
                                                     webClient.post(PN_TRIGGER_ENDPOINT, getAuthHeader(),
                                                                    headers(jsonString), jsonString))
                                .thenApply(r -> {
                                    if (this.hasErrors(r)) {
                                        logger.error(
                                                String.format("First leg. Error message received. Code %d info: %s",
                                                              r.getStatus(),
                                                              r.getBody()));
                                        throw new NDIServiceException("First leg error " + r.toString());
                                    }
                                    return mapToObject(r.getBody(), PNTriggerResponse.class);
                                })
                                .thenApply(converter::toResult)
                                .toCompletableFuture();
    }

    private String toJsonString(Object request) {
        try {
            JsonNode node = objectMapper.valueToTree(request);
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T mapToObject(String jsonBody, Class<T> aClass) {
        try {
            JsonNode resNode = objectMapper.readTree(jsonBody);
            return objectMapper.treeToValue(resNode, aClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletionStage<InitCallResult> firstLegQr(InitCallParams aParams) {
        QRTriggerQueryParms requestParams = converter.toQRQueryParam(aParams, ndiConfig.getClientId(),
                                                                     tokenProvider.getToken());

        String query = "client_id=" + requestParams.getClientId() +
                "&client_notification_token=" + requestParams.getClientNotificationToken() +
                "&response_type=" + requestParams.getResponseType() +
                "&nonce=" + requestParams.getNonce();
        String fullUrl = IHssApiClient.QR_AUTH_ENDPOINT + "?" + query;

        logger.info("request" + fullUrl);
        return CompletableFuture.supplyAsync(() -> webClient.get(fullUrl, getAuthHeader()))
                                .thenApply(r -> {
                                    if (this.hasErrors(r)) {
                                        logger.error(QR_AUTH_ENDPOINT);
                                        logger.error(
                                                String.format("First leg. Error message received. Code %d info: %s",
                                                              r.getStatus(),
                                                              r.getBody()));
                                        throw new NDIServiceException("First leg error: " + r.getBody());
                                    }
                                    logger.info("d: " + r.getBody());
                                    QRTriggerResponse tr = mapToObject(r.getBody(), QRTriggerResponse.class);
                                    return converter.toResult(tr, r.getBody());
                                });
    }

    @Override
    public CompletionStage<Void> secondLeg(HashSigningRequest request) {
        String jsonString = toJsonString(request);
        logger.info("second leg: url -" + HASH_SIGNING_ENPOINT);
        logger.info("data: " + jsonString);
        return CompletableFuture.supplyAsync(
                () -> webClient.post(HASH_SIGNING_ENPOINT, getAuthHeader(), headers(jsonString), jsonString))
                                .thenAccept((r) -> {
                                    if (hasErrors(r)) {
                                        logger.error(
                                                String.format("Second leg. Error message received. Code %d info: %s",
                                                              r.getStatus(),
                                                              r.getBody()));
                                        throw new NDIServiceException("Second leg. Error: " + r.getBody());
                                    }
                                    logger.info("success");
                                });
    }

    @Override
    public CompletionStage<Token> loginDi(DILoginParams loginParams) {
        String url = DI_LOGIN_URL;
        DILoginRequest request = converter.createLoginRequest(loginParams, ndiConfig.getClientId(),
                                                              ndiConfig.getClientSecret());
        String jsonString = toJsonString(request);
        logger.info("di-auth: url - " + url);
        logger.info("data - " + jsonString);
        return CompletableFuture.supplyAsync(
                () -> webClient.post(url, getAuthHeader(), headers(jsonString), jsonString))
                                .thenApply(r -> {
                                    logger.info(r.getBody());
                                    logger.info("st:" + r.getStatus());
                                    if (hasErrors(r)) {
                                        throw new RuntimeException("Incorrect response from NDI server.");
                                    }
                                    return this.mapToObject(r.getBody(), TokenResponse.class);
                                })
                                .thenApply(tokenConverter::fromResponse);
    }
}
