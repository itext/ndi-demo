package com.itextpdf.adapters.ndi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.itextpdf.adapters.ndi.client.api.IDSSApiClient;
import com.itextpdf.adapters.ndi.client.converters.ApiModelsConverter;
import com.itextpdf.adapters.ndi.client.exceptions.NDIServiceException;
import com.itextpdf.adapters.ndi.client.http.HttpResponse;
import com.itextpdf.adapters.ndi.client.http.IHttpClient;
import com.itextpdf.adapters.ndi.client.models.HashSigningRequest;
import com.itextpdf.adapters.ndi.client.models.InitCallQrResult;
import com.itextpdf.adapters.ndi.client.models.QRTriggerQueryParams;
import com.itextpdf.adapters.ndi.client.models.QRTriggerResponse;
import com.itextpdf.adapters.ndi.config.INDIInstanceConfig;
import com.itextpdf.adapters.ndi.signing.api.INotificationTokenGenerator;
import com.itextpdf.kernel.xmp.impl.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * API client. Used underneath the {@see SimpleHttpClient } for web requests.
 */
public class NDIApiClient implements IDSSApiClient {

    private static final Logger logger = LoggerFactory.getLogger(NDIApiClient.class);

    /**
     * Json content type
     */
    private static final String jsonContentType = "application/json";

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    private static final ApiModelsConverter converter = new ApiModelsConverter();


    /** The configuration of the ndi instance */
    private final INDIInstanceConfig ndiConfig;


    private final IHttpClient webClient;

    private final INotificationTokenGenerator tokenProvider;

    public NDIApiClient(INDIInstanceConfig ndiConfig, INotificationTokenGenerator tokenProvider,
                        IHttpClient webClient) {
        this.ndiConfig = ndiConfig;
        this.webClient = webClient;
        this.tokenProvider = tokenProvider;
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
    public CompletionStage<InitCallQrResult> firstLeg(String aNonce) {
        QRTriggerQueryParams requestParams = converter.toQRQueryParam(ndiConfig.getClientId(),
                                                                      tokenProvider.getToken(), aNonce);

        String query   = requestParams.toQueryString();
        String fullUrl = QR_AUTH_ENDPOINT + "?" + query;
        logger.info("time " +LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        logger.info("first leg, request: " + fullUrl);
        return CompletableFuture.supplyAsync(() -> webClient.get(fullUrl, getAuthHeader()))
                                .thenApply(r -> {
                                    if (this.hasErrors(r)) {
                                        logger.error(
                                                String.format("First leg. Error message received. Code %d info: %s",
                                                              r.getStatus(),
                                                              r.getBody()));
                                        throw new NDIServiceException("First leg error: " + r.getBody());
                                    }
                                    logger.info("body: " + r.getBody());
                                    logger.info(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                                    QRTriggerResponse tr = mapToObject(r.getBody(), QRTriggerResponse.class);
                                    return converter.toResult(tr, r.getBody());
                                });
    }

    @Override
    public CompletionStage<Void> secondLeg(HashSigningRequest request) {
        String jsonString = toJsonString(request);
        logger.info("time:" +LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        logger.info("second leg: url -" + getHashSigningEndpointUrl(request.getSignRef()));
        logger.info("data: " + jsonString);
        return CompletableFuture.supplyAsync(
                () -> webClient.post(getHashSigningEndpointUrl(request.getSignRef()),
                                     getAuthHeader(), headers(jsonString), jsonString))
                                .thenAccept((r) -> {
                                    if (hasErrors(r)) {
                                        logger.error(
                                                String.format("Second leg. Error: code %d info: %s",
                                                              r.getStatus(),
                                                              r.getBody()));
                                        throw new NDIServiceException("Second leg. Error: " + r.getBody());
                                    }
                                    logger.info(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                                    logger.info("success. s:" + r.getStatus() + " body:" + r.getBody());
                                });
    }

}
