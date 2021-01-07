package com.itextpdf.adapters.ndi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.itextpdf.adapters.ndi.model.ObjectAttribute;

import java.io.IOException;

public class CustomSerializer extends StdSerializer<ObjectAttribute> {

    public CustomSerializer() {
        this(null);
    }

    public CustomSerializer(Class<ObjectAttribute> t) {
        super(t);
    }

    @Override
    public void serialize(
            ObjectAttribute attribute,
            JsonGenerator generator,
            SerializerProvider provider)
            throws IOException, JsonProcessingException {

        generator.writeObject(attribute.getName());
    }
}

