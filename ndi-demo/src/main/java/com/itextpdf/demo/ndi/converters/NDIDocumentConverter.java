package com.itextpdf.demo.ndi.converters;

import com.codepoetics.ambivalence.Either;
import com.itextpdf.adapters.ndi.models.PdfFile;
import com.itextpdf.adapters.ndi.signing.models.PresignResult;
import com.itextpdf.adapters.ndi.signing.models.ContainerError;
import com.itextpdf.adapters.ndi.signing.models.InitializationResult;
import com.itextpdf.adapters.ndi.signing.services.NDIDocument;

import java.util.Optional;

public class NDIDocumentConverter {


    public Either<PdfFile, ContainerError> toOutput(NDIDocument aDocument) {
        return Optional.ofNullable(aDocument.getError())
                       .map(Either::<PdfFile, ContainerError>ofRight)
                       .orElse(Either.ofLeft(createOutputFile(aDocument)));
    }

    private PdfFile createOutputFile(NDIDocument aDocument) {
        return new PdfFile(aDocument.getDocName(), aDocument.getResult(), aDocument.getNdiHint());
    }

    public Either<InitializationResult, ContainerError> toInitResult(NDIDocument aDocument) {
        return Optional.ofNullable(aDocument.getError())
                       .map(Either::<InitializationResult, ContainerError>ofRight)
                       .orElse(Either.ofLeft(createInitCallResult(aDocument)));
    }

    private InitializationResult createInitCallResult(NDIDocument aDocument) {
        return new InitializationResult(aDocument.getSignatureRef(), aDocument.getQrCode());
    }

    public Either<PresignResult, ContainerError> toPresignResult(NDIDocument aDocument) {
        return Optional.ofNullable(aDocument.getError())
                       .map(Either::<PresignResult, ContainerError>ofRight)
                       .orElse(Either.ofLeft(createPreSignResult(aDocument)));
    }

    private PresignResult createPreSignResult(NDIDocument aDocument) {
        return new PresignResult(aDocument.getSignatureRef(), aDocument.getChallengeCode().toString());
    }

}