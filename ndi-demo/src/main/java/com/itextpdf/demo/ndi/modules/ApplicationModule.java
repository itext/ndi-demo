package com.itextpdf.demo.ndi.modules;

import com.google.inject.AbstractModule;
import com.itextpdf.adapters.impl.ndi.client.WebClient;
import com.itextpdf.adapters.ndi.client.IWebClient;
import com.itextpdf.adapters.ndi.client.api.IAuthApi;
import com.itextpdf.adapters.ndi.client.api.IHssApiClient;
import com.itextpdf.adapters.ndi.config.INDIInstanceConfig;
import com.itextpdf.adapters.ndi.signing.services.CallbackValidator;
import com.itextpdf.adapters.ndi.signing.services.NDIDocumentService;
import com.itextpdf.adapters.ndi.signing.services.api.IChainGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.IChallengeCodeGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.INonceGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.INotificationTokenGenerator;
import com.itextpdf.demo.ndi.auth.AuthService;
import com.itextpdf.demo.ndi.auth.IAuthService;
import com.itextpdf.demo.ndi.client.config.NDIInstanceConfigAWS;
import com.itextpdf.demo.ndi.providers.*;
import com.itextpdf.demo.ndi.services.ISigningService;
import com.itextpdf.demo.ndi.services.SigningService;
import com.itextpdf.signatures.ITSAClient;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.Environment;
import play.Logger;

public class ApplicationModule extends AbstractModule {


    /**
     * Логгер
     */
    private final static Logger.ALogger LOGGER = Logger.of(ApplicationModule.class);

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ApplicationModule.class);


    private Configuration configuration;


    public ApplicationModule(Environment aEnvironment, Configuration aConfiguration) {
        this.configuration = aConfiguration;
        //runMode = aConfiguration.getString("run.mode", "acc");
    }

    @Override
    protected synchronized void configure() {
        System.out.println(configuration.getString("logger.config"));
        Boolean isMocked = configuration.getBoolean("app.mocked");
        logger.info("isMocked: " + isMocked);

        bind(IWebClient.class).to(WebClient.class).asEagerSingleton();
        bind(IHssApiClient.class).toProvider(NDIApiServiceProvider.class);

        bind(CallbackValidator.class).toProvider(CallbackValidatorProvider.class);
        bind(INotificationTokenGenerator.class).toProvider(ClientNotificationTokenGeneratorProvider.class);
        bind(IChainGenerator.class).toProvider(ChainGeneratorProvider.class);
        bind(IChallengeCodeGenerator.class).toProvider(ChallengeCodeGeneratorProvider.class);
        bind(INonceGenerator.class).toProvider(NonceGeneratorProvider.class);
        bind(ITSAClient.class).toProvider(FreeTSAClientProvider.class);

        bind(NDIDocumentService.class).toProvider(NDIDocumentServiceProvider.class);

        bind(INDIInstanceConfig.class).to(NDIInstanceConfigAWS.class);
        bind(IAuthService.class).to(AuthService.class);
        bind(IAuthApi.class).toProvider(NDIApiServiceProvider.class);
        bind(ISigningService.class).to(SigningService.class);


        logger.info("Application initialization ");
    }


}
