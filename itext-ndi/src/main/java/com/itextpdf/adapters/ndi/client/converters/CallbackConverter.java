package com.itextpdf.adapters.ndi.client.converters;


import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.itextpdf.adapters.ndi.client.models.callback.CallbackErrorMessage;
import com.itextpdf.adapters.ndi.client.models.callback.CallbackFirstLegMessage;
import com.itextpdf.adapters.ndi.client.models.callback.CallbackSecondLegMessage;
import com.itextpdf.adapters.ndi.client.models.callback.NdiCallbackMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class CallbackConverter {

    private static final Logger logger = LoggerFactory.getLogger(CallbackConverter.class);

    public NdiCallbackMessage convertJWTToCallback(String jwtToken) {

        DecodedJWT jwt = JWT.decode(jwtToken);
        System.out.println(jwt.getClaim("user_cert").asString());
        System.out.println(jwt.getClaim("request_type").asString());

        //"signed_doc_hash"
        //"error"
        //"user_cert"

        /**
         * "sign_ref" : "1e0148ea-24c2-4e55-80c0-b23dbd45d2f2",
         *   "request_type" : "user_cert",
         *   "exp" : 1610588424,
         *   "user_cert" :
         *   "MIIBrTCCATKgAwIBAgICA
         *   +cwCgYIKoZIzj0EAwMwNTELMAkGA1UEBhMCU0cxDDAKBgNVBAoMA05ESTEYMBYGA1UEAwwPdGVzdEBuZGkuZ292LnNnMB4XDTIwMDYwMzA3MjQyN1oXDTIwMDYwMzA3MjYwN1owTDEtMCsGA1UEBRMkOGFkODA1YmUtNzgzNC00YjY4LWFlYzMtNmM4NTQ3NmZlYjExMRswGQYDVQQDDBJTMDAwMDAwMDkgSm9obiBEb2UwdjAQBgcqhkjOPQIBBgUrgQQAIgNiAARoaQYESAjZS0HJrpcX5mjQfQsODZCK5YmrlWIz1rizww4AXD9o4dtRUdpM9+FAke3ckxYiZc9K2havYuTK//d3OJG9ETyrgElUthWW6GaBdFsWZgDs/zs3FHr0RoM8X/0wCgYIKoZIzj0EAwMDaQAwZgIxAL9YIu3xY2v9bwb/chQgO7Jzbrq8gti2NVahsD7JNd9A+8RJrdyBTFfZR004zV36OQIxAO5YTqXQgrw+5PZWZ6RaewVDJn7DywXQJbey7Zry27ed7hxsYaT9BPA4Io6Yy2hGKQ==",
         *   "iat" : 1610588304,
         *   "nonce" : "9176f2c0-509f-4fae-8ebc-2dd6ece8575b"
         */

        /**
         *   "doc_hash" : "6ac7637da92c76385f95a92c7617e591a8f6df8f74f37ef8db7e25e648e1db7e",
         *   "sign_ref" : "e5b2bb03-90ce-44ea-b9eb-bdd4e20eeaf6",
         *   "doc_hash_signature" :
         *   "3066023100f63f35d3d1dea67450d9bcbeba95fbe78f5cbf6b407fe8da19ce1ed88b00e2809aa36e2e3a407678f76db046e460efad023100d96c239bc1a4251066bcd2677205b1b75c819488426aea32f7c92919a785741ae48101bf58366aabf7dfca1f536ae568",
         *   "request_type" : "signed_doc_hash",
         *   "exp" : 1610588424,
         *   "iat" : 1610588304
         */
        return new CallbackFirstLegMessage();
    }

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
                                //                                o.setNonce(opNonce.get());
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
                    .ifPresent(errorData::setError);
            extractValue(aQueryParams, "error_description")
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

    private static enum RequestType {
        FirstLeg("user_cert"), SecondLeg("signed_doc_hash"), Error("error");

        private final String name;

        RequestType(String name) {
            this.name = name;
        }
    }
}
