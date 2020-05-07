package com.itextpdf.adapters.ndi.signing.containers;


import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.signatures.IExternalSignatureContainer;

import java.io.InputStream;

public class PutSignatureContainer implements IExternalSignatureContainer {


    private byte[] cmsSignatureContents;

    public PutSignatureContainer(byte[] cmsSignatureContents) {
        this.cmsSignatureContents = cmsSignatureContents;
    }

    public byte[] sign(InputStream docBytes) {
        return cmsSignatureContents;
    }

    public void modifySigningDictionary(PdfDictionary signDic) {
    }
}

