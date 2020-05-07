package com.itextpdf.container.services;

import com.codepoetics.ambivalence.Either;
import com.itextpdf.adapters.ndi.auth.models.Token;
import com.itextpdf.adapters.ndi.client.models.ContainerError;
import com.itextpdf.adapters.ndi.client.models.InitCallResult;
import com.itextpdf.adapters.ndi.signing.PresignResult;
import com.itextpdf.adapters.ndi.signing.models.Type;
import com.itextpdf.file.models.PdfFile;

import java.util.Map;
import java.util.concurrent.CompletionStage;

public interface ISigningService {

    CompletionStage<Either<PresignResult, ContainerError>> receiveChallengeCode(String signRef);

    CompletionStage<Either<PdfFile, ContainerError>> getResult(String signRef);

    CompletionStage<Either<InitCallResult, ContainerError>> createContainer(Type initType, PdfFile fileToSign,
                                                                            Token token);

    CompletionStage<String> getResultDocName(String signRef, String ndiId);

    CompletionStage<Void> sendCallback(Map<String, String[]> aQueryParams);
}
