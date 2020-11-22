package com.itextpdf.demo.ndi.sign.models;

import com.itextpdf.adapters.ndi.impl.signing.services.NDIDocumentService;
import com.itextpdf.adapters.ndi.impl.client.models.callback.NdiCallbackMessage;
import com.itextpdf.adapters.ndi.impl.signing.services.NDIDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;



/**
 * A wrapper for {@link NDIDocument}
 */
public class NdiDocumentWrapper {

    private final CountDownLatch completed = new CountDownLatch(1);

    private final CountDownLatch challengeCodeIsReady = new CountDownLatch(1);

    private final NDIDocument document;

    private final NDIDocumentService service;

    private final Logger logger = LoggerFactory.getLogger(NdiDocumentWrapper.class);

    private final String signatureReference;

    public NdiDocumentWrapper(NDIDocument document, NDIDocumentService service) {
        this.service = service;
        this.document = document;
        this.signatureReference = document.getSignatureRef();
    }


    public String getSignatureReference() {
        return signatureReference;
    }

    private Boolean hasErrors() {
        return Objects.nonNull(document.getError());
    }

    public CompletionStage<NDIDocument> getReadyForSigningObject() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                challengeCodeIsReady.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return document;
        });
    }


    public CompletionStage<NDIDocument> getCompletedObject() {

        return CompletableFuture.supplyAsync(() -> {
            try {
                completed.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return document;
        });
    }


    public NDIDocument getDocument() {
        return document;
    }

    public synchronized CompletionStage<Void> updateFromCallback(NdiCallbackMessage data) {

        return service.updateFromCallback(document, data)
                      .thenAccept(s -> {
                          switch (s.getStatus()) {
                              case COMPLETED:
                                  completed.countDown();
                                  break;
                              case PREPARED_FOR_SIGNING:
                                  challengeCodeIsReady.countDown();
                                  break;
                              case TERMINATED:
                              default:
                                  completed.countDown();
                                  challengeCodeIsReady.countDown();
                                  break;
                          }
                      });


    }
}
