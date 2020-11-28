package com.itextpdf.adapters.ndi.signing.converters;


import com.itextpdf.adapters.ndi.client.models.HashSigningRequest;
import com.itextpdf.adapters.ndi.helper.models.SecondStepInput;
import com.itextpdf.adapters.ndi.signing.NDIDocument;
import com.itextpdf.adapters.ndi.signing.models.FirstStepInput;
import org.bouncycastle.util.encoders.Hex;

public class NDIDocumentConverter {

    public SecondStepInput createSecondStepInput(NDIDocument aDocument, byte[] signedHashBytes) {
        return new SecondStepInput(aDocument.getPreparedContent(),
                                   aDocument.getFieldName(),
                                   aDocument.getHash(),
                                   aDocument.getCertificatesChain(),
                                   aDocument.getOcsp(),
                                   signedHashBytes);
    }

    public FirstStepInput createFirstStepInput(NDIDocument aDocument) {
        FirstStepInput fis = new FirstStepInput();

        fis.setSource(aDocument.getSource());
        fis.setFieldName(aDocument.getFieldName());
        return fis;
    }

    public HashSigningRequest convertToHashSigningRequest(NDIDocument aDocument, String aNonce, byte[] secondDigest) {
        HashSigningRequest request = new HashSigningRequest();
        request.setSignRef(aDocument.getSignatureRef());
        request.setDocName(aDocument.getDocName());
        request.setChallengeCode(aDocument.getChallengeCode());
        String hexencodedDigest = Hex.toHexString(secondDigest);
        request.setDocHash(hexencodedDigest);
        request.setNonce(aNonce);
        return request;
    }
}
