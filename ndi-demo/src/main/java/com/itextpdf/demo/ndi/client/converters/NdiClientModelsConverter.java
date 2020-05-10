package com.itextpdf.demo.ndi.client.converters;

import com.itextpdf.adapters.impl.ndi.client.models.PNTriggerRequest;
import com.itextpdf.adapters.impl.ndi.client.models.PNTriggerResponse;
import com.itextpdf.adapters.impl.ndi.client.models.QRTriggerQueryParms;
import com.itextpdf.adapters.impl.ndi.client.models.QRTriggerResponse;
import com.itextpdf.adapters.ndi.client.models.DILoginParams;
import com.itextpdf.adapters.ndi.client.models.InitCallParams;
import com.itextpdf.adapters.ndi.client.models.InitCallQrResult;
import com.itextpdf.adapters.ndi.client.models.InitCallResult;
import com.itextpdf.adapters.ndi.client.models.DILoginRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class NdiClientModelsConverter {


    //todo
    private LocalDateTime calculateDeadline(Long aExpiresIn) {
        LocalDateTime requestTime = LocalDateTime.now();
        return requestTime.plus(aExpiresIn, ChronoUnit.SECONDS);
    }

    public InitCallQrResult toResult(QRTriggerResponse response, String qrData) {
        InitCallQrResult result = new InitCallQrResult();
        result.setSignRef(response.getSignRef());
        result.setExpiresAt(calculateDeadline(response.getExpiresIn()));
        result.setQrCodeData(qrData);
        return result;
    }

    public InitCallResult toResult(PNTriggerResponse response) {
        InitCallResult result = new InitCallResult();
        result.setSignRef(response.getSignRef());
        result.setExpiresAt(calculateDeadline(response.getExpiresIn()));
        return result;
    }


    public QRTriggerQueryParms toQRQueryParam(InitCallParams params, String clientId, String notificationToken) {
        QRTriggerQueryParms request = new QRTriggerQueryParms();
        request.setLoginHint(params.getUserNdiId());
        request.setNonce(params.getNonce());
        request.setClientId(clientId);
        request.setClientNotificationToken(notificationToken);
        return request;
    }

    public PNTriggerRequest toPNRequest(InitCallParams params, String notificationToken) {
        PNTriggerRequest request = new PNTriggerRequest();
        request.setIdTokenHint(params.getUserNdiId());
        request.setNonce(params.getNonce());
        request.setClientNotificationToken(notificationToken);
        return request;
    }

    public DILoginRequest createLoginRequest(DILoginParams loginParams, String clientId, String clientSecret) {
        DILoginRequest request = new DILoginRequest();
        request.setClientId(clientId);
        request.setClientSecret(clientSecret);
        request.setNonce(loginParams.getNonce());
        request.setBindingMessage(loginParams.getMessage());
        request.setScope("openid");
        request.setLoginHint(loginParams.getNdiId());
        return request;
    }
}
