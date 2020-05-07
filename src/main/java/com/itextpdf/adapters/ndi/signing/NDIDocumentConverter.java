package com.itextpdf.adapters.ndi.signing;

import com.codepoetics.ambivalence.Either;
import com.itextpdf.adapters.ndi.client.models.ContainerError;
import com.itextpdf.adapters.ndi.client.models.InitCallResult;
import com.itextpdf.file.models.PdfFile;

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

    public Either<InitCallResult, ContainerError> toInitResult(NDIDocument aDocument) {
        return Optional.ofNullable(aDocument.getError())
                       .map(Either::<InitCallResult, ContainerError>ofRight)
                       .orElse(Either.ofLeft(createInitCallResult(aDocument)));
    }

    private InitCallResult createInitCallResult(NDIDocument aDocument) {
        InitCallResult r = new InitCallResult();
        //                r.setExpiresAt(d.getExpiresAt());
        r.setQrCodeData(aDocument.getQrCodeData());
        r.setSignRef(aDocument.getSignatureRef());
        return r;
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
