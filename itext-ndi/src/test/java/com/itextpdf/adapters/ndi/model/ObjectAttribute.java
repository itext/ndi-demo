package com.itextpdf.adapters.ndi.model;

public class ObjectAttribute {

    public String name;

    public String getName() {
        return name;
    }

    public ObjectAttribute getCopy() throws CloneNotSupportedException {
        return  new ObjectAttribute();
    }
}
