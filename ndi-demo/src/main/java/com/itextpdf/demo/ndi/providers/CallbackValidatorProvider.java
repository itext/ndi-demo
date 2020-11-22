package com.itextpdf.demo.ndi.providers;

import com.itextpdf.adapters.ndi.signing.CallbackValidator;

import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class CallbackValidatorProvider implements Provider<CallbackValidator> {

    @Override
    public CallbackValidator get() {
        return new CallbackValidator();
    }
}
