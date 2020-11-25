package com.itextpdf.demo.ndi.providers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import play.libs.Json;

import javax.inject.Provider;

public class JavaJsonProvider implements Provider<ObjectMapper> {

    @Override
    public ObjectMapper get() {
        ObjectMapper mapper =
                new ObjectMapper()
                        // enable features and customize the object mapper here ...
                        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Needs to set to Json helper
        Json.setObjectMapper(mapper);

        return mapper;
    }
}
