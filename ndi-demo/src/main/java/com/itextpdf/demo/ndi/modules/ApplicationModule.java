package com.itextpdf.demo.ndi.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.itextpdf.adapters.ndi.impl.client.SimpleWebClient;
import com.itextpdf.adapters.ndi.client.IWebClient;
import com.itextpdf.adapters.ndi.client.api.IHssApiClient;
import com.itextpdf.adapters.ndi.config.INDIInstanceConfig;
import com.itextpdf.adapters.ndi.impl.config.NDIInstanceConfig;
import com.itextpdf.adapters.ndi.impl.signing.services.CallbackValidator;
import com.itextpdf.adapters.ndi.impl.signing.services.NDIDocumentService;
import com.itextpdf.adapters.ndi.signing.services.api.IChainGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.IChallengeCodeGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.INonceGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.INotificationTokenGenerator;
import com.itextpdf.demo.ndi.auth.IAuthService;
import com.itextpdf.demo.ndi.auth.LocalAuthService;
import com.itextpdf.demo.ndi.exceptions.ConfigurationError;
import com.itextpdf.demo.ndi.providers.*;
import com.itextpdf.demo.ndi.sign.services.ISigningService;
import com.itextpdf.demo.ndi.sign.services.SigningService;
import com.itextpdf.signatures.IOcspClient;
import com.itextpdf.signatures.ITSAClient;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.Environment;
import play.Logger;

import java.util.Optional;

public class ApplicationModule extends AbstractModule {


    /**
     * Логгер
     */
    private final static Logger.ALogger LOGGER = Logger.of(ApplicationModule.class);

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ApplicationModule.class);


    private Configuration configuration;


    public ApplicationModule(Environment aEnvironment, Configuration aConfiguration) {
        this.configuration = aConfiguration;
    }

    @Override
    protected synchronized void configure() {
        System.out.println(configuration.getString("logger.config"));
bind(ObjectMapper.class).toProvider(JavaJsonProvider.class).asEagerSingleton();
        bind(INDIInstanceConfig.class).to(NDIInstanceConfig.class);
        bind(IWebClient.class).to(SimpleWebClient.class).asEagerSingleton();
        bind(IHssApiClient.class).toProvider(NDIApiServiceProvider.class);
//        bind(IHssApiClient.class).toProvider(WSClientForNDIProvider.class);

        bind(CallbackValidator.class).toProvider(CallbackValidatorProvider.class);
        bind(INotificationTokenGenerator.class).toProvider(ClientNotificationTokenGeneratorProvider.class);
        bind(IChainGenerator.class).toProvider(ChainGeneratorProvider.class);
        bind(IChallengeCodeGenerator.class).toProvider(ChallengeCodeGeneratorProvider.class);
        bind(INonceGenerator.class).toProvider(NonceGeneratorProvider.class);
        bind(ITSAClient.class).toProvider(FreeTSAClientProvider.class);
        bind(IOcspClient.class).toProvider(OSCPClientProvider.class);
        bind(NDIDocumentService.class).toProvider(NDIDocumentServiceProvider.class);


        bind(IAuthService.class).to(LocalAuthService.class);
        bind(ISigningService.class).to(SigningService.class);


        logger.info("Application initialization ");
    }

    @Provides
    NDIInstanceConfig provideConfiguration() {
        String clientId = Optional.ofNullable(configuration.getString("ndi.client.id"))
                                  .orElseThrow(() -> new ConfigurationError("ndi.client.id is not configured"));
        String clientSecret = Optional.ofNullable(configuration.getString("ndi.client.secret"))
                                      .orElseThrow(() -> new ConfigurationError("ndi.client.secret is not configured"));
        return new NDIInstanceConfig(clientId, clientSecret);

    }

}
