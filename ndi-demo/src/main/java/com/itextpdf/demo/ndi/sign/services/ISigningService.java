package com.itextpdf.demo.ndi.sign.services;

import com.codepoetics.ambivalence.Either;
import com.itextpdf.adapters.ndi.signing.models.ContainerError;
import com.itextpdf.demo.ndi.files.PdfFile;
import com.itextpdf.demo.ndi.sign.models.InitializationResult;
import com.itextpdf.demo.ndi.sign.models.PresignResult;

import java.util.Map;
import java.util.concurrent.CompletionStage;

public interface ISigningService {

    CompletionStage<Either<PresignResult, ContainerError>> receiveChallengeCode(String signRef);

    CompletionStage<Either<PdfFile, ContainerError>> getResult(String signRef);

    CompletionStage<Either<InitializationResult, ContainerError>> createContainer(PdfFile fileToSign);

    CompletionStage<Void> sendCallback(Map<String, String[]> aQueryParams);
}
