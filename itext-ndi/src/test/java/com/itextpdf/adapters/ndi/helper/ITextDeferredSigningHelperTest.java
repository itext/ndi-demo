package com.itextpdf.adapters.ndi.helper;

import com.itextpdf.adapters.ndi.helper.models.FirstStepOutput;
import com.itextpdf.adapters.ndi.helper.models.SecondStepInput;
import com.itextpdf.io.util.FileUtil;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class ITextDeferredSigningHelperTest {

    com.itextpdf.adapters.ndi.helper.iTextDeferredSigningHelper helper = new iTextDeferredSigningHelper(null, null);

    private String sourceRoot = "./src/test/resources/";

    private Certificate[] chain;

    private PrivateKeySignature pks;

    private byte[] readFile() throws URISyntaxException, IOException {
        File testFile = new File(sourceRoot + "simple.pdf");
        return Files.readAllBytes(testFile.toPath());
    }

    private void storeInFile(byte[] content, String fileName) throws IOException {
        OutputStream os = FileUtil.getBufferedOutputStream(sourceRoot + fileName);
        os.write(content, 0, content.length);
    }


    @Before
    public void setup()
            throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException,
                   CertificateException {
        Security.addProvider(new BouncyCastleProvider());

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(sourceRoot + "ndikey"), "123456".toCharArray());
        String     alias = ks.aliases().nextElement();
        PrivateKey pk    = (PrivateKey) ks.getKey(alias, "123456".toCharArray());
        chain = ks.getCertificateChain(alias);
        pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, "BC");
    }

    @Test
    public void testPrepareToDeferredSigning() throws IOException, GeneralSecurityException, URISyntaxException {
        FirstStepOutput output = helper.prepareToDeferredSigning(readFile(), null);
        Assert.assertNotNull(output);
        Assert.assertNotNull(output.getDigest());

        System.out.println(Hex.toHexString(output.getDigest()));
        Assert.assertNotNull(output.getPreparedContent());
        Assert.assertEquals("Signature1", output.getFieldName());
        storeInFile(output.getPreparedContent(), "prepared.pdf");


    }

    public PdfPKCS7 verifySignature(SignatureUtil signUtil, String name) throws GeneralSecurityException, IOException {
        System.out.println("Signature covers whole document: " + signUtil.signatureCoversWholeDocument(name));
        System.out.println("Document revision: " + signUtil.getRevision(name) + " of " + signUtil.getTotalRevisions());
        PdfPKCS7 pkcs7 = signUtil.readSignatureData(name);
        System.out.println("Integrity check OK? " + pkcs7.verifySignatureIntegrityAndAuthenticity());
        return pkcs7;
    }


    @Test
    public void testSigning()
            throws GeneralSecurityException, IOException, URISyntaxException {

        final String fieldName = "Signature1";

        FirstStepOutput firstStepOutput = helper.prepareToDeferredSigning(readFile(), fieldName);
        PdfPKCS7        sgn             = helper.createPkcs7Container(new BouncyCastleDigest(), chain);

        byte[] attrBytes = sgn.getAuthenticatedAttributeBytes(firstStepOutput.getDigest(),
                                                              PdfSigner.CryptoStandard.CADES,
                                                              null,
                                                              null);

        byte[] signedDigest = pks.sign(attrBytes);

        SecondStepInput secondStepInput = new SecondStepInput(firstStepOutput.getPreparedContent(),
                                                              firstStepOutput.getFieldName(),
                                                              firstStepOutput.getDigest(),
                                                              chain, signedDigest);


        byte[] signedFile = helper.completeSigning(secondStepInput);
        storeInFile(signedFile, "signed.pdf");


        PdfDocument   document = new PdfDocument(new PdfReader(sourceRoot + "signed.pdf"));
        SignatureUtil util     = new SignatureUtil(document);

        Assert.assertTrue(util.signatureCoversWholeDocument(fieldName));
        Assert.assertEquals(1, util.getRevision(fieldName));
        Assert.assertEquals(1, util.getTotalRevisions());
        PdfPKCS7 pkcs7 = util.readSignatureData(fieldName);
        Assert.assertTrue(pkcs7.verifySignatureIntegrityAndAuthenticity());

    }

    @Test
    public void testCalculateSecondDigest() {
        String s         = "54af74d1a5d85608db2fa19aac06ed77aa2688b5892bac8e97ac31f8702c3a39";
        byte[] hash      = Hex.decode(s);
        byte[] attrBytes = helper.calculateSecondDigest(hash, chain);
        Assert.assertEquals("62c124e211847b2fd33f9e8a7a090cdf99a5fe0777f695825bfa6dcf68a5f346",
                            Hex.toHexString(attrBytes));
    }

    public void testAddLtv() {
    }

}
