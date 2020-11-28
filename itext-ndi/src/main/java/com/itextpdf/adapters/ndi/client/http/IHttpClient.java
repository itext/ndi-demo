package com.itextpdf.adapters.ndi.client.http;


import java.util.Map;

/**
 * Web client which is being used by {@see NDIApiClient} to call API
 */
public interface IHttpClient {

    HttpResponse get(String aUrl, String aAuthHeader);

    HttpResponse post(String aUrl, String aAuthHeader, Map<String, String> headers, String payload);

}
