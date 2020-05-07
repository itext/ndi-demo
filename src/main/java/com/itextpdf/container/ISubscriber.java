package com.itextpdf.container;

import com.itextpdf.adapters.ndi.client.models.callback.NdiCallbackMessage;

public interface ISubscriber {

    void notify(NdiCallbackMessage data);
}
