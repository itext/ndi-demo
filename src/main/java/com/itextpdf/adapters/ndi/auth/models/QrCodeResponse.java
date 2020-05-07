package com.itextpdf.adapters.ndi.auth.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {
 * "auth_req_id": "itextaws:ozkbdY577eILZBV8",
 * "expires_in": 300,
 * "qr_code": "iVBORw0KGgoAAAANSUhEUgAAAFUAAABVCAAAAAAdnveGAAAEeklEQVR42u2ZwW4cMQxD+f8/zQLZHfk9edBDgdyaAGk26
 * /XYlERSavobX/m/68+Pz9fPL58X/Xz/LMjnZ/Jd/nn3s/7zKt7l2ZUPmJdn7RwgOft+V3x+n0OUu
 * +b75jnAd2We1TjX87Sf92d3rtau55C43vf8B5yeBw8+f911MB0sg1ukXIVodD7mXTuReHB4AH3uMNtP9PDciaRwDQP/z98rB5Bnc4gJ9HPa74
 * /JrlncicjOV1weiIW/9AR64PxGd9CKawubHXwy/zyp
 * /qTwQS9z7nP0A3HC202cnozSpud1VSdZCKieULDP9XXyye3nErgpE2Hqv4rWUw052TW7KrFq1IArCnmgHxTjiJJmerAiJRVB6HquwguwMoE5QS6SQEGdkic2wyRX6BBD1LNwPX8B6OfyKl+kEgDL2jUAMWLrXmGuHnQewkPW6HSk4NkLUA0tDpnoDkwtVMGp8iKmwBvFJB5LSF3edfJzgq/KCVWnpzBKMl61FXCqGACAnqRtXbQTs4sHFFbkGmm7yg6IJwDZys2sKfRpNAwSAfY/YeThK9ocIXz90yr+8yDqiXYtqwSif2ozSisQAs5ulzEldyCm0Kz0KAr/5EWjKkDVWcfBZocPfBGWlqN1Ajb2BLpVPZBCxJRmvtcM1oq6lMLwbNyQDGpcT1VTQ4iVj+8QNDdhqyopl8vfITlLtGb/E8JuiYj51IESiZ/71FBttY1ELKT+Q7IQv0Mw4P2FAJMVcJkkbDTO44oEkMZO2rIUZjG4PLUlAlUuwx26NYsCHAA+OBeW970z67DX/tzhs6A3cCi66KowYjYuRR8TNiq7WnajAnN3EpCQ3AdGiZ0gBXhdDt4WQfKts9M1NupptsswKdkn98Smy12IV6XcRRXGvj6xvcm2BlC19sUPsKBsB1E3ZJ0AZDAjeUDWPlHaq+xX4tNl9GYXtS5omCiJ7C0rmlLahVbb6c50SVeDQxqUJzRnBZxROhUnRuhoueBFY+POUuzNbFJDu/qDF6YXTlE/je44Dpq611y41ubesVX7l7ohTkUQL+V5lBDlF54hDlEWVWXb0kbi5fynhbEKx21Eu7rE8LMVuLTLRxvVNbi7kXuL7C3go2MvTSUZdd4Ta4PTlnkOfTvkf6thuh28BxpUPYaP9gCzJI+0Nq602HeCqQ2kk926oF0be2QPNdQjkamqK+5JgTDt6vStVKSYNbK6PsXJGEzAqn/kv5WHbZNHAOcN1hfjJEmuBzj35GlzCiPmOEghJU+731KOeoy3miB1vGsQEUhn7Vgoqyg7TQvb5T92q7kGU1ejyXbjSh26/+TuC+BFo+4XOn41FfqzNZqlq+nHNSSjke3KwbfLo2Nak8BD7wG3rJmQJg8vM/jQpYNK1yjodjBlJ3XTlRyqogw5lVV0Db7AUPac8NSrzSWzaqjFHrrWocoKUP09H6jIqi/THE7ws0aHvBx72kgwKFqvu+qu6Zsxjma5y3HeuO6uAz4N/uvqvto1ab5zwDPIReqJLXjc4DT//3/rN3f9A9lbVosKXCZ0AAAAAElFTkSuQmCC"
 * }
 */
public class QrCodeResponse {

    @JsonProperty("auth_req_id")
    private String authReqId;

    @JsonProperty("expires_in")
    private String expiresIn;

    /**
     * base 64 string representing qr code 200*200
     */
    @JsonProperty("qr_code")
    private String qrCodeBase64;

    public String getAuthReqId() {
        return authReqId;
    }

    public void setAuthReqId(String authReqId) {
        this.authReqId = authReqId;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }
}
