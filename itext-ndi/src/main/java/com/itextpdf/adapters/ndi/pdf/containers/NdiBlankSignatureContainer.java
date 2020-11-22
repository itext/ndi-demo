package com.itextpdf.adapters.ndi.pdf.containers;

import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.ExternalBlankSignatureContainer;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

public class NdiBlankSignatureContainer extends ExternalBlankSignatureContainer {

    private static PdfName filter = PdfName.Adobe_PPKLite;

    private static PdfName subFilter = PdfName.Adbe_pkcs7_detached;

    private byte[] docDigest;

    private MessageDigest messageDigest;


    public NdiBlankSignatureContainer(MessageDigest messageDigest) {
        super(filter, subFilter);
        this.messageDigest = messageDigest;
    }

    public byte[] getDocDigest() {
        return docDigest;
    }

    @Override
    public byte[] sign(InputStream data) throws GeneralSecurityException {

        try {
            docDigest = DigestAlgorithms.digest(data, messageDigest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new GeneralSecurityException("Hashing issue");
        }
        return super.sign(data);
    }
}
