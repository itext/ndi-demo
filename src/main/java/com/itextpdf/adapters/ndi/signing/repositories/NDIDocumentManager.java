package com.itextpdf.adapters.ndi.signing.repositories;

import com.itextpdf.adapters.ndi.signing.NDIDocument;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NDIDocumentManager implements IPdfDocumentNDIStorage {

    ConcurrentHashMap<String, NDIDocument> documents = new ConcurrentHashMap<>();

    @Override
    public Optional<NDIDocument> find(String signRef) {
                return Optional.ofNullable(documents.get(signRef));

    }

    @Override
    public  void remove(String signRef) {
        this.documents.remove(signRef);
    }


    @Override
    public  void save(NDIDocument file) {
        this.documents.put(file.getSignatureRef(), file);
    }
}
