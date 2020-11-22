package com.itextpdf.adapters.ndi.pdf.containers;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.signatures.IExternalSignatureContainer;

import java.io.InputStream;
import java.security.GeneralSecurityException;

public class SetSignatureInContainer  implements IExternalSignatureContainer {

    private byte[] encodedSignature;

    public SetSignatureInContainer(byte[] encodedSignature) {
        this.encodedSignature = encodedSignature;
    }

    @Override
    public byte[] sign(InputStream data) throws GeneralSecurityException {
        return encodedSignature;
    }

    @Override
    public void modifySigningDictionary(PdfDictionary signDic) {

    }
}
