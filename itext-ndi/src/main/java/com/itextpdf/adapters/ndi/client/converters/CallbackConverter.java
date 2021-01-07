package com.itextpdf.adapters.ndi.client.converters;


import com.itextpdf.adapters.ndi.client.models.callback.*;
import com.itextpdf.adapters.ndi.client.models.callback.common.ErrorSubtypes;
import com.itextpdf.adapters.ndi.client.models.callback.common.ErrorTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class CallbackConverter {

    private static final Logger logger = LoggerFactory.getLogger(CallbackConverter.class);

    public NdiCallbackMessage convertParamsToCallbackMessage(Map<String, String[]> aQueryParams) {
        try {
            Optional<String> opSignRef = extractValue(aQueryParams, "sign_ref");
            Optional<String> opNonce   = extractValue(aQueryParams, "nonce");
            if (!opSignRef.isPresent() || !opNonce.isPresent()) {
                throw new RuntimeException("Either 'sign_ref' or 'nonce'  must be present in the request parameters.");
            }
            return createCallbackFromQueryParam(aQueryParams)
                    .map(
                            o -> {
                                o.setSignRef(opSignRef.get());
                                o.setNonce(opNonce.get());
                                return o;
                            })
                    .orElseThrow(() -> new RuntimeException("The received callback has not be recognized"));
        } catch (Exception e) {
            String       params  = String.join(", ", aQueryParams.keySet());
            final String message = String.format("Callback request cannot be parsed. Params: %s", params);
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    //todo
    private Optional<NdiCallbackMessage> createCallbackFromQueryParam(Map<String, String[]> aQueryParams) {

        Optional<String> opUserCert  = extractValue(aQueryParams, "usr_cert");
        Optional<String> opError     = extractValue(aQueryParams, "error");
        Optional<String> opSignature = extractValue(aQueryParams, "signature");

        Optional<NdiCallbackMessage> opData = opError.map(e -> {
            CallbackErrorMessage errorData = new CallbackErrorMessage();
            extractValue(aQueryParams, "error")
                    .map(ErrorTypes::findByValue)
                    .ifPresent(errorData::setError);
            extractValue(aQueryParams, "error_description")
                    .map(ErrorSubtypes::findByValue)
                    .ifPresent(errorData::setErrorDescription);
            return errorData;
        });
        if (!opData.isPresent()) {
            opData = opUserCert.map(userCert -> {
                CallbackFirstLegMessage firstLegData = new CallbackFirstLegMessage();
                firstLegData.setUserCert(opUserCert.get());
                return firstLegData;
            });
        }
        if (!opData.isPresent()) {
            opData = opSignature.map(signedHash -> {
                CallbackSecondLegMessage sl = new CallbackSecondLegMessage();
                sl.setDocHashSignature(opSignature.get());
                return sl;
            });
        }

        //logger.info("callback has been parsed to " + opData.find().getClass());
        return opData;
    }

    private Optional<String> extractValue(Map<String, String[]> aQueryParams, String aParamName) {
        return Optional.ofNullable(aQueryParams.get(aParamName)).map(s -> s[0]);
    }
}
