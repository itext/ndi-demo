package com.itextpdf.demo.ndi.providers;

import com.itextpdf.adapters.ndi.impl.signing.services.tsa.FreeTSAClient;
import com.itextpdf.signatures.ITSAClient;

import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class FreeTSAClientProvider implements Provider<ITSAClient> {

    @Override
    public ITSAClient get() {
        return new FreeTSAClient();
    }
}
