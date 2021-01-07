package com.itextpdf.adapters.ndi.client.models.callback;

import com.itextpdf.adapters.ndi.client.models.callback.common.ErrorSubtypes;
import com.itextpdf.adapters.ndi.client.models.callback.common.ErrorTypes;

/**
 * Callback with an error message.
 * Contains an error description. It appears in the case when any leg was not completed successfully.
 *
 * https://www.ndi-api.gov.sg/assets/lib/trusted-services/ds/specs/hsv2.0.0.yaml.html#operation/clientNotificationEndpoint
 * {
 *   "sign_ref" : "9a1804b2-c246-43b7-a399-4139990da2a2",
 *   "request_type" : "error",
 *   "error_description" : "DSS has failed to send the user cert to DSAP.",
 *   "ndi_request_id" : "d20b23e9-adb9-4a33-b820-c77a67ef4fdc",
 *   "exp" : 1609907094,
 *   "error" : "client_notification_failed",
 *   "iat" : 1609906974
 * }
 *
 */
public final class CallbackErrorMessage extends NdiCallbackMessage {

    private String error;

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

}
