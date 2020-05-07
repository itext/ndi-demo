package com.itextpdf.container;

import com.codepoetics.ambivalence.Either;
import com.itextpdf.adapters.ndi.client.models.InitCallResult;
import com.itextpdf.adapters.ndi.client.models.ContainerError;
import com.itextpdf.adapters.ndi.signing.models.Type;

import java.io.OutputStream;
import java.util.concurrent.CompletionStage;

public interface ISigningWrapper extends ISubscriber {

    CompletionStage<Void> sign();

    CompletionStage<Void> waitForCompletion();

    CompletionStage<Either<OutputStream, ContainerError>> getOutput();

    CompletionStage<Integer> getChallengeCode();

    CompletionStage<InitCallResult> init(Type containerType);

    String getQrCodeData();

    String getSignRef();
}
