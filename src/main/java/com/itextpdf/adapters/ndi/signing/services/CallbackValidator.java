package com.itextpdf.adapters.ndi.signing.services;

import com.itextpdf.adapters.ndi.client.models.callback.NdiCallbackMessage;
import com.itextpdf.adapters.ndi.signing.exceptions.CallbackValidationException;
import com.itextpdf.adapters.ndi.signing.models.ExpectedCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
public class CallbackValidator {

    private final Logger logger = LoggerFactory.getLogger(CallbackValidator.class);


    private CopyOnWriteArrayList<ExpectedCallback> waitingList = new CopyOnWriteArrayList<>();


    @Inject
    public CallbackValidator() {
    }

    public void validate(NdiCallbackMessage message) throws CallbackValidationException {

        waitingList.stream()
                   .filter(c -> c.getNonce().equals(message.getNonce()))
                   .filter(c -> c.getSignRef().equals(message.getSignRef()))
                   .findFirst()
                   .map(this.waitingList::remove)
                   .orElseThrow(() -> toException(message));
    }

    private CallbackValidationException toException(NdiCallbackMessage message) {
        return new CallbackValidationException(
                String.format(
                        "Unexpected callback message. The nonce %s and signRef %s pair is not registered",
                        message.getNonce(), message.getSignRef()
                ));
    }

    void addToWaitingList(ExpectedCallback request) {
        waitingList.add(request);
    }

    void removeFromWaitingList(ExpectedCallback request) {
        waitingList.remove(request);
    }

    void reset() {
        waitingList.clear();
    }
}