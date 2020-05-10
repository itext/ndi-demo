package com.itextpdf.adapters.ndi.config;

/**
 * Configuration of a NDI instance.
 * Any configuration must implement this interface.
 * The used configuration should be configured in {@link com.itextpdf.demo.ndi.modules.ApplicationModule}
 */
public interface INDIInstanceConfig {

    String getClientId();

    String getClientSecret();


}
