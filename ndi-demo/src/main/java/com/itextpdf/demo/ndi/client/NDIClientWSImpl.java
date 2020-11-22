package com.itextpdf.demo.ndi.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.adapters.ndi.client.converters.ApiModelsConverter;
import com.itextpdf.adapters.ndi.client.models.HashSigningRequest;
import com.itextpdf.adapters.ndi.client.models.InitCallQrResult;
import com.itextpdf.adapters.ndi.client.models.QRTriggerQueryParms;
import com.itextpdf.adapters.ndi.client.models.QRTriggerResponse;
import com.itextpdf.adapters.ndi.client.api.IHssApiClient;
import com.itextpdf.adapters.ndi.client.exceptions.NDIServiceException;
import com.itextpdf.adapters.ndi.config.INDIInstanceConfig;
import com.itextpdf.adapters.ndi.signing.api.INotificationTokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/** Ndi client implemetation which is based on WSClient */
public class NDIClientWSImpl implements IHssApiClient {

    private static final Logger logger = LoggerFactory.getLogger(NDIClientWSImpl.class);

    /**Json content type */
    private static final String jsonContentType = "application/json";

    private final INotificationTokenGenerator tokenProvider;

    /**
     * The configuration of the ndi instance
     */
    private INDIInstanceConfig ndiConfig;

    /**
     * Client for web services
     */
    private WSClient client;

    private final ApiModelsConverter converter = new ApiModelsConverter();

    @Inject
    public NDIClientWSImpl(INDIInstanceConfig config, WSClient client, INotificationTokenGenerator tokenProvider) {
        this.ndiConfig = config;
        this.client = client;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public CompletionStage<InitCallQrResult> firstLeg(String aNonce) {

        QRTriggerQueryParms requestParams = converter.toQRQueryParam(ndiConfig.getClientId(),
                                                                     tokenProvider.getToken(), aNonce);
        WSRequest wsRequest = client.url(QR_AUTH_ENDPOINT)
                                    .setAuth(ndiConfig.getClientId(), ndiConfig.getClientSecret())
                                    .setQueryParameter("client_id", requestParams.getClientId())
                                    .setQueryParameter("client_notification_token",
                                                       requestParams.getClientNotificationToken())
                                    .setQueryParameter("response_type", requestParams.getResponseType())
                                    .setQueryParameter("nonce", requestParams.getNonce());
        String s = wsRequest.getQueryParameters()
                            .entrySet()
                            .stream()
                            .map(e -> e.getKey() + "=" + String.join(",", e.getValue()))
                            .collect(
                                    Collectors.joining("&"));

        logger.info("request" + wsRequest.getUrl() + "?" + s);
        return wsRequest.get()
                        .thenApply(r -> {
                            if (this.hasErrors(r)) {
                                logger.error(QR_AUTH_ENDPOINT);
                                logger.error(String.format("First leg. Error message received. Code %d info: %s",
                                                           r.getStatus(),
                                                           r.getBody()));
                                throw new NDIServiceException("First leg error " + r.getBody());
                            }
                            QRTriggerResponse tr     = Json.fromJson(r.asJson(), QRTriggerResponse.class);
                            String            qrData = r.asJson().toString();
                            return converter.toResult(tr, qrData);
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


    private CompletionStage<WSResponse> post(String url, JsonNode json) {
        return client.url(url)
                     .setAuth(ndiConfig.getClientId(), ndiConfig.getClientSecret())
                     .setContentType(jsonContentType)
                     .post(json);
    }


}
