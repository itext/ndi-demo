package com.itextpdf.adapters.ndi.signing.repositories;


import com.itextpdf.adapters.ndi.signing.NDIDocument;

import java.util.Optional;

public interface IPdfDocumentNDIStorage <T extends NDIDocument> {

    Optional<T> find(String signRef);

    void remove(String signRef);

    void save(T file);
}
