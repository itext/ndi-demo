package com.itextpdf.adapters.ndi.impl.signing.services;

import com.itextpdf.adapters.ndi.impl.signing.services.containers.exceptions.CallbackValidationException;
import com.itextpdf.adapters.ndi.impl.client.models.callback.CallbackSecondLegMessage;
import com.itextpdf.adapters.ndi.impl.client.models.callback.NdiCallbackMessage;
import com.itextpdf.adapters.ndi.impl.signing.services.models.ExpectedCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * A validator that being used by {@link NDIDocumentService} to register {@link ExpectedCallback}, after positive
 * response from NDI.
 * Can be used to validate callback messages
 */
public class CallbackValidator {

    private final Logger logger = LoggerFactory.getLogger(CallbackValidator.class);

    private CopyOnWriteArrayList<ExpectedCallback> waitingList = new CopyOnWriteArrayList<>();

    /**
     * Validates if the nonce and the signature reference of the given callback is in the waiting list.
     *
     * @param aCallback the message to be checked
     * @throws CallbackValidationException if the nonce can`t be found in the waiting list
     */
    public void validate(NdiCallbackMessage aCallback) throws CallbackValidationException {
        if (aCallback instanceof CallbackSecondLegMessage) {
            logger.info("second step callback");

            //todo NDI is sending incorrect nonce on the Second leg
            validate((CallbackSecondLegMessage) aCallback);
            return;
        }
        waitingList.stream()
                   .filter(c -> c.getNonce().equals(aCallback.getNonce()))
                   .filter(c -> c.getSignRef().equals(aCallback.getSignRef()))
                   .findFirst()
                   .map(this.waitingList::remove)
                   .orElseThrow(() -> toException(aCallback));
    }

    private void validate(CallbackSecondLegMessage aSecondCallback) throws CallbackValidationException {
        waitingList.stream()
                   .filter(c -> c.getSignRef().equals(aSecondCallback.getSignRef()))
                   .findFirst()
                   .map(this.waitingList::remove)
                   .orElseThrow(() -> toException(aSecondCallback));
    }

    private CallbackValidationException toException(NdiCallbackMessage aNdiMessage) {
        final String message = String.format(
                "Unexpected callback message. The nonce %s and signRef %s pair is not registered",
                aNdiMessage.getNonce(), aNdiMessage.getSignRef()
        );
        logger.error(message+waitingList.stream().map(c->c.getNonce()+" "+c.getSignRef()).collect(Collectors.joining()));
        return new CallbackValidationException(
                message);
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