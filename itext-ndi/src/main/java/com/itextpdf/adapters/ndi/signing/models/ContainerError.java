package com.itextpdf.adapters.ndi.signing.models;

import com.itextpdf.adapters.ndi.client.models.callback.ErrorTypes;

public class ContainerError {

    private String error = ErrorTypes.UNRECOGNIZED_REASON.getValue();

    private String errorDescription;


    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }


    @Override
    public String toString() {
        return "ContainerError{" +
                "error='" + error + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }
}
