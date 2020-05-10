package com.itextpdf.adapters.ndi.models;

import java.util.UUID;

public class PdfFile {

    private final String id;

    private final String fileName;

    private final byte[] content;

    private final String userId;

    public PdfFile(String fileName, byte[] content, String userId) {
        this.fileName = fileName;
        this.content = content;
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public byte[] getContent() {
        return content;
    }

}
