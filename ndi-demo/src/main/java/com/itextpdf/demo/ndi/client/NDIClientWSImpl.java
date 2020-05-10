package com.itextpdf.demo.ndi.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.adapters.impl.ndi.client.models.PNTriggerRequest;
import com.itextpdf.adapters.impl.ndi.client.models.PNTriggerResponse;
import com.itextpdf.adapters.impl.ndi.client.models.QRTriggerQueryParms;
import com.itextpdf.adapters.impl.ndi.client.models.QRTriggerResponse;
import com.itextpdf.adapters.ndi.client.models.DILoginParams;
import com.itextpdf.adapters.ndi.client.models.Token;
import com.itextpdf.adapters.ndi.client.api.IHssApiClient;
import com.itextpdf.adapters.ndi.client.api.IAuthApi;
import com.itextpdf.adapters.ndi.client.exceptions.NDIServiceException;
import com.itextpdf.adapters.ndi.client.models.HashSigningRequest;
import com.itextpdf.adapters.ndi.client.models.InitCallParams;
import com.itextpdf.adapters.ndi.client.models.InitCallResult;
import com.itextpdf.adapters.ndi.config.INDIInstanceConfig;
import com.itextpdf.adapters.ndi.signing.services.api.INotificationTokenGenerator;
import com.itextpdf.adapters.impl.ndi.client.converters.TokenConverter;
import com.itextpdf.adapters.ndi.client.models.DILoginRequest;
import com.itextpdf.adapters.ndi.client.models.TokenResponse;
import com.itextpdf.demo.ndi.client.converters.NdiClientModelsConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Singleton
public class NDIClientWSImpl implements IHssApiClient, IAuthApi {

    private static final Logger logger = LoggerFactory.getLogger(NDIClientWSImpl.class);

    /**
     * Json content type
     */
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

    private final NdiClientModelsConverter converter = new NdiClientModelsConverter();

    private final TokenConverter tokenConverter = new TokenConverter();


    @Inject
    public NDIClientWSImpl(INDIInstanceConfig config, WSClient client, INotificationTokenGenerator tokenProvider) {
        this.ndiConfig = config;
        this.client = client;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public CompletionStage<InitCallResult> firstLeg(InitCallParams params) {

        PNTriggerRequest request = converter.toPNRequest(params, tokenProvider.getToken());
        JsonNode         node    = Json.toJson(request);
        logger.debug("first leg: url - " + IHssApiClient.PN_TRIGGER_ENDPOINT);
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


    @Override
    public CompletionStage<InitCallResult> firstLegQr(InitCallParams params) {

        QRTriggerQueryParms requestParams = converter.toQRQueryParam(params, ndiConfig.getClientId(),
                                                                     tokenProvider.getToken());
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


    //parse error
    //{"error":"invalid_request","error_description":"Error: Failed to obtain OC
    // SP response: 502"}
    @Override
    public CompletionStage<Token> loginDi(DILoginParams loginParams) {
        String url = DI_LOGIN_URL;
        DILoginRequest request = converter.createLoginRequest(loginParams, ndiConfig.getClientId(),
                                                              ndiConfig.getClientSecret());
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
                }).thenApply(tokenConverter::fromResponse);
    }


    private CompletionStage<WSResponse> post(String url, JsonNode json) {
        return client.url(url)
                     .setAuth(ndiConfig.getClientId(), ndiConfig.getClientSecret())
                     .setContentType(jsonContentType)
                     .post(json);
    }


}
