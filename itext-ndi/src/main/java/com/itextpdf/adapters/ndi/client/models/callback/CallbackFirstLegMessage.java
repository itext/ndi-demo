package com.itextpdf.adapters.ndi.client.models.callback;

/**
 * First leg callback
 * User Certificate Notification claim.
 * <p>
 * Response on a document signing session initiation .
 * <p>
 * *https://stg-id.singpass.gov.sg/docs/doc-signing#user_cert_notification_claims
 * {
 * "sign_ref" : "aa286534-506a-4268-b8b5-57e1930e616b",
 * <p>
 * "request_type" : "user_cert",
 * <p>
 * "exp" : 1609907095,
 * "user_cert" :
 * "MIIBrTCCATKgAwIBAgICA
 * +cwCgYIKoZIzj0EAwMwNTELMAkGA1UEBhMCU0cxDDAKBgNVBAoMA05ESTEYMBYGA1UEAwwPdGVzdEBuZGkuZ292LnNnMB4XDTIwMDYwMzA3MjQyN1oXDTIwMDYwMzA3MjYwN1owTDEtMCsGA1UEBRMkOGFkODA1YmUtNzgzNC00YjY4LWFlYzMtNmM4NTQ3NmZlYjExMRswGQYDVQQDDBJTMDAwMDAwMDkgSm9obiBEb2UwdjAQBgcqhkjOPQIBBgUrgQQAIgNiAARoaQYESAjZS0HJrpcX5mjQfQsODZCK5YmrlWIz1rizww4AXD9o4dtRUdpM9+FAke3ckxYiZc9K2havYuTK//d3OJG9ETyrgElUthWW6GaBdFsWZgDs/zs3FHr0RoM8X/0wCgYIKoZIzj0EAwMDaQAwZgIxAL9YIu3xY2v9bwb/chQgO7Jzbrq8gti2NVahsD7JNd9A+8RJrdyBTFfZR004zV36OQIxAO5YTqXQgrw+5PZWZ6RaewVDJn7DywXQJbey7Zry27ed7hxsYaT9BPA4Io6Yy2hGKQ==",
 * "iat" : 1609906975,
 * <p>
 * "nonce" : "57564e23-4632-4b5f-8164-0c6d92838207"
 * }
 */
public final class CallbackFirstLegMessage extends NdiCallbackMessage {

    private String userCert;

    private String nonce;


    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getUserCert() {
        return userCert;
    }

    public void setUserCert(String userCert) {
        this.userCert = userCert;
    }

}
