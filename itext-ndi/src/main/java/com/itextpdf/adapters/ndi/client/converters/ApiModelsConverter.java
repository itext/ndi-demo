package com.itextpdf.adapters.ndi.client.converters;

import com.itextpdf.adapters.ndi.client.models.QRTriggerQueryParms;
import com.itextpdf.adapters.ndi.client.models.QRTriggerResponse;
import com.itextpdf.adapters.ndi.client.models.InitCallQrResult;

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
        result.setQrCodeData(qrData);
        return result;
    }

    public QRTriggerQueryParms toQRQueryParam(String clientId, String notificationToken, String aNonce) {
        QRTriggerQueryParms request = new QRTriggerQueryParms();
        request.setNonce(aNonce);
        request.setClientId(clientId);
        request.setClientNotificationToken(notificationToken);
        return request;
    }


}
