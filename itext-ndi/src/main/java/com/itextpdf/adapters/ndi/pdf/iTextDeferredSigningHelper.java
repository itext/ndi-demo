package com.itextpdf.adapters.ndi.pdf;

import com.itextpdf.adapters.ndi.pdf.containers.SetSignatureContainer;
import com.itextpdf.adapters.ndi.pdf.models.FirstStepOutput;
import com.itextpdf.adapters.ndi.pdf.containers.NdiBlankSignatureContainer;
import com.itextpdf.adapters.ndi.pdf.models.SecondStepInput;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class iTextDeferredSigningHelper {

    private static final Logger logger = LoggerFactory.getLogger(iTextDeferredSigningHelper.class);

    private static final String hashAlgorithm = DigestAlgorithms.SHA256;

    /**
     * Elliptic curve algorithm is being used for keys in NDI DSS
     */
    private static final String encryptionAlgorithm = "ECDSA";

    private static final BouncyCastleDigest digest = new BouncyCastleDigest();

    private final ITSAClient tsaClient;

    private final IOcspClient ocspClient;

    private int signatureLength = 12288;

    public iTextDeferredSigningHelper(ITSAClient tsaClient, IOcspClient ocspClient) {
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
    public FirstStepOutput prepareToDeferredSigning(byte[] source, String fieldName)
            throws IOException, GeneralSecurityException {
        FirstStepOutput fso = new FirstStepOutput();

        ByteArrayInputStream  bis       = new ByteArrayInputStream(source);
        PdfReader             reader    = new PdfReader(bis);
        ByteArrayOutputStream bos       = new ByteArrayOutputStream();
        PdfSigner             pdfSigner = new PdfSigner(reader, bos, new StampingProperties().useAppendMode());

        Optional.ofNullable(fieldName).ifPresent(pdfSigner::setFieldName);

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
    //preparedContent
    //hash
    //oscp
    //tsa

    //output
    //result

    /**
     *
     * @param secondStepInput
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public byte[] completeSigning(SecondStepInput secondStepInput) throws IOException, GeneralSecurityException {

        PdfPKCS7 sgn = createPkcs7Container(digest, secondStepInput.getCertificateChain());
        sgn.setExternalDigest(secondStepInput.getaSignedHash(), null, encryptionAlgorithm);
        byte[] encodedPKCS7 = sgn.getEncodedPKCS7(secondStepInput.getDocumentDigest(),
                                                  PdfSigner.CryptoStandard.CADES,
                                                  tsaClient,
                                                  getOCSP(secondStepInput.getCertificateChain()),
                                                  null);


        ByteArrayOutputStream signedOutput = new ByteArrayOutputStream();
        ByteArrayInputStream  is           = new ByteArrayInputStream(secondStepInput.getPreparedContent());
        PdfReader             reader       = new PdfReader(is);
        PdfDocument           doc          = new PdfDocument(reader);
        PdfSigner.signDeferred(doc, secondStepInput.getFieldName(), signedOutput,
                               new SetSignatureContainer(encodedPKCS7));
        return signedOutput.toByteArray();

    }

    private PdfPKCS7 createPkcs7Container(IExternalDigest digest, Certificate[] certificates) {
        try {
            return new PdfPKCS7(null, certificates, hashAlgorithm, null, digest, false);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Pkcs7 container cannot be created: " + e.getMessage());
        }
    }

    public byte[] calculateSecondDigest(byte[] aDigest, Certificate[] aCertificates) {

        PdfPKCS7 sgn = createPkcs7Container(digest, aCertificates);

        byte[] attrBytes = sgn.getAuthenticatedAttributeBytes(aDigest,
                                                              PdfSigner.CryptoStandard.CADES,
                                                              getOCSP(aCertificates),
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


    public void addLtv(InputStream is, OutputStream outputStream) {
        try {
            PdfReader reader = new PdfReader(is);
            PdfDocument pdfDoc = new PdfDocument(reader, new PdfWriter(outputStream),
                                                 new StampingProperties().useAppendMode());

            LtvVerification v             = new LtvVerification(pdfDoc);
            SignatureUtil   signatureUtil = new SignatureUtil(pdfDoc);

            List<String> names         = signatureUtil.getSignatureNames();
            String       lastSignature = names.get(names.size() - 1);

            v.addVerification(lastSignature, ocspClient, null,
                              LtvVerification.CertificateOption.SIGNING_CERTIFICATE,
                              LtvVerification.Level.OCSP,
                              LtvVerification.CertificateInclusion.YES);

            v.merge();
            PdfSigner ps = new PdfSigner(reader, outputStream, new StampingProperties().useAppendMode());
            ps.timestamp(tsaClient, null);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    private List<byte[]> getOCSP(Certificate[] chain) {

        if (null == this.ocspClient || chain.length == 0) {
            return Collections.emptyList();
        }


        return IntStream.range(0, chain.length - 1)
                        .mapToObj(j -> ocspClient.getEncoded((X509Certificate) chain[j], (X509Certificate) chain[j + 1],
                                                             null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
    }
}
