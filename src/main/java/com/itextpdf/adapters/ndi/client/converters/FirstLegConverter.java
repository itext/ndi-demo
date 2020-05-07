package com.itextpdf.adapters.ndi.client.converters;

import com.itextpdf.adapters.ndi.client.models.InitCallResult;
import com.itextpdf.adapters.ndi.client.models.PNTriggerResponse;
import com.itextpdf.adapters.ndi.client.models.QRTriggerResponse;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class FirstLegConverter {

    @Inject
    public FirstLegConverter() {
    }

    //todo
    private LocalDateTime calculateDeadline(Long aExpiresIn) {
        LocalDateTime requestTime = LocalDateTime.now();
        return requestTime.plus(aExpiresIn, ChronoUnit.SECONDS);
    }

    public InitCallResult toResult(QRTriggerResponse response) {
        LocalDateTime requestTime = LocalDateTime.now();

        InitCallResult result = new InitCallResult();
        result.setSignRef(response.getSignRef());
        result.setExpiresAt(calculateDeadline(response.getExpiresIn()));
        return result;
    }

    public InitCallResult toResult(PNTriggerResponse response) {
        InitCallResult result = new InitCallResult();
        result.setSignRef(response.getSignRef());
        result.setExpiresAt(calculateDeadline(response.getExpiresIn()));
        return result;
    }
}
