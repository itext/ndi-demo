package com.itextpdf.modules;

import com.google.inject.AbstractModule;
import com.itextpdf.adapters.ndi.signing.services.ChainProvider;
import com.itextpdf.adapters.ndi.signing.services.ChallengeCodeGenerator;
import com.itextpdf.adapters.ndi.signing.services.ClientNotificationTokenProvider;
import com.itextpdf.adapters.ndi.signing.services.NonceGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.IChainGenerator;
import com.itextpdf.adapters.ndi.tsa.FreeTSAClient;
import com.itextpdf.adapters.ndi.auth.services.AuthService;
import com.itextpdf.adapters.ndi.auth.services.FakeAuthService;
import com.itextpdf.adapters.ndi.auth.services.IAuthService;
import com.itextpdf.adapters.ndi.client.INDIClient;
import com.itextpdf.adapters.ndi.client.ITokenClient;
import com.itextpdf.adapters.ndi.client.impl.NDIClientWSImpl;
import com.itextpdf.adapters.ndi.config.INDIConfig;
import com.itextpdf.adapters.ndi.config.NDIConfigAWS;
import com.itextpdf.container.services.SigningService;
import com.itextpdf.adapters.ndi.signing.services.api.IChallengeCodeGenerator;
import com.itextpdf.adapters.ndi.signing.services.api.INonceGenerator;
import com.itextpdf.container.services.ISigningService;
import com.itextpdf.adapters.ndi.signing.services.api.ITokenProvider;
import com.itextpdf.signatures.TSAClientBouncyCastle;
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
        //DOMConfigurator.configure(configuration.getString("logger.config"));
        //bind(INDIConfig.class).to(NDIAWSConfig.class);
        bind(INDIConfig.class).to(NDIConfigAWS.class);
        if (!isMocked) {
            bind(IAuthService.class).to(AuthService.class);
        } else {
            bind(IAuthService.class).to(FakeAuthService.class);
        }
        bind(INDIClient.class).to(NDIClientWSImpl.class);
        bind(ITokenClient.class).to(NDIClientWSImpl.class);
        bind(TSAClientBouncyCastle.class).to(FreeTSAClient.class);
        bind(INonceGenerator.class).to(NonceGenerator.class);
        bind(ITokenProvider.class).to(ClientNotificationTokenProvider.class);
        bind(ISigningService.class).to(SigningService.class);
        bind(IChallengeCodeGenerator.class).to(ChallengeCodeGenerator.class);
        bind(IChainGenerator.class).to(ChainProvider.class);

        logger.info("Application initialization ");
    }

}
