package com.itextpdf.adapters.ndi.client.converters;

import com.itextpdf.adapters.ndi.client.models.InitCallQrResult;
import com.itextpdf.adapters.ndi.client.models.QRTriggerQueryParams;
import com.itextpdf.adapters.ndi.client.models.QRTriggerResponse;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ApiModelsConverter {


    private LocalDateTime calculateDeadline(Long aExpiresIn) {
        LocalDateTime requestTime = LocalDateTime.now();
        return requestTime.plus(aExpiresIn, ChronoUnit.SECONDS);
    }

    public InitCallQrResult toResult(QRTriggerResponse response, String qrData) {
        InitCallQrResult result = new InitCallQrResult();
        result.setSignRef(response.getSignRef());
        result.setExpiresAt(calculateDeadline(response.getExpiresIn()));
        result.setNonce(response.getNonce());
        result.setQrCodeData(qrData);
        return result;
    }

    public QRTriggerQueryParams toQRQueryParam(String clientId, String notificationToken, String aNonce) {
        QRTriggerQueryParams request = new QRTriggerQueryParams();
        request.setNonce(aNonce);
        request.setClientId(clientId);
        request.setClientNotificationToken(notificationToken);
        return request;
    }


}
