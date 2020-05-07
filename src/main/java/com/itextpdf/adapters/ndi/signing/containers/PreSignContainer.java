package com.itextpdf.adapters.ndi.signing.containers;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalSignatureContainer;

import java.io.InputStream;
import java.security.MessageDigest;

public class PreSignContainer implements IExternalSignatureContainer {

    private final MessageDigest messageDigest;

    private final PdfName filter;

    private final PdfName subFilter;

    private byte[] digest;


    public PreSignContainer(MessageDigest messageDigest, PdfName filter, PdfName subFilter) {
        this.messageDigest = messageDigest;
        this.filter = filter;
        this.subFilter = subFilter;
    }

    public byte[] getDigest() {
        return digest;
    }

    public byte[] sign(InputStream docBytes) {
        try {
            digest = DigestAlgorithms.digest(docBytes, messageDigest);

            return new byte[0];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void modifySigningDictionary(PdfDictionary signDic) {
        signDic.put(PdfName.Filter, filter);
        signDic.put(PdfName.SubFilter, subFilter);
    }
}
