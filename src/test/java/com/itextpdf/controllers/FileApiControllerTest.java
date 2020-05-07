package com.itextpdf.controllers;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.adapters.ndi.auth.services.FakeAuthService;
import com.itextpdf.adapters.ndi.auth.services.IAuthService;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.route;


public class FileApiControllerTest extends WithApplication {

    @Override
    protected Application provideApplication() {

        return new GuiceApplicationBuilder().overrides(bind(IAuthService.class).to(FakeAuthService.class))
                                            .build();
    }

    @Test
    public void testUploadTemplDocument() {
        Http.RequestBuilder request = Helpers.fakeRequest(com.itextpdf.controllers.routes.FileController.uploadTestDocument());
        Result              result  = route(app, request);
        assertEquals(SEE_OTHER, result.status());
    }

    @Test
    public void testUpload() throws IOException {
        File file = getFile();
        Http.MultipartFormData.Part<Source<ByteString, ?>> part =
                new Http.MultipartFormData.FilePart<>(
                        "fileToSign",
                        "ndi-test.pdf",
                        "application/pdf",
                        FileIO.fromPath(file.toPath()));

        Http.RequestBuilder request =
                Helpers.fakeRequest(com.itextpdf.controllers.routes.FileController.upload())
                       .session("connected", "itest4")
                       .bodyMultipart(Collections.singletonList(part), mat);

        Result result  = Helpers.route(app, request);
        String content = Helpers.contentAsString(result);
        System.out.println(result.status());
        System.out.println(content);
        assertEquals(SEE_OTHER, result.status());
        assertNotEquals("",content);
        JsonNode node = Json.parse(content);
        String fileId = node.get("fileId").toString();
        assertNotEquals("", fileId);
    }

    private File getFile() {
        URL url = getClass().getResource("/ndi-test.pdf");
        return new File(url.getFile());
    }
}
