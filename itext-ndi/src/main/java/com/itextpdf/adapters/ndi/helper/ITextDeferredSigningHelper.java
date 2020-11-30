package com.itextpdf.adapters.ndi.helper;

import com.itextpdf.adapters.ndi.helper.models.FirstStepOutput;
import com.itextpdf.adapters.ndi.helper.models.SecondStepInput;
import com.itextpdf.adapters.ndi.signing.models.FirstStepInput;
import com.itextpdf.signatures.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.Certificate;

public class ITextDeferredSigningHelper {

    private static final Logger logger = LoggerFactory.getLogger(ITextDeferredSigningHelper.class);

    /**
     * sha256 - hashing algorithm
     */
    private static final String hashAlgorithm = DigestAlgorithms.SHA256;

    /** Elliptic curve algorithm is being used for keys in NDI DSS*/
    private static final String encryptionAlgorithm = "ECDSA";

    private static final BouncyCastleDigest digest = new BouncyCastleDigest();

    private int signatureLength = 8192;

    public ITextDeferredSigningHelper() {
    }


    /**
     * input:
     * <p>
     * source
     * fieldname
     */
    //prepared content
    //hash
    //fieldname
    public FirstStepOutput prepareToDeferredSigning(FirstStepInput aFirstStepInput)
            throws IOException, GeneralSecurityException {
        return FirstStepOutput.createDummyOutput(aFirstStepInput.getSource());
//        return fso;
    }

    //input
    //byte[] preparedContent
    //byte[] hash
    //oscp
    //tsa

    //output
    //byte[] result

    /**
     * @param secondStepInput
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public byte[] completeSigning(SecondStepInput secondStepInput) throws IOException, GeneralSecurityException {

        return secondStepInput.getPreparedContent();

    }

    public PdfPKCS7 createPkcs7Container(IExternalDigest digest, Certificate[] certificates) {
        try {
            return new PdfPKCS7(null, certificates, hashAlgorithm, null, digest, false);
        } catch (GeneralSecurityException e) {
            logger.error("Pkcs7 container cannot be created: " + e.getMessage());
            throw new RuntimeException("Pkcs7 container cannot be created: " + e.getMessage());
        }
    }

    public byte[] calculateSecondDigest(byte[] aDigest, Certificate[] aCertificates) {

        PdfPKCS7 sgn = createPkcs7Container(digest, aCertificates) ;

        byte[] attrBytes = sgn.getAuthenticatedAttributeBytes(aDigest,
                                                              PdfSigner.CryptoStandard.CADES,
                                                              null,
                                                              null);

        return getMessageDigest().digest(attrBytes);
    }

    private MessageDigest getMessageDigest() {
        try {
            return digest.getMessageDigest(hashAlgorithm);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
