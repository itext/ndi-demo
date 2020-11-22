package com.itextpdf.demo.ndi.providers;

import com.itextpdf.adapters.ndi.config.INDIInstanceConfig;
import com.itextpdf.adapters.ndi.signing.api.INotificationTokenGenerator;
import com.itextpdf.demo.ndi.client.NDIClientWSImpl;
import play.libs.ws.WSClient;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class WSClientForNDIProvider implements Provider<NDIClientWSImpl> {


    private INotificationTokenGenerator notificationTokenGenerator;

    private INDIInstanceConfig config;

    private WSClient client;

    @Inject
    public WSClientForNDIProvider(
            INotificationTokenGenerator notificationTokenGenerator,
            INDIInstanceConfig config, WSClient client) {
        this.notificationTokenGenerator = notificationTokenGenerator;
        this.config = config;
        this.client = client;
    }

    @Override
    public NDIClientWSImpl get() {
        return new NDIClientWSImpl(config, client, notificationTokenGenerator);
    }
}
