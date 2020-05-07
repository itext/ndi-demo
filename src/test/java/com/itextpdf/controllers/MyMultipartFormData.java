package com.itextpdf.controllers;

import play.mvc.Http.*;

import java.util.List;
import java.util.Map;


public class MyMultipartFormData<A> extends MultipartFormData<A> {
    Map<String, String> form;

    public MyMultipartFormData(Map<String, String> form) {
        this.form = form;
    }

    public Map<String, String[]> asFormUrlEncoded() {
        return null;
    }

    /**
     * Retrieves all file parts.
     *
     * @return the file parts
     */
    public List<FilePart<A>> getFiles() {
        return null;
    }
}