package com.itextpdf.adapters.ndi.client.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DILoginRequest {

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("client_notification_token")
    private String clientNotificationToken;


    @JsonProperty("login_hint")
    private String loginHint; //{ndi_id_hash} sha 256

    @JsonProperty("binding_message")
    private String bindingMessage; // loginForm to itext,

    private String nonce;

    @JsonProperty("acr_values")
    private String acrValues = "mod-mf";

    private String display;

    //    private String scope;
    //    private String clientNotificationToken; //generated
    //    private String acrValues; //"mod-mf"
    //    private String display; // "qr-code",
    //            "binding_message" : {your_msg},
    //            "nonce" : {random_nonce}


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getClientNotificationToken() {
        return clientNotificationToken;
    }

    public void setClientNotificationToken(String clientNotificationToken) {
        this.clientNotificationToken = clientNotificationToken;
    }

    public String getLoginHint() {
        return loginHint;
    }

    public void setLoginHint(String loginHint) {
        this.loginHint = loginHint;
    }

    public String getBindingMessage() {
        return bindingMessage;
    }

    public void setBindingMessage(String bindingMessage) {
        this.bindingMessage = bindingMessage;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getAcrValues() {
        return acrValues;
    }

    public void setAcrValues(String acrValues) {
        this.acrValues = acrValues;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }
}
