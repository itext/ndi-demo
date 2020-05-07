package com.itextpdf.file;

import com.itextpdf.adapters.ndi.auth.services.FakeAuthService;
import com.itextpdf.adapters.ndi.auth.services.IAuthService;
import com.itextpdf.file.models.PdfFile;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.inject.Bindings.bind;

class FileServiceTest extends WithApplication {

    @Override
    protected Application provideApplication() {

        return new GuiceApplicationBuilder().overrides(bind(IAuthService.class).to(FakeAuthService.class))
                                            .build();
    }

    @Test
    void getFileOrThrow() {
    }

    @Test
    void createFile() throws IOException {
        File            file        = getFile();
        FileInputStream stream      = new FileInputStream(file);
        FileRepo        repo        = new FileRepo();
        FileService     service     = new FileService(repo);
        byte[]          bytes       = Files.readAllBytes(file.toPath());
        PdfFile         createdFile = service.createFile(bytes, "itest5", "ndi-test.pdf");

        assertNotNull(createdFile.getId());

        assertEquals(createdFile.getContent().length, file.length());
    }

    private File getFile() {
        URL url = getClass().getResource("/ndi-test.pdf");
        return new File(url.getFile());
    }

    @Test
    public void dummy() {
        new String(new char[0]).replace('\0', '0');
    }
}