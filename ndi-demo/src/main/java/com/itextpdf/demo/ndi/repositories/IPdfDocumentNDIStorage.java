package com.itextpdf.demo.ndi.repositories;


import com.itextpdf.adapters.ndi.signing.services.NDIDocument;

import java.util.Optional;

public interface IPdfDocumentNDIStorage <T extends NDIDocument> {

    Optional<T> find(String signRef);

    void remove(String signRef);

    void save(T file);
}
