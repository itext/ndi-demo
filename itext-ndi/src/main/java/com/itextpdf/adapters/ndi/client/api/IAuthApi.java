package com.itextpdf.adapters.ndi.client.api;


import com.itextpdf.adapters.ndi.client.models.DILoginParams;
import com.itextpdf.adapters.ndi.client.models.Token;

import java.util.concurrent.CompletionStage;

/**
 * Interface caller  for NDI authorization and authentication api.
 **/
public interface IAuthApi {

    String AUTH_DOMAIN = "https://api.sandbox.ndi.gov.sg/api/v1/asp";

    String LOGIN_FORM_URL = AUTH_DOMAIN + "/auth";

    String DI_LOGIN_URL = AUTH_DOMAIN + "/di-auth";

    String TOKEN_ENDPOINT = AUTH_DOMAIN + "/token";

    CompletionStage<Token> loginDi(DILoginParams loginParams);


}
