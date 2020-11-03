package com.itextpdf.adapters.ndi.signing.services;

import com.itextpdf.adapters.ndi.client.api.IHssApiClient;
import com.itextpdf.adapters.ndi.client.models.HashSigningRequest;
import com.itextpdf.adapters.ndi.client.models.InitCallParams;
import com.itextpdf.adapters.ndi.client.models.InitCallQrResult;
import com.itextpdf.adapters.ndi.client.models.InitCallResult;
import com.itextpdf.adapters.ndi.client.models.callback.*;
import com.itextpdf.adapters.ndi.signing.models.SigningStatus;
import com.itextpdf.adapters.ndi.signing.containers.PreSignContainer;
import com.itextpdf.adapters.ndi.signing.containers.SetSignatureContainer;
import com.itextpdf.adapters.ndi.signing.converters.QrCodeGenerator;
import com.itextpdf.adapters.ndi.signing.models.ContainerError;
import com.itextpdf.adapters.ndi.signing.models.ExpectedCallback;
import com.itextpdf.adapters.ndi.signing.models.Type;
import com.itextpdf.adapters.ndi.signing.services.api.IChainGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.IChallengeCodeGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.INonceGenerator;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.signatures.*;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.itextpdf.adapters.ndi.signing.models.Type.QR;

/**
 * Signing a pdf document via NDI API can be done using this class.
 * <p>
 * For now, a signing using this module consists of 3 calls.
 * 1. Create a new NDIDocument. {@link NDIDocumentService#init}
 * 2. Update NDIdocument using the first callback.
 * .{@link NDIDocumentService#updateFromCallback(NDIDocument, NdiCallbackMessage)}
 * 3. Update NDIdocument using the second callback.
 * {@link NDIDocumentService#updateFromCallback(NDIDocument, NdiCallbackMessage)}
 */
public class NDIDocumentService {

    private static final String hashAlgorithm = DigestAlgorithms.SHA256;

    private static final String encryptionAlgorithm = "ECDSA";

    private final static Logger logger = LoggerFactory.getLogger(NDIDocumentService.class);

    private final IHssApiClient ndiApi;

    private final IChallengeCodeGenerator codeGenerator;

    private final INonceGenerator nonceGenerator;

    private final ITSAClient tsaClient;

    private final IChainGenerator chainGenerator;

    private final QrCodeGenerator qrCodeGenerator;

    private final CallbackValidator callbackValidator;

    private final BouncyCastleDigest digest = new BouncyCastleDigest();

    public NDIDocumentService(IHssApiClient ndiApi,
                              IChallengeCodeGenerator codeGenerator,
                              INonceGenerator nonceGenerator,
                              ITSAClient tsaClient,
                              IChainGenerator chainGenerator,
                              QrCodeGenerator qrCodeGenerator,
                              CallbackValidator callbackValidator) {
        this.ndiApi = ndiApi;
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

    /**
     * Hits either PN or Qr first leg
     *
     * @param aNdiUserTokenId
     * @param aNonce          nonce
     * @param type            the type of Flow
     * @return
     */
    private CompletionStage<InitCallResult> initCall(String aNdiUserTokenId, String aNonce, Type type) {
        InitCallParams payload = new InitCallParams(aNdiUserTokenId, aNonce, type);
        switch (type) {
            case PN:
                return ndiApi.firstLeg(payload);
            case QR:
                return ndiApi.firstLegQr(payload);
            default:
                throw new RuntimeException("NRE");
        }
    }


    /**
     * Initializes a new signing process.
     * <p>
     * Requests the creation of new signing session one of given {@link Type} on NDI API.
     *
     * @param aContent        content of the document to be signed
     * @param aDocName        name of the document to be signed
     * @param userId          id of the user requesting the signing.
     * @param aFieldName      name of the signature`s field which will be used for NDI signature
     * @param aNDIUserTokenId user`s token id.
     * @param aNdiSigningType the type of NDI initialization
     * @return the new NDIDocument
     */
    public CompletionStage<NDIDocument> init(byte[] aContent, String aDocName,
                                             String userId, String aFieldName,
                                             String aNDIUserTokenId, Type aNdiSigningType) {
        String aNonce = nonceGenerator.generate();

        CompletionStage<NDIDocument> flCall = this.initCall(aNDIUserTokenId, aNonce, aNdiSigningType)
                                                  .thenApply(response -> {
                                                      NDIDocument document = new NDIDocument(
                                                              response.getSignRef(), aContent, aDocName,
                                                              aFieldName, userId, response.getExpiresAt());
                                                      if (QR.equals(aNdiSigningType)) {
                                                          String encodedQrCode = Optional.ofNullable(
                                                                  ((InitCallQrResult) response).getQrCodeData())
                                                                                         .map(qrCodeGenerator::generatePNG)
                                                                                         .orElseThrow(
                                                                                                 () -> new IllegalAccessError(
                                                                                                         "Qr code data cannot be empty"));
                                                          document.setQrCode(encodedQrCode);
                                                      }
                                                      ;
                                                      return document;
                                                  });

        flCall.thenAccept(document -> registerInValidator(document, aNonce));

        flCall.thenAcceptAsync(document -> logger.info(String.format("Document created: sign ref %s, expires at %s",
                                                                     document.getSignatureRef(),
                                                                     document.getExpiresAt()
                                                                             .format(DateTimeFormatter.ofPattern(
                                                                                     "mm:ss")))));
        return flCall.thenApply(d -> {
            return changeStatus(d, SigningStatus.INITIALIZED);
        });
    }


    public boolean isTerminated(NDIDocument aDocument) {
        return SigningStatus.TERMINATED.equals(aDocument.getStatus());
    }

    public boolean isActive(NDIDocument aDocument) {
        return !isTerminated(aDocument) && LocalDateTime.now().isBefore(aDocument.getExpiresAt());
    }

    /**
     * Updates the document by the data from the given message.
     *
     * @param aDocument to be updated
     * @param aMessage  the callback message
     * @return the updated document
     */
    public CompletionStage<NDIDocument> updateFromCallback(NDIDocument aDocument, NdiCallbackMessage aMessage) {
        logger.info("callback of type: " + aMessage.getClass());
        if (aMessage instanceof CallbackFirstLegMessage) {
            return applyFirstCallback(aDocument, (CallbackFirstLegMessage) aMessage);
        }
        //terminated states below
        if (aMessage instanceof CallbackSecondLegMessage) {
            return applySecondCallback(aDocument, (CallbackSecondLegMessage) aMessage);

        }
        if (aMessage instanceof CallbackErrorMessage) {
            CallbackErrorMessage errorData = (CallbackErrorMessage) aMessage;
            ContainerError       e         = new ContainerError();
            e.setError(errorData.getError().getValue());
            e.setErrorDescription(errorData.getErrorDescription().getType());
            aDocument.setError(e);
            aDocument.setStatus(SigningStatus.TERMINATED);
        }
        return CompletableFuture.completedFuture(aDocument);
    }

    private CompletionStage<NDIDocument> applySecondCallback(NDIDocument aDocument, CallbackSecondLegMessage aMessage) {
        return CompletableFuture.supplyAsync(() -> aDocument)
                                .thenApply((d) -> this.completeSigning(d, aMessage))
                                .thenApply(this::addLTVToResult)
                                .thenApply(d -> changeStatus(d, SigningStatus.COMPLETED))
                                .exceptionally(t -> changeStatus(aDocument, SigningStatus.TERMINATED));
    }

    private CompletionStage<NDIDocument> applyFirstCallback(NDIDocument aDocument, CallbackFirstLegMessage aMessage) {
        return CompletableFuture.supplyAsync(() -> aDocument)
                                .thenApply((d) -> this.setupCertificate(d, aMessage))
                                .thenCompose(this::prepareToDeferredSigning)
                                .thenApply(d -> changeStatus(d, SigningStatus.PREPARED_FOR_SIGNING))
                                .exceptionally(t -> changeStatus(aDocument, SigningStatus.TERMINATED));
    }

    /**
     * Changes status of NdiDocument
     * @param aDocument to be updated
     * @param status new signing status
     * @return
     */
    private NDIDocument changeStatus(NDIDocument aDocument, SigningStatus status) {
        aDocument.setStatus(status);
        return aDocument;
    }

    private NDIDocument setupCertificate(NDIDocument aDocument, CallbackFirstLegMessage message) {
        String usrCert = message.getUsrCert();
        //todo validate chain
        Certificate[] chain = this.chainGenerator.getCompleteChain(usrCert);
        aDocument.setCertificatesChain(chain);

        List<byte[]> ocspList = getOCSP(chain);
        aDocument.setOscp(ocspList);
        return aDocument;
    }

    private List<byte[]> getOCSP(Certificate[] chain) {
        OcspClientBouncyCastle ocspClient = new OcspClientBouncyCastle(null);
        return IntStream.range(0, chain.length - 1)
                        .mapToObj(j -> ocspClient.getEncoded((X509Certificate) chain[j], (X509Certificate) chain[j + 1], null))
                        .filter(Objects::nonNull).collect(Collectors.toList());
    }


    private CompletionStage<NDIDocument> prepareToDeferredSigning(NDIDocument aDocument) {
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
            MessageDigest    md            = this.getMessageDigest();
            PreSignContainer external      = new PreSignContainer(md, filter, subFilter);
            pdfSigner.signExternalContainer(external, estimatedSize);

            aDocument.setPreparedContent(bos.toByteArray());
            aDocument.setHash(external.getDigest());
            updateFieldNameIfNeeded(aDocument, pdfSigner);

            Integer chCode = codeGenerator.generate();
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


        PdfPKCS7 sgn = createPkcs7Container(digest, aDocument.getCertificatesChain());
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
                                   new SetSignatureContainer(encodedPKCS7));
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
        addLtv(is, padesLTOutput);
        aDocument.setResult(padesLTOutput.toByteArray());
        return aDocument;
    }

    private void updateFieldNameIfNeeded(NDIDocument aDocument, PdfSigner pdfSigner) {
        if (null == aDocument.getFieldName() || "".equals(aDocument.getFieldName().trim())) {
            aDocument.setFieldName(pdfSigner.getFieldName());
        }
    }

    private String calculateSecondDigest(NDIDocument aDocument) {

        PdfPKCS7 sgn = createPkcs7Container(digest, aDocument.getCertificatesChain());

        byte[] attrBytes = sgn.getAuthenticatedAttributeBytes(aDocument.getHash(),
                                                              PdfSigner.CryptoStandard.CADES,
                                                              aDocument.getOscp(),
                                                              null);

        byte[] attrDigest = getMessageDigest().digest(attrBytes);
        return Hex.toHexString(attrDigest).toUpperCase();
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

        String aNonce = nonceGenerator.generate();
        request.setNonce(aNonce);

        return ndiApi.secondLeg(request)
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


    private void addLtv(InputStream is, OutputStream outputStream) {
        try {
            PdfReader reader = new PdfReader(is);
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(is), new PdfWriter(outputStream),
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


}
