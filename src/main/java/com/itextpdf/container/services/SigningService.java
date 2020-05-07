package com.itextpdf.container.services;

import com.codepoetics.ambivalence.Either;
import com.itextpdf.adapters.ndi.auth.models.Token;
import com.itextpdf.adapters.ndi.client.converters.CallbackConverter;
import com.itextpdf.adapters.ndi.client.models.ContainerError;
import com.itextpdf.adapters.ndi.client.models.InitCallResult;
import com.itextpdf.adapters.ndi.client.models.callback.NdiCallbackMessage;
import com.itextpdf.adapters.ndi.signing.NDIDocument;
import com.itextpdf.adapters.ndi.signing.NDIDocumentConverter;
import com.itextpdf.adapters.ndi.signing.NdiDocumentWrapper;
import com.itextpdf.adapters.ndi.signing.PresignResult;
import com.itextpdf.adapters.ndi.signing.exceptions.CallbackValidationException;
import com.itextpdf.adapters.ndi.signing.models.Type;
import com.itextpdf.adapters.ndi.signing.repositories.NDIDocumentWrapperManager;
import com.itextpdf.adapters.ndi.signing.services.CallbackValidator;
import com.itextpdf.adapters.ndi.signing.services.NDIDocumentService;
import com.itextpdf.file.models.PdfFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

public class SigningService implements ISigningService {

    private static final Logger logger = LoggerFactory.getLogger(SigningService.class);

    private final CallbackConverter converter;

    private final NDIDocumentConverter documentConverter;

    private final NDIDocumentWrapperManager wrapperManager;

    private final NDIDocumentService documentService;

    private final CallbackValidator callbackValidator;

    ConcurrentHashMap<String, String> nonceWrapperMap = new ConcurrentHashMap<>();

    @Inject
    public SigningService(CallbackConverter converter,
                          NDIDocumentConverter documentConverter,
                          NDIDocumentWrapperManager wrapperManager,
                          NDIDocumentService documentService,
                          CallbackValidator callbackValidator) {
        this.converter = converter;
        this.documentConverter = documentConverter;
        this.wrapperManager = wrapperManager;
        this.documentService = documentService;
        this.callbackValidator = callbackValidator;
    }


    @Override
    public CompletionStage<Either<InitCallResult, ContainerError>> createContainer(Type initType, PdfFile fileToSign,
                                                                                   Token token) {


        CompletionStage<NDIDocument> docPromise = documentService.init(fileToSign.getContent(),
                                                                       fileToSign.getFileName(),
                                                                       fileToSign.getUserId(),
                                                                       null,
                                                                       token.getId(),
                                                                       initType);


        docPromise.thenApply(d -> new NdiDocumentWrapper(d, documentService))
                  .thenAcceptAsync(wrapperManager::save);
        return docPromise.thenApply(documentConverter::toInitResult);


    }

    @Override
    public CompletionStage<String> getResultDocName(String signRef, String ndiId) {
        try {
            NdiDocumentWrapper wrapper = this.getWrapperOrThrow(signRef);
            return wrapper.getCompletedObject()
                          .thenApply(d -> {
                              String s = d.getDocName().replace(".pdf", "");
                              return String.format("%s-signed_by_%s.pdf", s, d.getNdiHint());
                          });
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletionStage<Void> sendCallback(Map<String, String[]> aQueryParams) {
        NdiCallbackMessage data = converter.convertParamsToCallbackMessage(aQueryParams);
        try {
            callbackValidator.validate(data);
            NdiDocumentWrapper wrapper = getWrapperOrThrow(data.getSignRef());
            return wrapper.updateFromCallback(data);
        } catch (FileNotFoundException | CallbackValidationException e) {
            final String message = "Invalid callback.";
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    private NdiDocumentWrapper getWrapperOrThrow(String signRef) throws FileNotFoundException {
        return this.wrapperManager.find(signRef)
                                  .orElseThrow((() -> new FileNotFoundException(
                                          "Ndi document is not found")));
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
            this.wrapperManager.remove(signRef);
            return wrapper.getCompletedObject()
                          .thenApply(documentConverter::toOutput);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
