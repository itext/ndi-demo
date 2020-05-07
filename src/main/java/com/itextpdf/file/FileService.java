package com.itextpdf.file;

import com.itextpdf.file.models.PdfFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.util.Optional;

@Singleton
public class FileService {


    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final FileRepo fileRepo;

    @Inject
    public FileService(FileRepo fileRepo) {
        this.fileRepo = fileRepo;
    }

    public PdfFile getFileOrThrow(String fileRef, String ndiId) throws FileNotFoundException {

        String fkey = fileRepo.key(ndiId, fileRef);
        return Optional.ofNullable(fileRepo.find(fkey))
                       .map(f -> {
                           logger.info("file info: " + f.toString());
                           return f;
                       })
                       .filter(f -> f.getUserId().equals(ndiId))
                       .orElseThrow(() -> new FileNotFoundException("File: " + fileRef + " is not found"));
    }

    public void save(PdfFile uplFile) {
        fileRepo.save(uplFile);
    }

    /**
     * Creates file and returns its.
     *
     * @param aUserId
     * @param aName
     * @return
     */
    public PdfFile createFile(byte[] content, String aUserId, String aName) {
        PdfFile upFile = new PdfFile(aName, content, aUserId);
        fileRepo.save(upFile);
        logger.info(String.format("file has been created%s", upFile.getId()));
        return upFile;
    }

}