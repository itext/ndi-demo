package com.itextpdf.demo.ndi.sign;

import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.adapters.ndi.signing.models.ContainerError;
import com.itextpdf.demo.ndi.auth.IAuthService;
import com.itextpdf.demo.ndi.files.FileService;
import com.itextpdf.demo.ndi.files.PdfFile;
import com.itextpdf.demo.ndi.sign.services.ISigningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class SignController extends Controller {

    private final static Logger logger = LoggerFactory.getLogger(SignController.class);

    private final ISigningService iSigningService;

    private final FileService fileService;

    private final IAuthService authService;

    @Inject
    public SignController(ISigningService iSigningService,
                          FileService fileService, IAuthService authService) {
        this.iSigningService = iSigningService;
        this.fileService = fileService;
        this.authService = authService;
    }

    /**
     * Runs a signing process of type {@code type} for a file with id = {@code aFileId};
     *
     * @param aFileId   of source file
     * @return
     */
    public CompletionStage<Result> signInit(String aFileId) {
        try {

            if (!authService.isAuthorized()) {
                return CompletableFuture.completedFuture(
                        redirect(com.itextpdf.demo.ndi.controllers.routes.HomeController.appLogin(null)));
            }
            String ndiId = authService.getSessionUserId();
            logger.info("user: " + ndiId + " calls signing of " + aFileId);
            PdfFile fileToSign = fileService.getFileOrThrow(aFileId, ndiId);

            return iSigningService.createContainer(fileToSign)
                                  .thenApply(e -> e.map(Json::toJson, Json::toJson))
                                  .thenApply(e -> e.join(Results::ok, Results::internalServerError));
        } catch (FileNotFoundException nfe) {
            return CompletableFuture.completedFuture(notFound(nfe.getMessage()));
        } catch (IllegalArgumentException ia) {
            return CompletableFuture.completedFuture(badRequest(ia.getMessage()));
        } catch (CompletionException ia) {
            return CompletableFuture.completedFuture(internalServerError(ia.getMessage()));
        }
    }

    /**
     * Serves ndi callbacks. Expect to get JWT
     */

    public CompletionStage<Result> signJWTCallback() {
        JsonNode jsonBody = request().body().asJson();

        return iSigningService.processCallback(jsonBody)
                              .thenApply((a) -> (Result) Results.ok())
                              .exceptionally((t) -> {
                                  logger.error("Bad response: " + t.getMessage());
                                  return Results.badRequest(t.getMessage());
                              });
    }
    public CompletionStage<Result> signCallbackPost() {
        logger.info("post");
        return CompletableFuture.completedFuture(Results.ok());
    }

    /**
     * Returns the result of the signing.
     * <p>
     * If container is completed successfully, the result file was stored in file service
     * {@link FileService#createFile(byte[], String, String)}
     *
     * @param aSignRef of a signing container
     * @return id of the output file or error.
     */
    public CompletionStage<Result> result(String aSignRef) {
        if (!authService.isAuthorized()) {
            return CompletableFuture.completedFuture(
                    redirect(com.itextpdf.demo.ndi.controllers.routes.HomeController.appLogin(null)));
        }
        return iSigningService.getResult(aSignRef)
                              .thenApply(e -> e.map(f -> {
                                  fileService.save(f);
                                  return f.getId();
                              }, ContainerError::getErrorDescription))
                              .thenApply(e -> e.join(Results::ok, Results::internalServerError));
    }

    /**
     * Returns challenge code for signing container.
     *
     * @param aSignRef id of signing container
     * @return
     */
    public CompletionStage<Result> challengeCode(String aSignRef) {
        if (!authService.isAuthorized()) {
            return CompletableFuture.completedFuture(
                    redirect(com.itextpdf.demo.ndi.controllers.routes.HomeController.appLogin(null)));
        }
        return iSigningService.receiveChallengeCode(aSignRef)
                              .thenApply(e -> e.map(Json::toJson, ContainerError::getErrorDescription))
                              .thenApply(e -> e.join(Results::ok, Results::internalServerError));
    }

}
