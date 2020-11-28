package com.itextpdf.adapters.ndi.helper.containers;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.ExternalBlankSignatureContainer;
import com.itextpdf.signatures.IExternalSignatureContainer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
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
public class NdiBlankSignatureContainer extends ExternalBlankSignatureContainer {

    private static PdfName filter = PdfName.Adobe_PPKLite;

    private static PdfName subFilter = PdfName.ETSI_CAdES_DETACHED;
//    private static PdfName subFilter = PdfName.Adbe_pkcs7_detached;

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
