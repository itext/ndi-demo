package com.itextpdf.demo.ndi.providers;

import com.itextpdf.adapters.ndi.signing.NonceGenerator;

import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class NonceGeneratorProvider implements Provider<NonceGenerator> {

    @Override
    public NonceGenerator get() {
        return new NonceGenerator();
    }
}
