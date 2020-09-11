package com.itextpdf.demo.ndi.controllers;

import com.itextpdf.demo.ndi.auth.IAuthService;
import com.itextpdf.demo.ndi.file.PdfFile;
import com.itextpdf.demo.ndi.file.FileService;
import com.itextpdf.io.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static play.mvc.Controller.*;
import static play.mvc.Results.badRequest;

public class FileController {

    public static final String pdfContentType = "application/pdf";

    private final static Logger logger = LoggerFactory.getLogger(FileController.class);

    private final IAuthService authService;

    private final FileService fileService;

    @Inject
    public FileController(IAuthService authService, FileService fileService) {
        this.authService = authService;
        this.fileService = fileService;
    }

    public Result uploadTestDocument() {
        try {
            if (!authService.isAuthorized()) {
                return redirect(com.itextpdf.demo.ndi.controllers.routes.HomeController.appLogin(null));
            }
            String      ndiId        = authService.getSessionUserId();
            String      documentName = "ndi-test.pdf";
            InputStream is           = getClass().getResourceAsStream("/" + documentName);
            byte[]      bytes        = StreamUtil.inputStreamToArray(is);

            PdfFile file = fileService.createFile(bytes, ndiId, documentName);
            return redirect(com.itextpdf.demo.ndi.controllers.routes.FileController.signPage(file.getId()));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

    }


    public Result upload() throws IOException {
        if (!authService.isAuthorized()) {
            return redirect(com.itextpdf.demo.ndi.controllers.routes.HomeController.appLogin(null));
        }
        String                                ndiId      = authService.getSessionUserId();
        Http.MultipartFormData<File>          body       = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> fileToSign = body.getFile("fileToSign");
        if (fileToSign == null) {
            return badRequest("Form does not contain file data");
        }
        if (!pdfContentType.equals(fileToSign.getContentType())) {
            flash("message", "Pdf files only are allowed");
            return redirect(com.itextpdf.demo.ndi.controllers.routes.HomeController.personalPage());
        }
        File    file    = fileToSign.getFile();
        byte[]  content = Files.readAllBytes(file.toPath());
        PdfFile uplFile = new PdfFile(fileToSign.getFilename(), content, ndiId);
        logger.info("file:" + fileToSign.getFile().getAbsolutePath());
        fileService.save(uplFile);
        return redirect(com.itextpdf.demo.ndi.controllers.routes.FileController.signPage(uplFile.getId()));

    }


    /**
     * Returns file as attachment
     *
     * @param aFileId of a needed file
     * @return
     */
    public Result download(String aFileId) {
        try {
            if (!authService.isAuthorized()) {
                return redirect(com.itextpdf.demo.ndi.controllers.routes.HomeController.appLogin(null));
            }
            String  ndiId   = authService.getSessionUserId();
            PdfFile pdfFile = fileService.getFileOrThrow(aFileId, ndiId);
            return ok(pdfFile.getContent()).withHeader("ContentType", pdfContentType)
                                           .withHeader("Content-Disposition",
                                                       "attachment; filename=" + pdfFile.getFileName() + "");
        } catch (FileNotFoundException nfe) {
            return Results.notFound(nfe.getMessage());
        }


    }


    public Result signPage(String aFileId) {
        try {
            logger.info(" sign page for " + aFileId);
            if (!authService.isAuthorized()) {
                return redirect(com.itextpdf.demo.ndi.controllers.routes.HomeController.appLogin(null));
            }
            String  ndiId      = authService.getSessionUserId();
            PdfFile fileToSign = fileService.getFileOrThrow(aFileId, ndiId);
            return ok(com.itextpdf.demo.ndi.views.html.signing.render(fileToSign.getFileName(), ndiId, aFileId));
        } catch (FileNotFoundException nfe) {
            return notFound(nfe.getMessage());
        }
    }


}
