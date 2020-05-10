package com.itextpdf.demo.ndi.providers;

import com.itextpdf.adapters.ndi.client.api.IHssApiClient;
import com.itextpdf.adapters.ndi.signing.converters.QrCodeGenerator;
import com.itextpdf.adapters.ndi.signing.services.CallbackValidator;
import com.itextpdf.adapters.ndi.signing.services.NDIDocumentService;
import com.itextpdf.adapters.ndi.signing.services.api.IChainGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.IChallengeCodeGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.INonceGenerator;
import com.itextpdf.signatures.ITSAClient;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class NDIDocumentServiceProvider implements Provider<NDIDocumentService> {


    private final IHssApiClient ndiClient;

    private final IChallengeCodeGenerator codeGenerator;

    private final INonceGenerator nonceGenerator;

    private final ITSAClient tsaClient;

    private final IChainGenerator chainGenerator;

    private final QrCodeGenerator qrCodeGenerator;

    private final CallbackValidator callbackValidator;

    @Inject
    public NDIDocumentServiceProvider(IHssApiClient ndiClient,
                                      IChallengeCodeGenerator codeGenerator,
                                      INonceGenerator nonceGenerator,
                                      ITSAClient tsaClient,
                                      IChainGenerator chainGenerator,
                                      QrCodeGenerator qrCodeGenerator,
                                      CallbackValidator callbackValidator) {
        this.ndiClient = ndiClient;
        this.codeGenerator = codeGenerator;
        this.nonceGenerator = nonceGenerator;
        this.tsaClient = tsaClient;
        this.chainGenerator = chainGenerator;
        this.qrCodeGenerator = qrCodeGenerator;
        this.callbackValidator = callbackValidator;
    }

    @Override
    public NDIDocumentService get() {
        return new NDIDocumentService(ndiClient, codeGenerator, nonceGenerator, tsaClient,
                                      chainGenerator, qrCodeGenerator, callbackValidator);
    }
}
