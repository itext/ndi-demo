package com.itextpdf.adapters.ndi.helper;

import com.itextpdf.adapters.ndi.helper.containers.NdiBlankSignatureContainer;
import com.itextpdf.adapters.ndi.helper.containers.SetSignatureContainer;
import com.itextpdf.adapters.ndi.helper.models.FirstStepOutput;
import com.itextpdf.adapters.ndi.helper.models.SecondStepInput;
import com.itextpdf.adapters.ndi.signing.models.FirstStepInput;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ITextDeferredSigningHelper {

    private static final Logger logger = LoggerFactory.getLogger(ITextDeferredSigningHelper.class);

    /**
     * sha256 - hashing algorithm
     */
    private static final String hashAlgorithm = DigestAlgorithms.SHA256;

    /** Elliptic curve algorithm is being used for keys in NDI DSS*/
    private static final String encryptionAlgorithm = "ECDSA";

    private static final BouncyCastleDigest digest = new BouncyCastleDigest();

    private static final PdfSigner.CryptoStandard cryptoStandard = PdfSigner.CryptoStandard.CADES;

    private int signatureLength = 8192;

    private final ITSAClient tsaClient;

    private final IOcspClient ocspClient;


    public ITextDeferredSigningHelper(ITSAClient tsaClient, IOcspClient ocspClient) {
        this.tsaClient = tsaClient;
        this.ocspClient = ocspClient;
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
        logger.info("prepare for signing " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        FirstStepOutput fso = new FirstStepOutput();

        ByteArrayInputStream  bis       = new ByteArrayInputStream(aFirstStepInput.getSource());
        PdfReader             reader    = new PdfReader(bis);
        ByteArrayOutputStream bos       = new ByteArrayOutputStream();
        PdfSigner             pdfSigner = new PdfSigner(reader, bos, new StampingProperties().useAppendMode());

        Optional.ofNullable(aFirstStepInput.getFieldName()).ifPresent(pdfSigner::setFieldName);

        MessageDigest              md       = this.getMessageDigest();
        NdiBlankSignatureContainer external = new NdiBlankSignatureContainer(md);

        int size = Optional.ofNullable(tsaClient)
                           .map(c -> signatureLength + c.getTokenSizeEstimate())
                           .orElse(signatureLength);
        pdfSigner.signExternalContainer(external, size);
        fso.setPreparedContent(bos.toByteArray());
        fso.setFieldName(pdfSigner.getFieldName());
        fso.setDigest(external.getDocDigest());
        return fso;
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

        PdfPKCS7 sgn = createPkcs7Container(digest, secondStepInput.getCertificateChain());
        sgn.setExternalDigest(secondStepInput.getSignedHash(), null, encryptionAlgorithm);
        byte[] encodedPKCS7 = sgn.getEncodedPKCS7(secondStepInput.getDocumentDigest(),
                                                  cryptoStandard,
                                                  tsaClient,
                                                  null,
                                                  null);


        ByteArrayOutputStream signedOutput = new ByteArrayOutputStream();
        ByteArrayInputStream  is           = new ByteArrayInputStream(secondStepInput.getPreparedContent());
        PdfReader             reader       = new PdfReader(is);
        PdfDocument           doc          = new PdfDocument(reader);
        PdfSigner.signDeferred(doc, secondStepInput.getFieldName(), signedOutput,
                               new SetSignatureContainer(encodedPKCS7));
        return signedOutput.toByteArray();

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
                                                              cryptoStandard,
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


    public byte[] addLtvInfo(byte[] aDocContent, String aSignatureName)
            throws java.io.IOException, GeneralSecurityException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayInputStream  bis = new ByteArrayInputStream(aDocContent);
        PdfDocument document = new PdfDocument(new PdfReader(bis),
                                               new PdfWriter(bos),
                                               new StampingProperties().useAppendMode());
        LtvVerification ltvVerification = new LtvVerification(document);
        ltvVerification.addVerification(aSignatureName,
                                        ocspClient, null,
                                        LtvVerification.CertificateOption.WHOLE_CHAIN,
                                        LtvVerification.Level.OCSP,
                                        LtvVerification.CertificateInclusion.YES);
        ltvVerification.merge();
        document.close();
        return bos.toByteArray();
    }

    public List<byte[]> getOCSP(Certificate[] chain) {

        if (null == this.ocspClient || chain.length == 0) {
            logger.error("chain is empty");
            return Collections.emptyList();
        }
        return IntStream.range(0, chain.length - 1)
                        .mapToObj(j -> ocspClient.getEncoded((X509Certificate) chain[j], (X509Certificate) chain[j + 1],
                                                             null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
    }

    public byte[] addTimestamp(byte[] aDocContent) throws IOException, GeneralSecurityException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayInputStream  bis = new ByteArrayInputStream(aDocContent);
        PdfSigner signer = new PdfSigner(new PdfReader(bis),
                                         bos,
                                         new StampingProperties().useAppendMode());
        signer.timestamp(tsaClient, "timestampSig1");
        return bos.toByteArray();
    }
}
