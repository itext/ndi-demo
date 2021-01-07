package com.itextpdf.adapters.ndi;

import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.Serializers;

public class CustomSerializerFactory extends BeanSerializerFactory {

    public CustomSerializerFactory(SerializerFactoryConfig config) {
        super(config);
        SerializerFactoryConfig config1 =  new SerializerFactoryConfig();
        config1.withAdditionalSerializers(new Serializers.Base())
    }


}
