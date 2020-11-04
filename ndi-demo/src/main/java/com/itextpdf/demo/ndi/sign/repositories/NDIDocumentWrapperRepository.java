package com.itextpdf.demo.ndi.sign.repositories;

import com.itextpdf.demo.ndi.sign.models.NdiDocumentWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class NDIDocumentWrapperRepository {

    private final Logger logger = LoggerFactory.getLogger(NDIDocumentWrapperRepository.class);

    private final ConcurrentHashMap<String, NdiDocumentWrapper> documents = new ConcurrentHashMap<>();

    @Inject
    public NDIDocumentWrapperRepository() {
    }

    public Optional<NdiDocumentWrapper> find(String signRef) {
        logger.info("keys: "+this.documents.keySet().stream().collect(Collectors.joining(", ")));
        return Optional.ofNullable(documents.get(signRef));

    }

    public void remove(String signRef) {
        this.documents.remove(signRef);
    }


    public void save(NdiDocumentWrapper file) {
        this.documents.put(file.getSignatureReference(), file);
    }


}
