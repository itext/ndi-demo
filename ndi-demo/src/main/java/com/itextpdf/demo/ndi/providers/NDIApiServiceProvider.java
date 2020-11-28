package com.itextpdf.demo.ndi.providers;

import com.itextpdf.adapters.ndi.client.NDIApiClient;
import com.itextpdf.adapters.ndi.client.http.IHttpClient;
import com.itextpdf.adapters.ndi.config.INDIInstanceConfig;
import com.itextpdf.adapters.ndi.signing.api.INotificationTokenGenerator;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class NDIApiServiceProvider implements Provider<NDIApiClient> {


    private INotificationTokenGenerator notificationTokenGenerator;

    private INDIInstanceConfig config;

    private IHttpClient client;

    @Inject
    public NDIApiServiceProvider(
            INotificationTokenGenerator notificationTokenGenerator,
            INDIInstanceConfig config, IHttpClient client) {
        this.notificationTokenGenerator = notificationTokenGenerator;
        this.config = config;
        this.client = client;
    }

    @Override
    public NDIApiClient get() {

        return new NDIApiClient(config, notificationTokenGenerator, client);
    }
}
