package com.itextpdf.adapters.ndi.client.converters;

import com.itextpdf.adapters.ndi.client.models.InitCallQrResult;
import com.itextpdf.adapters.ndi.client.models.SignInitResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ApiModelsConverter {


    private LocalDateTime toLocalDateTime(Long aExpiresAt) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(aExpiresAt), ZoneId.systemDefault());
    }

    public InitCallQrResult toResult(SignInitResponse response) {
        InitCallQrResult result = new InitCallQrResult();
        result.setSignRef(response.getSignRef());
        result.setExpiresAt(toLocalDateTime(response.getExpiresAt()));
        result.setQrCodeData(response.getQrCode().getPayload());
        return result;
    }




}
