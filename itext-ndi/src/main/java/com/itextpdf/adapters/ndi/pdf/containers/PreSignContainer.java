package com.itextpdf.adapters.ndi.pdf.containers;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalSignatureContainer;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * A quite classical and universal implementation of {@link IExternalSignatureContainer} for using on step-1 of the
 * deferred signing process.
 * <p>
 * * PreSignContainer external      = new PreSignContainer(md, filter, subFilter);
 * pdfSigner.signExternalContainer(external, estimatedSize);
 * <p>
 *  The output of the signExternalContainer further can be used as  input for
 * {@link com.itextpdf.signatures.PdfSigner#signDeferred(PdfDocument, String, OutputStream, IExternalSignatureContainer)}
 *  with the {@link SetSignatureContainer}
 */
public class PreSignContainer implements IExternalSignatureContainer {

    private final MessageDigest messageDigest;

    private final PdfName filter;

    private final PdfName subFilter;

    /** the calculated digest*/
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
