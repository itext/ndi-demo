package com.itextpdf.demo.ndi.providers;

import com.itextpdf.adapters.impl.ndi.signing.services.ClientNotificationTokenGenerator;

import com.itextpdf.adapters.ndi.signing.services.api.INotificationTokenGenerator;

import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class ClientNotificationTokenGeneratorProvider implements Provider<INotificationTokenGenerator> {


    @Override
    public INotificationTokenGenerator get() {
        return new ClientNotificationTokenGenerator();
    }
}
