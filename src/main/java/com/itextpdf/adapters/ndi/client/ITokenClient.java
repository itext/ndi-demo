package com.itextpdf.adapters.ndi.client;

import com.itextpdf.adapters.ndi.auth.models.DILoginRequest;
import com.itextpdf.adapters.ndi.auth.models.QrCodeResponse;
import com.itextpdf.adapters.ndi.auth.models.TokenResponse;

import java.util.concurrent.CompletionStage;

public interface ITokenClient {

    String AUTH_DOMAIN = "https://api.sandbox.ndi.gov.sg/api/v1/asp";

    String LOGIN_FORM_URL = AUTH_DOMAIN + "/auth";

    String DI_LOGIN_URL = AUTH_DOMAIN + "/di-auth";

    String TOKEN_ENDPOINT = AUTH_DOMAIN + "/token";

    CompletionStage<TokenResponse> loginDi(DILoginRequest request);




}
