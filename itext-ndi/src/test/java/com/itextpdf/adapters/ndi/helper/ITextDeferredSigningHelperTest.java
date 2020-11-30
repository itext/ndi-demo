package com.itextpdf.adapters.ndi.helper;

import com.itextpdf.adapters.ndi.helper.models.FirstStepOutput;
import com.itextpdf.adapters.ndi.helper.models.SecondStepInput;
import com.itextpdf.adapters.ndi.signing.ChainFromFileGenerator;
import com.itextpdf.adapters.ndi.signing.models.FirstStepInput;
import com.itextpdf.adapters.ndi.signing.tsa.FreeTSAClient;
import com.itextpdf.io.util.FileUtil;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class ITextDeferredSigningHelperTest {

    ITextDeferredSigningHelper helper = new ITextDeferredSigningHelper();

    private String sourceRoot = "./src/test/resources/";
    private String destRoot = "./src/test/resources/";

    private Certificate[] chain;

    private PrivateKeySignature pks;

    private String usrCert = "-----BEGIN CERTIFICATE-----\n" +
            "MIIGJzCCBA+gAwIBAgIGAXTfM4IBMA0GCSqGSIb3DQEBCwUAMIGaMQswCQYDVQQG\n" +
            "EwJTRzESMBAGA1UECAwJU2luZ2Fwb3JlMRIwEAYDVQQHDAlTaW5nYXBvcmUxIjAg\n" +
            "BgNVBAsMGU5hdGlvbmFsIERpZ2l0YWwgSWRlbnRpdHkxHzAdBgNVBAMMFnNhbmRi\n" +
            "b3guYXBpLm5kaS5nb3Yuc2cxHjAcBgkqhkiG9w0BCQEWD2ljYUB0ZWNoLmdvdi5z\n" +
            "ZzAeFw0yMDA5MzAxMzI4NDhaFw0yMTEwMTAxMzI4NDhaMFcxCzAJBgNVBAYTAlNH\n" +
            "MRIwEAYDVQQIDAlTaW5nYXBvcmUxEDAOBgNVBAoMB2dvdnRlY2gxETAPBgNVBAsM\n" +
            "CHNpbmdwYXNzMQ8wDQYDVQQDDAZpdGVzdDYwWTATBgcqhkjOPQIBBggqhkjOPQMB\n" +
            "BwNCAART6CSi+MveboQgtFbc0HjjeuUYWCwzBgdkV6GNbYu/3UTjhKX5ZofViMkc\n" +
            "10fORjjLTxK7wk/Gj9fRqkZdCFRgo4ICfjCCAnowCQYDVR0TBAIwADAOBgNVHQ8B\n" +
            "Af8EBAMCBeAwHQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsGAQUFBwMEMB8GCWCGSAGG\n" +
            "+EIBDQQSFhB1c2VyIGNlcnRpZmljYXRlMB0GA1UdDgQWBBSfVh//CxvwZY8Nod/Q\n" +
            "ZPquo6PKqDBuBgNVHREEZzBlpGMwYTELMAkGA1UEBhMCU0cxEDAOBgNVBAoMB2dv\n" +
            "dnRlY2gxETAPBgNVBAsMCHNpbmdwYXNzMS0wKwYDVQQDDCRkZjc4OTJmMC0wMzIw\n" +
            "LTExZWItYThiZC0xOTc2MGVkOGYxNzEwHwYDVR0jBBgwFoAUgYhSUufkWYkvOyKF\n" +
            "aaExKi3fyMYwVwYDVR0fBFAwTjAloCOgIYYfaHR0cDovL2NhMS50ZWNoLmdvdi5z\n" +
            "Zy9pY2ExLmNybDAloCOgIYYfaHR0cDovL2NhMi50ZWNoLmdvdi5zZy9pY2ExLmNy\n" +
            "bDCCARIGCCsGAQUFBwEBBIIBBDCCAQAwQwYIKwYBBQUHMAKGN2h0dHA6Ly9vY3Nw\n" +
            "LnNhbmRib3gubmRpLmdvdi5zZy9hcGkvdjEvY2EvY2VydHMvaWNhMS5jcnQwQwYI\n" +
            "KwYBBQUHMAKGN2h0dHA6Ly9vY3NwLnNhbmRib3gubmRpLmdvdi5zZy9hcGkvdjEv\n" +
            "Y2EvY2VydHMvaWNhMi5jcnQwOQYIKwYBBQUHMAGGLWh0dHA6Ly9vY3NwLnNhbmRi\n" +
            "b3gubmRpLmdvdi5zZy9hcGkvdjEvdmEvb2NzcDA5BggrBgEFBQcwAYYtaHR0cDov\n" +
            "L29jc3Auc2FuZGJveC5uZGkuZ292LnNnL2FwaS92MS92YS9vY3NwMA0GCSqGSIb3\n" +
            "DQEBCwUAA4ICAQCxJR9KB+QG0OP+42WpmPrwIpSjV11Bbj/qfhuycC9QTe+hsTfU\n" +
            "jO0Wy+VKXkqIQtOVLIaP/JjFjRQmM9Ma3hk/FLfE4C2TTSCN6vKAqas/v5dONGpq\n" +
            "ggFafXBtrNPQDWWPa/UdPThkXnAXhUrdke8LWofoh2cOufL+u66Nv0wqS9mcfNi8\n" +
            "VJsTylu4GBWdEIItI9Z++jmamHVCOnB0cy/1fi/lXPUdBNwwMopVBGPix8q4dJrJ\n" +
            "WxVYz7ro9sPlXnDWnhsBqN3oQHAFmiH+YhbhJ/ZgJNfhgGPhYxpd8deIlKRI46Aw\n" +
            "ZF4zwxw8V0zwi/sjcdwf3JmeBTWGDeYBFjpr038ESad1dAxXgysVm7nczou24YL4\n" +
            "Ij0ONUFKBR7oWdz07ygImfbriGWd/M4zYbtRXp+TtILLCa+HsCXFarqqwxsIP+UW\n" +
            "iueIv4B8/YtF/yVR9w6bENHIfH1FgvqJRM75NvbhMnXF8Tn/W810le4lmPmbCpuc\n" +
            "6NMNV6Bj1aFDhzryg+ElWvxAEeVT3HpCQzx1ajOd1SCkULI00XDJKBoTnwKDOtI+\n" +
            "2XugbV4l/+Ca7A7bWth8Ixajw/ZBnMrkcd2TUaMFLnPOWFHef8KW7IArgh+0YFjX\n" +
            "sSqtIylAO94/ynQNjOuuZ8nejRbREjqXV4xNY9bCif3ABsfejSIlNrxVBg==\n" +
            "-----END CERTIFICATE-----";

    private byte[] readFile() throws URISyntaxException, IOException {
        File testFile = new File(sourceRoot + "simple.pdf");
        return Files.readAllBytes(testFile.toPath());
    }

    private void storeInFile(byte[] content, String fileName) throws IOException {
        OutputStream os = FileUtil.getBufferedOutputStream(destRoot + fileName);
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

        FirstStepOutput output = helper.prepareToDeferredSigning(createFirstStepInput(null));
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
    @Ignore
    public void testSigning()
            throws GeneralSecurityException, IOException, URISyntaxException {

        final String fieldName = "Signature1";

        FirstStepInput  fis             = createFirstStepInput(fieldName);
        FirstStepOutput firstStepOutput = helper.prepareToDeferredSigning(fis);
        PdfPKCS7        sgn             = helper.createPkcs7Container(new BouncyCastleDigest(), chain);

        byte[] attrBytes = sgn.getAuthenticatedAttributeBytes(firstStepOutput.getDigest(),
                                                              PdfSigner.CryptoStandard.CADES,
                                                              null,
                                                              null);

        byte[] signedDigest = pks.sign(attrBytes);

        SecondStepInput secondStepInput = new SecondStepInput(firstStepOutput.getPreparedContent(),
                                                              firstStepOutput.getFieldName(),
                                                              firstStepOutput.getDigest(),
                                                              chain, null, signedDigest);


        byte[] signedFile = helper.completeSigning(secondStepInput);
        storeInFile(signedFile, "signed.pdf");

        PdfDocument   document = new PdfDocument(new PdfReader(destRoot + "signed.pdf"));
        SignatureUtil util     = new SignatureUtil(document);

        Assert.assertTrue(util.signatureCoversWholeDocument(fieldName));
        Assert.assertEquals(1, util.getRevision(fieldName));
        Assert.assertEquals(1, util.getTotalRevisions());

        PdfPKCS7 pkcs7 = util.readSignatureData(fieldName);
        Assert.assertTrue(pkcs7.verifySignatureIntegrityAndAuthenticity());

    }

    private FirstStepInput createFirstStepInput(String fieldName) throws URISyntaxException, IOException {
        FirstStepInput fis = new FirstStepInput();
        fis.setFieldName(fieldName);
        fis.setSource(readFile());
        return fis;
    }

    @Test
    public void testCalculateSecondDigest() {
        String s         = "54af74d1a5d85608db2fa19aac06ed77aa2688b5892bac8e97ac31f8702c3a39";
        byte[] hash      = Hex.decode(s);
        byte[] secondDigest = helper.calculateSecondDigest(hash, chain);
        //        Assert.assertEquals("62c124e211847b2fd33f9e8a7a090cdf99a5fe0777f695825bfa6dcf68a5f346",
        //                            Hex.toHexString(attrBytes));
    }

//    public List<byte[]> getOcspTest() throws CertificateException {
//        Certificate[] chainGen = new ChainFromFileGenerator().getCompleteChain(this.usrCert);
//        System.out.println("size " + chainGen.length);
//        return helper.getOCSP(chainGen);
//    }

    public List<byte[]> getOCSP(Certificate[] chain) {
        IOcspClient  client = new OcspClientBouncyCastle(null);
        List<byte[]> b      = new ArrayList<>();
        if (chain.length > 1 && client != null) {
            for (int j = 0; j < chain.length - 1; ++j) {
                byte[] ocsp = client.getEncoded((X509Certificate) chain[j], (X509Certificate) chain[j + 1], null);
                if (ocsp != null) {
                    b.add(ocsp);
                }
            }
        }
        return b;
        //        return IntStream.range(0, chain.length - 1)
        //                        .mapToObj(j -> ocspClient.getEncoded((X509Certificate) chain[j], (X509Certificate)
        //                        chain[j + 1],
        //                                                             null))
        //                        .filter(Objects::nonNull)
        //                        .collect(Collectors.toList());
    }

    @Test
    @Ignore
    public void testOcsp() throws CertificateException {
        Certificate[] chainGen = new ChainFromFileGenerator().getCompleteChain(this.usrCert);
        List<byte[]> t1 = getOCSP(chainGen);
        List<byte[]> t2 = getOCSP(chainGen);
        System.out.println("size " + t1.size());
        Assert.assertEquals(t1.size(), t2.size());
        int i;
        for (i = 0; i < t1.size(); i++) {
            Assert.assertEquals(t1.get(i), t2.get(i));
        }
        Assert.assertTrue(t1.equals(t2));
    }

}
