package com.itextpdf.adapters.ndi.helper.containers;


import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.signatures.IExternalSignatureContainer;

import java.io.InputStream;

/**
 * An external container that supposed to be used for putting CMS encoded signature content in the prepared by
 * {@link NdiBlankSignatureContainer} pdf document
 */
public class SetSignatureContainer implements IExternalSignatureContainer {


    private byte[] cmsSignatureContents;

    public SetSignatureContainer(byte[] cmsSignatureContents) {
        this.cmsSignatureContents = cmsSignatureContents;
    }

    public byte[] sign(InputStream docBytes) {
        return cmsSignatureContents;
    }

    public void modifySigningDictionary(PdfDictionary signDic) {
    }
}

