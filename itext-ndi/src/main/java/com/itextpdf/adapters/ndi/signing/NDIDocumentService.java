package com.itextpdf.adapters.ndi.signing;

import com.itextpdf.adapters.ndi.client.api.IHssApiClient;
import com.itextpdf.adapters.ndi.client.models.HashSigningRequest;
import com.itextpdf.adapters.ndi.client.models.callback.*;
import com.itextpdf.adapters.ndi.signing.api.IChainGenerator;
import com.itextpdf.adapters.ndi.signing.api.IChallengeCodeGenerator;
import com.itextpdf.adapters.ndi.signing.api.INonceGenerator;
import com.itextpdf.adapters.ndi.signing.converters.QrCodeGenerator;
import com.itextpdf.adapters.ndi.signing.models.ContainerError;
import com.itextpdf.adapters.ndi.signing.models.SigningStatus;
import com.itextpdf.adapters.ndi.signing.models.Type;
import com.itextpdf.adapters.ndi.signing.models.ExpectedCallback;
import com.itextpdf.adapters.ndi.pdf.models.SecondStepInput;
import com.itextpdf.adapters.ndi.pdf.iTextDeferredSigningHelper;
import com.itextpdf.adapters.ndi.pdf.models.FirstStepOutput;
import com.itextpdf.signatures.IOcspClient;
import com.itextpdf.signatures.ITSAClient;
import com.itextpdf.signatures.PdfSignatureAppearance;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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


    private final static Logger logger = LoggerFactory.getLogger(NDIDocumentService.class);

    private final IHssApiClient ndiApi;

    private final IChallengeCodeGenerator challengeCodeGenerator;

    private final INonceGenerator nonceGenerator;

    private final IChainGenerator chainGenerator;

    private final QrCodeGenerator qrCodeGenerator;

    private final CallbackValidator callbackValidator;

    private final iTextDeferredSigningHelper signingHelper;

    public NDIDocumentService(IHssApiClient ndiApi,
                              IChallengeCodeGenerator challengeCodeGenerator,
                              INonceGenerator nonceGenerator,
                              ITSAClient tsaClient,
                              IOcspClient ocspClient,
                              IChainGenerator chainGenerator,
                              QrCodeGenerator qrCodeGenerator,
                              CallbackValidator callbackValidator) {
        this.ndiApi = ndiApi;
        this.challengeCodeGenerator = challengeCodeGenerator;
        this.nonceGenerator = nonceGenerator;
        this.chainGenerator = chainGenerator;
        this.qrCodeGenerator = qrCodeGenerator;
        this.callbackValidator = callbackValidator;
        this.signingHelper = new iTextDeferredSigningHelper(tsaClient, ocspClient);
    }

    public PdfSignatureAppearance getAppearance() {
        return null;
    }


    /**
     * Initializes a new signing process.
     * <p>
     * Requests the creation of new signing session one of given {@link Type} on NDI API.
     *
     * @param aContent   content of the document to be signed
     * @param aDocName   name of the document to be signed
     * @param userId     id of the user requesting the signing.
     * @param aFieldName name of the signature`s field which will be used for NDI signature
     * @return the new NDIDocument
     */
    public CompletionStage<NDIDocument> init(byte[] aContent, String aDocName, String userId, String aFieldName) {
        String aNonce = nonceGenerator.generate();

        CompletionStage<NDIDocument> flCall;
        flCall = ndiApi.firstLeg(aNonce)
                       .thenApply(response -> {
                           NDIDocument document = new NDIDocument(response.getSignRef(), aContent, aDocName,
                                                                  aFieldName, userId, response.getExpiresAt());
                           Optional.ofNullable(response.getQrCodeData())
                                   .map(qrCodeGenerator::generatePNG)
                                   .ifPresent(document::setQrCode);
                           return document;
                       });

        flCall.thenAccept(document -> registerInValidator(document, aNonce));
        flCall.thenAcceptAsync(document -> logger.info(String.format("Document created: sign ref %s, expires at %s",
                                                                     document.getSignatureRef(),
                                                                     document.getExpiresAt()
                                                                             .format(DateTimeFormatter.ofPattern(
                                                                                     "hh:mm:ss")))));
        return flCall.thenApply(d -> changeStatus(d, SigningStatus.INITIALIZED));
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
            return processFirstCallback(aDocument, (CallbackFirstLegMessage) aMessage);
        }
        //terminated states below
        if (aMessage instanceof CallbackSecondLegMessage) {
            return processSecondCallback(aDocument, (CallbackSecondLegMessage) aMessage);

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

    private CompletionStage<NDIDocument> processSecondCallback(NDIDocument aDocument,
                                                               CallbackSecondLegMessage aMessage) {
        return CompletableFuture.supplyAsync(() -> aDocument)
                                .thenApply((d) -> this.completeSigning(d, aMessage.getSignature()))
                                //add ltv optionally
                                //                                .thenApply(this::addLTVToResult)
                                .thenApply(d -> changeStatus(d, SigningStatus.COMPLETED))
                                .exceptionally(t -> changeStatus(aDocument, SigningStatus.TERMINATED));
    }

    private CompletionStage<NDIDocument> processFirstCallback(NDIDocument aDocument, CallbackFirstLegMessage aMessage) {
        return CompletableFuture.supplyAsync(() -> aDocument)
                                .thenApply((d) -> this.setupCertificate(d, aMessage.getUsrCert()))
                                //populate oscp - optionally
                                .thenApply(this::prepareForDeferredSigning)
                                .thenApply(this::fillinChallengeCode)
                                .thenCompose(d -> this.sendDocumentForSigning(d).thenApply(v -> d))
                                .thenApply(d -> changeStatus(d, SigningStatus.PREPARED_FOR_SIGNING))
                                .exceptionally(t -> changeStatus(aDocument, SigningStatus.TERMINATED));
    }

    private NDIDocument prepareForDeferredSigning(NDIDocument aDocument) {
        try {
            //todo
            FirstStepOutput fso = signingHelper.prepareToDeferredSigning(aDocument.getSource(),
                                                                         aDocument.getFieldName());
            aDocument.setPreparedContent(fso.getPreparedContent());
            aDocument.setHash(fso.getDigest());
            aDocument.setFieldName(fso.getFieldName());
            return aDocument;

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

    /**
     * Changes status of NdiDocument
     *
     * @param aDocument to be updated
     * @param aStatus   new signing status
     * @return
     */
    private NDIDocument changeStatus(NDIDocument aDocument, SigningStatus aStatus) {
        aDocument.setStatus(aStatus);
        return aDocument;
    }

    private NDIDocument setupCertificate(NDIDocument aDocument, String aUserCertificate) {
        Certificate[] chain = this.chainGenerator.getCompleteChain(aUserCertificate);
        aDocument.setCertificatesChain(chain);
        return aDocument;
    }


    private NDIDocument completeSigning(NDIDocument aDocument, String aSignedHash) {
        try {
            byte[] signedHashBytes = Hex.decode(aSignedHash);
            SecondStepInput secondStepInput = new SecondStepInput(aDocument.getPreparedContent(),
                                                                  aDocument.getFieldName(), aDocument.getHash(),
                                                                  aDocument.getCertificatesChain(),
                                                                  signedHashBytes);
            byte[] signedDocument = signingHelper.completeSigning(secondStepInput);
            aDocument.setResult(signedDocument);
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

    private NDIDocument fillinChallengeCode(NDIDocument aDocument) {
        aDocument.setChallengeCode(challengeCodeGenerator.generate());
        return aDocument;
    }


    private NDIDocument addLTVToResult(NDIDocument aDocument) {
        ByteArrayOutputStream      padesLTOutput = new ByteArrayOutputStream();
        final ByteArrayInputStream is            = new ByteArrayInputStream(aDocument.getResult());
        signingHelper.addLtv(is, padesLTOutput);
        aDocument.setResult(padesLTOutput.toByteArray());
        return aDocument;
    }


    private CompletionStage<NDIDocument> sendDocumentForSigning(NDIDocument aDocument) {
        logger.info("Hash signing function call");

        HashSigningRequest request = new HashSigningRequest();
        request.setSignRef(aDocument.getSignatureRef());
        request.setDocName(aDocument.getDocName());
        request.setChallengeCode(aDocument.getChallengeCode());

        byte[] secondDigest = signingHelper.calculateSecondDigest(aDocument.getHash(),
                                                                  aDocument.getCertificatesChain());
        String hexencodedDigest = Hex.toHexString(secondDigest).toUpperCase();
        request.setDocumentSecondDigest(hexencodedDigest);

        String aNonce = nonceGenerator.generate();
        request.setNonce(aNonce);

        return ndiApi.secondLeg(request)
                     .thenAccept((v) -> registerInValidator(aDocument, aNonce))
                     .thenApply((v) -> aDocument)
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


}
