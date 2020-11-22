package com.itextpdf.demo.ndi.sign.services;

import com.codepoetics.ambivalence.Either;
import com.itextpdf.adapters.ndi.impl.client.converters.CallbackConverter;
import com.itextpdf.adapters.ndi.impl.client.models.callback.NdiCallbackMessage;
import com.itextpdf.demo.ndi.files.PdfFile;
import com.itextpdf.adapters.ndi.impl.signing.services.containers.exceptions.CallbackValidationException;
import com.itextpdf.adapters.ndi.impl.signing.services.models.ContainerError;
import com.itextpdf.adapters.ndi.impl.signing.services.models.InitializationResult;
import com.itextpdf.adapters.ndi.impl.signing.services.models.PresignResult;
import com.itextpdf.adapters.ndi.impl.signing.services.CallbackValidator;
import com.itextpdf.adapters.ndi.impl.signing.services.NDIDocument;
import com.itextpdf.adapters.ndi.impl.signing.services.NDIDocumentService;
import com.itextpdf.demo.ndi.sign.converters.NDIDocumentConverter;
import com.itextpdf.demo.ndi.sign.models.NdiDocumentWrapper;
import com.itextpdf.demo.ndi.sign.repositories.NDIDocumentWrapperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SigningService implements ISigningService {

    private static final Logger logger = LoggerFactory.getLogger(SigningService.class);

    private final CallbackConverter converter;

    private final NDIDocumentConverter documentConverter;

    private final NDIDocumentWrapperRepository wrapperManager;

    private final NDIDocumentService documentService;

    private final CallbackValidator callbackValidator;


    @Inject
    public SigningService(CallbackConverter converter,
                          NDIDocumentConverter documentConverter,
                          NDIDocumentWrapperRepository wrapperManager,
                          NDIDocumentService documentService,
                          CallbackValidator callbackValidator) {
        this.converter = converter;
        this.documentConverter = documentConverter;
        this.wrapperManager = wrapperManager;
        this.documentService = documentService;
        this.callbackValidator = callbackValidator;
    }


    @Override
    public CompletionStage<Either<InitializationResult, ContainerError>> createContainer(PdfFile fileToSign) {


        CompletionStage<NDIDocument> docPromise = documentService.init(fileToSign.getContent(),
                                                                       fileToSign.getFileName(),
                                                                       fileToSign.getUserId(),
                                                                       null
        );

        docPromise.thenApply(d -> new NdiDocumentWrapper(d, documentService))
                  .thenAcceptAsync(wrapperManager::save);
        return docPromise.thenApply(documentConverter::toInitResult);


    }

    @Override
    public CompletionStage<Void> sendCallback(Map<String, String[]> aQueryParams) {
        NdiCallbackMessage data = converter.convertParamsToCallbackMessage(aQueryParams);

        try {
            try {
                callbackValidator.validate(data);
            } catch (CallbackValidationException e) {
                logger.error(e.getMessage());
            }

            NdiDocumentWrapper wrapper = getWrapperOrThrow(data.getSignRef());
            return CompletableFuture.runAsync(() -> wrapper.updateFromCallback(data));
        } catch (FileNotFoundException e) {
            final String message = "Invalid callback.";
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    private NdiDocumentWrapper getWrapperOrThrow(String signRef) throws FileNotFoundException {
        return this.wrapperManager.find(signRef)
                                  .orElseThrow((() -> new FileNotFoundException(
                                          "Ndi document is not found: " + signRef)));
    }

    @Override
    public CompletionStage<Either<PresignResult, ContainerError>> receiveChallengeCode(String signRef) {
        try {
            NdiDocumentWrapper wrapper = this.getWrapperOrThrow(signRef);
            return wrapper.getReadyForSigningObject()
                          .thenApply(documentConverter::toPresignResult);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletionStage<Either<PdfFile, ContainerError>> getResult(String signRef) {
        try {
            NdiDocumentWrapper wrapper = this.getWrapperOrThrow(signRef);

            CompletionStage<NDIDocument> document = wrapper.getCompletedObject();
            document.thenApply(NDIDocument::getSignatureRef).thenAccept(this.wrapperManager::remove);
            return document.thenApply(documentConverter::toOutput);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
