package com.itextpdf.adapters.ndi.signing.repositories;

import com.itextpdf.adapters.ndi.signing.NdiDocumentWrapper;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NDIDocumentWrapperManager {

    private final ConcurrentHashMap<String, NdiDocumentWrapper> documents = new ConcurrentHashMap<>();

    @Inject
    public NDIDocumentWrapperManager() {
    }

    public Optional<NdiDocumentWrapper> find(String signRef) {
        return Optional.ofNullable(documents.get(signRef));

    }

    public void remove(String signRef) {
        this.documents.remove(signRef);
    }


    public void save(NdiDocumentWrapper file) {
        this.documents.put(file.getSignatureReference(), file);
    }
}
