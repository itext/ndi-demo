package com.itextpdf.adapters.ndi.signing.services;

import com.itextpdf.adapters.ndi.client.INDIClient;
import com.itextpdf.adapters.ndi.client.models.ContainerError;
import com.itextpdf.adapters.ndi.client.models.HashSigningRequest;
import com.itextpdf.adapters.ndi.client.models.InitCallParams;
import com.itextpdf.adapters.ndi.client.models.InitCallResult;
import com.itextpdf.adapters.ndi.client.models.callback.*;
import com.itextpdf.adapters.ndi.signing.NDIDocument;
import com.itextpdf.adapters.ndi.signing.SigningStatus;
import com.itextpdf.adapters.ndi.signing.containers.PreSignContainer;
import com.itextpdf.adapters.ndi.signing.containers.PutSignatureContainer;
import com.itextpdf.adapters.ndi.signing.models.ExpectedCallback;
import com.itextpdf.adapters.ndi.signing.models.Type;
import com.itextpdf.adapters.ndi.signing.services.api.IChainGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.IChallengeCodeGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.INonceGenerator;
import com.itextpdf.container.converters.QrCodeGenerator;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.signatures.*;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NDIDocumentService {

    private static final String hashAlgorithm = DigestAlgorithms.SHA256;

    private static final String encryptionAlgorithm = "ECDSA";

    private final static Logger logger = LoggerFactory.getLogger(NDIDocumentService.class);

    private final INDIClient ndiClient;

    private final IChallengeCodeGenerator codeGenerator;

    private final INonceGenerator nonceGenerator;

    private final TSAClientBouncyCastle tsaClient;

    private final IChainGenerator chainGenerator;

    private final BouncyCastleDigest digest = new BouncyCastleDigest();

    private final QrCodeGenerator qrCodeGenerator;

    private final CallbackValidator callbackValidator;

    @Inject
    public NDIDocumentService(INDIClient ndiClient,
                              IChallengeCodeGenerator codeGenerator,
                              INonceGenerator nonceGenerator,
                              TSAClientBouncyCastle tsaClient,
                              IChainGenerator chainGenerator,
                              QrCodeGenerator qrCodeGenerator,
                              CallbackValidator callbackValidator) {
        this.ndiClient = ndiClient;
        this.codeGenerator = codeGenerator;
        this.nonceGenerator = nonceGenerator;
        this.tsaClient = tsaClient;
        this.chainGenerator = chainGenerator;
        this.qrCodeGenerator = qrCodeGenerator;
        this.callbackValidator = callbackValidator;
    }

    public PdfSignatureAppearance getAppearance() {
        return null;
    }

    private CompletionStage<InitCallResult> initCall(String ndiHint, String aNonce, Type type) {
        InitCallParams payload = new InitCallParams(ndiHint, aNonce, type);
        switch (type) {
            case PN:
                return ndiClient.firstLeg(payload);
            case QR:
                return ndiClient.firstLegQr(payload);
            default:
                throw new RuntimeException("NRE");
        }
    }


    public CompletionStage<NDIDocument> init(byte[] aContent,
                                             String aDocName,
                                             String userId,
                                             String aFieldName,
                                             String aNDIUserHint,
                                             Type containerType) {
        String aNonce = nonceGenerator.newNonce();

        CompletionStage<NDIDocument> flCall = this.initCall(aNDIUserHint, aNonce, containerType)

                                                  .thenApply(response -> {
                                                      NDIDocument document = new NDIDocument(
                                                              response.getSignRef(), aContent, aDocName,
                                                              aFieldName, userId, response.getExpiresAt());
                                                      Optional.ofNullable(response.getQrCodeData())
                                                              .map(qrCodeGenerator::generatePNG)
                                                              .ifPresent(document::setQrCodeData);
                                                      return document;
                                                  });

        flCall.thenAcceptAsync(document -> registerInValidator(document, aNonce));

        flCall.thenAcceptAsync(document -> logger.info(String.format("Document created: sign ref %s, expires at %s",
                                                                     document.getSignatureRef(),
                                                                     document.getExpiresAt()
                                                                             .format(DateTimeFormatter.ofPattern(
                                                                                     "mm:ss")))));
        return flCall;
    }

    private NDIDocument setupCertificate(NDIDocument aDocument, CallbackFirstLegMessage message) {
        String usrCert = message.getUsrCert();
        //todo validate chain
        Certificate[] chain = this.chainGenerator.getChain(usrCert);
        aDocument.setCertificates(chain);

        List<byte[]> ocspList = getOCSP(chain);
        aDocument.setOscp(ocspList);
        return aDocument;
    }

    private List<byte[]> getOCSP(Certificate[] chain) {
        OcspClientBouncyCastle ocspClient = new OcspClientBouncyCastle(null);
        return IntStream.range(0, chain.length - 1)
                        .mapToObj(j -> ocspClient.getEncoded((X509Certificate) chain[j],
                                                             (X509Certificate) chain[j + 1], null))
                        .filter(Objects::nonNull).collect(Collectors.toList());
    }


    private CompletionStage<NDIDocument> prepareToDefferedSigning(NDIDocument aDocument) {
        try {
            ByteArrayInputStream  bis       = new ByteArrayInputStream(aDocument.getSource());
            PdfReader             reader    = new PdfReader(bis);
            ByteArrayOutputStream bos       = new ByteArrayOutputStream();
            PdfSigner             pdfSigner = new PdfSigner(reader, bos, new StampingProperties().useAppendMode());

            Optional.ofNullable(aDocument.getFieldName())
                    .ifPresent(pdfSigner::setFieldName);

            //todo setup appearance

            PdfName          filter        = PdfName.Adobe_PPKLite;
            PdfName          subFilter     = PdfName.Adbe_pkcs7_detached;
            int              estimatedSize = 19500;
            MessageDigest    md            = new BouncyCastleDigest().getMessageDigest(hashAlgorithm);
            PreSignContainer external      = new PreSignContainer(md, filter, subFilter);
            pdfSigner.signExternalContainer(external, estimatedSize);

            aDocument.setPreparedContent(bos.toByteArray());
            aDocument.setHash(external.getDigest());
            updateFieldNameIfNeeded(aDocument, pdfSigner);

            Integer chCode = codeGenerator.newCode();
            aDocument.setChallengeCode(chCode);

            return this.sendDocumentForSigning(aDocument)
                       .thenApply((s) -> aDocument);
        } catch (IOException | GeneralSecurityException e) {
            final String errorMessage = String.format(
                    "Signing process failure on the document preparation step. SignRef: %s",
                    aDocument.getSignatureRef());
            logger.error(errorMessage);

            ContainerError error = new ContainerError();
            error.setErrorDescription(
                    "Signing process failure on the document preparation step. Reason: " + e.getMessage());
            error.setError(ErrorTypes.UNRECOGNIZED_REASON.getValue());
            aDocument.setError(error);

            throw new RuntimeException(e);
        }
    }

    private NDIDocument completeSigning(NDIDocument aDocument, CallbackSecondLegMessage message) {

        String signedHash      = message.getSignature();
        byte[] signedHashBytes = Hex.decode(signedHash);


        PdfPKCS7 sgn = createPkcs7Container(digest, aDocument.getCertificates());
        sgn.setExternalDigest(signedHashBytes, null, encryptionAlgorithm);
        byte[] encodedPKCS7 = sgn.getEncodedPKCS7(aDocument.getHash(),
                                                  PdfSigner.CryptoStandard.CADES,
                                                  tsaClient,
                                                  aDocument.getOscp(),
                                                  null);


        try {
            ByteArrayOutputStream signedOutput = new ByteArrayOutputStream();
            ByteArrayInputStream  is           = new ByteArrayInputStream(aDocument.getPreparedContent());
            PdfReader             reader       = new PdfReader(is);
            PdfDocument           doc          = new PdfDocument(reader);
            PdfSigner.signDeferred(doc,
                                   aDocument.getFieldName(),
                                   signedOutput,
                                   new PutSignatureContainer(encodedPKCS7));
            aDocument.setResult(signedOutput.toByteArray());
            return aDocument;
        } catch (IOException | GeneralSecurityException e) {
            final String errorMessage = String.format("Signing process failure on the final step. SignRef: %s",
                                                      aDocument.getSignatureRef());
            logger.error(errorMessage, e);
            ContainerError error = new ContainerError();
            error.setErrorDescription("Signing process failure on the final step. Reason: " + e.getMessage());
            error.setError(ErrorTypes.UNRECOGNIZED_REASON.getValue());
            aDocument.setError(error);
            throw new RuntimeException(errorMessage, e);
        }
    }

    private NDIDocument addLTVToResult(NDIDocument aDocument) {
        ByteArrayOutputStream      padesLTOutput = new ByteArrayOutputStream();
        final ByteArrayInputStream is            = new ByteArrayInputStream(aDocument.getResult());
        addLtv(is, padesLTOutput, this.tsaClient);
        aDocument.setResult(padesLTOutput.toByteArray());
        return aDocument;
    }

    private void updateFieldNameIfNeeded(NDIDocument aDocument, PdfSigner pdfSigner) {
        if (null == aDocument.getFieldName() || "".equals(aDocument.getFieldName().trim())) {
            aDocument.setFieldName(pdfSigner.getFieldName());
        }
    }

    private String calculateSecondDigest(NDIDocument aDocument) {

        PdfPKCS7 sgn = createPkcs7Container(digest, aDocument.getCertificates());

        byte[] attrBytes = sgn.getAuthenticatedAttributeBytes(aDocument.getHash(),
                                                              PdfSigner.CryptoStandard.CADES,
                                                              aDocument.getOscp(),
                                                              null);

        byte[] attrDigest = getMessageDigest().digest(attrBytes);
        return Hex.toHexString(attrDigest);
    }

    private MessageDigest getMessageDigest() {
        try {
            return digest.getMessageDigest(hashAlgorithm);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private PdfPKCS7 createPkcs7Container(IExternalDigest digest, Certificate[] certificates) {
        try {

            return new PdfPKCS7(null, certificates, hashAlgorithm, null, digest, false);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Pkcs7 container cannot be created: " + e.getMessage());
        }
    }

    private CompletionStage<Void> sendDocumentForSigning(NDIDocument aDocument) {
        logger.info("Hash signing function call");

        HashSigningRequest request = new HashSigningRequest();
        request.setSignRef(aDocument.getSignatureRef());
        request.setDocName(aDocument.getDocName());
        request.setChallengeCode(aDocument.getChallengeCode());

        String hexencodedDigest = calculateSecondDigest(aDocument);
        request.setDocHash(hexencodedDigest);

        String aNonce = nonceGenerator.newNonce();
        request.setNonce(aNonce);

        return ndiClient.secondLeg(request)
                        .thenAccept((v) -> registerInValidator(aDocument, aNonce))
                        .exceptionally(t -> {
                            logger.error(t.getMessage());
                            ContainerError error = new ContainerError();
                            error.setError(ErrorTypes.UNRECOGNIZED_REASON.getValue());
                            error.setErrorDescription(String.format("Second leg error: %s", t.getMessage()));
                            aDocument.setError(error);
                            throw new RuntimeException(t);
                        });
    }

    private void registerInValidator(NDIDocument aDocument, String aNonce) {
        ExpectedCallback ec = new ExpectedCallback(aNonce, aDocument.getSignatureRef(),
                                                   aDocument.getExpiresAt());
        callbackValidator.addToWaitingList(ec);
    }


    private void addLtv(InputStream is, OutputStream outputStream, TSAClientBouncyCastle tsaClient) {
        try {
            PdfReader reader = new PdfReader(is);
            PdfDocument pdfDoc = new PdfDocument(reader, new PdfWriter(outputStream),
                                                 new StampingProperties().useAppendMode());

            LtvVerification v             = new LtvVerification(pdfDoc);
            SignatureUtil   signatureUtil = new SignatureUtil(pdfDoc);

            List<String> names         = signatureUtil.getSignatureNames();
            String       lastSignature = names.get(names.size() - 1);
            v.addVerification(lastSignature, new OcspClientBouncyCastle(null), null,
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


    public CompletionStage<SigningStatus> updateFromCallback(NDIDocument document, NdiCallbackMessage message) {
        logger.info("callback of type: " + message.getClass());

        if (message instanceof CallbackFirstLegMessage) {

            CompletionStage<SigningStatus> res = CompletableFuture.supplyAsync(() -> document)
                                                                  .thenApply((d) -> this.setupCertificate(d,
                                                                                                          (CallbackFirstLegMessage) message))
                                                                  .thenCompose(this::prepareToDefferedSigning)
                                                                  .thenApply(d -> SigningStatus.PREPARED_FOR_SIGNING)
                                                                  .exceptionally(d -> SigningStatus.TERMINATED);
            return res;
        }
        //terminated states below
        if (message instanceof CallbackSecondLegMessage) {
            CompletionStage<SigningStatus> res = CompletableFuture.supplyAsync(() -> document)
                                                                  .thenApply((d) -> this.completeSigning(d,
                                                                                                         (CallbackSecondLegMessage) message))
                                                                  .thenApply(this::addLTVToResult)
                                                                  .thenApply(d -> SigningStatus.COMPLETED)
                                                                  .exceptionally(d -> SigningStatus.TERMINATED);
            return res;

        }
        if (message instanceof CallbackErrorMessage) {
            CallbackErrorMessage errorData = (CallbackErrorMessage) message;
            ContainerError       e         = new ContainerError();
            e.setError(errorData.getError().getValue());
            e.setErrorDescription(errorData.getErrorDescription().getType());
            document.setError(e);

        }
        return CompletableFuture.completedFuture(SigningStatus.TERMINATED);
    }


}
