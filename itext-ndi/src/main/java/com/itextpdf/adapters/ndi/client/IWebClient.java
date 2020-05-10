package com.itextpdf.adapters.ndi.client;


import com.itextpdf.adapters.ndi.client.models.HttpResponse;

import java.util.Map;

/**
 * Web client which is being used by {@see NDIApiClientService} to call API
 */
public interface IWebClient {

    HttpResponse get(String aUrl, String aAuthHeader);

    HttpResponse post(String aUrl, String aAuthHeader, Map<String, String> headers, String payload);

}
