package com.itextpdf.adapters.ndi.client.models.callback;

import com.itextpdf.adapters.ndi.client.models.callback.common.ErrorSubtypes;
import com.itextpdf.adapters.ndi.client.models.callback.common.ErrorTypes;

/**
 * Callback with an error message.
 * Contains an error description. It appears in the case when any leg was not completed successfully.
 *
 * https://www.ndi-api.gov.sg/assets/lib/trusted-services/ds/specs/hsv2.0.0.yaml.html#operation/clientNotificationEndpoint
 *  {
 * "error": "invalid_request",
 * "error_description": "user_cancelled",
 * "sign_ref": "string",
 * "nonce": "string"
 * }
 */
public final class CallbackErrorMessage extends NdiCallbackMessage {

    private ErrorTypes error;

    private ErrorSubtypes errorDescription;

    public ErrorTypes getError() {
        return error;
    }

    public void setError(ErrorTypes error) {
        this.error = error;
    }

    public ErrorSubtypes getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(ErrorSubtypes errorDescription) {
        this.errorDescription = errorDescription;
    }

}
