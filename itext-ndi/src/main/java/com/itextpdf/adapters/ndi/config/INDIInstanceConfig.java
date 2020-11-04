package com.itextpdf.adapters.ndi.config;

/**
 * Configuration of a NDI instance.
 * Any configuration must implement this interface.
 */
public interface INDIInstanceConfig {

    String getClientId();

    String getClientSecret();


}
