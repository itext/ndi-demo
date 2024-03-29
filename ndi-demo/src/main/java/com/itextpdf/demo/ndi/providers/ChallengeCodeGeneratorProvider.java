package com.itextpdf.demo.ndi.providers;

import com.itextpdf.adapters.ndi.signing.ChallengeCodeGenerator;
import com.itextpdf.adapters.ndi.signing.api.IChallengeCodeGenerator;

import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class ChallengeCodeGeneratorProvider implements Provider<IChallengeCodeGenerator> {

    @Override
    public IChallengeCodeGenerator get() {
        return new ChallengeCodeGenerator();
    }
}
