package com.itextpdf.demo.ndi.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.adapters.ndi.client.converters.ApiModelsConverter;
import com.itextpdf.adapters.ndi.client.models.HashSigningRequest;
import com.itextpdf.adapters.ndi.client.models.InitCallQrResult;
import com.itextpdf.adapters.ndi.client.models.QRTriggerQueryParams;
import com.itextpdf.adapters.ndi.client.models.QRTriggerResponse;
import com.itextpdf.adapters.ndi.client.api.IDSSApiClient;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletionStage;

/** Ndi client implementation based on WSClient */
public class NDIClientWSImpl implements IDSSApiClient {

    private static final Logger logger = LoggerFactory.getLogger(NDIClientWSImpl.class);

    /**Json content type */
    private static final String jsonContentType = "application/json";

    private final INotificationTokenGenerator tokenProvider;

    /** The configuration of the ndi instance*/
    private INDIInstanceConfig ndiConfig;

    /**Client for web services*/
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

        QRTriggerQueryParams requestParams = converter.toQRQueryParam(ndiConfig.getClientId(),
                                                                      tokenProvider.getToken(), aNonce);
        WSRequest wsRequest = client.url(QR_AUTH_ENDPOINT)
                                    .setAuth(ndiConfig.getClientId(), ndiConfig.getClientSecret())
                                    .setQueryString(requestParams.toQueryString());
        logger.info("time:" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        logger.info("request" + wsRequest.getUrl() + "?" + requestParams.toQueryString());
        return wsRequest.get()
                        .thenApply(r -> {
                            if (this.hasErrors(r)) {
                                logger.error(QR_AUTH_ENDPOINT);
                                logger.error(String.format("First leg. Error message received. Code %d info: %s",
                                                           r.getStatus(),
                                                           r.getBody()));
                                throw new NDIServiceException("First leg error " + r.getBody());
                            }
                            logger.info("answ:" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

                            QRTriggerResponse tr     = Json.fromJson(r.asJson(), QRTriggerResponse.class);
                            String            qrData = r.asJson().toString();
                            return converter.toResult(tr, qrData);
                        });
    }

    @Override
    public CompletionStage<Void> secondLeg(HashSigningRequest request) {
        JsonNode node = Json.toJson(request);
        logger.info("second leg: url -" + HASH_SIGNING_ENPOINT_TEMPLATE);
        logger.info("time:" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        logger.info("data: " + node.toString());
        return post(HASH_SIGNING_ENPOINT_TEMPLATE, node)
                .thenAccept((r) -> {
                    if (hasErrors(r)) {
                        logger.error(String.format("Second leg. Error message received. Code %d info: %s",
                                                   r.getStatus(),
                                                   r.getBody()));
                        throw new NDIServiceException("Second leg. Error: " + r.getBody());
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
