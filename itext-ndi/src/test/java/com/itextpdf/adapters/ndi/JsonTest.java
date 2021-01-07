package com.itextpdf.adapters.ndi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.itextpdf.adapters.ndi.model.Obj;
import com.itextpdf.adapters.ndi.model.ObjectAttribute;
import org.junit.Test;

public class JsonTest {

    @Test
    public void serialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
       String s = mapper.writeValueAsString(new Obj());
       System.out.println(s);
    }

    @Test
    public void name() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(ObjectAttribute.class, MyMixInForIgnoreType.class);
        Obj obj = new Obj();

        String dtoAsString = mapper.writeValueAsString(obj);
    }
    @Test
    public void custom() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule("SimpleModule");
        // simpleModule.addSerializer(new ItemSerializer());
        simpleModule.addSerializer(Item.class, new ItemSerializer());
        mapper.registerModule(simpleModule);

        mapper.addMixIn(ObjectAttribute.class, MyMixInForIgnoreType.class);
        Obj obj = new Obj();

        String dtoAsString = mapper.writeValueAsString(obj);
    }
}
