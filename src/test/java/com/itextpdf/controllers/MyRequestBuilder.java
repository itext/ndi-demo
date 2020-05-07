package com.itextpdf.controllers;

import play.mvc.Http.RequestBody;
import play.mvc.Http.RequestBuilder;

public class MyRequestBuilder extends RequestBuilder {

    RequestBuilder requestBuilder;

    public MyRequestBuilder(RequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    public void setBody(RequestBody body, String contentType) {
        super.body(body, contentType);
    }
}
