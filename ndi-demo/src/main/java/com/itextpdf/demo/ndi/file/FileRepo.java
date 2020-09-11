package com.itextpdf.demo.ndi.file;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class FileRepo {


    ConcurrentHashMap<String, PdfFile> files = new ConcurrentHashMap<>();

    @Inject
    public FileRepo() {

    }

    public PdfFile find(String id) {
        return files.get(id);
    }

    public void save(PdfFile PdfFile) {
        files.put(key(PdfFile), PdfFile);
    }

    public void remove(String id) {
        files.remove(id);
    }

    public String key(String ndiId, String fileId) {
        return ndiId + "." + fileId;
    }

    public String key(PdfFile file) {
        return key(file.getUserId(), file.getId());
    }
}
