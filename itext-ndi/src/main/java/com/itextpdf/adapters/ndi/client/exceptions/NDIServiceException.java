package com.itextpdf.adapters.ndi.client.exceptions;

public class NDIServiceException extends RuntimeException {


    public NDIServiceException() {
    }

    public NDIServiceException(String message) {
        super(message);
    }

    public NDIServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NDIServiceException(Throwable cause) {
        super(cause);
    }

    public NDIServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
