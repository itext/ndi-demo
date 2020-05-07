package com.itextpdf.adapters.ndi.signing.services;

import com.itextpdf.adapters.ndi.signing.services.api.INonceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.SecureRandom;

@Singleton
public class NonceGenerator implements INonceGenerator {

    private static Logger logger = LoggerFactory.getLogger(NonceGenerator.class);

    @Inject
    public NonceGenerator() {

    }

    @Override
    public String newNonce() {
        SecureRandom  secureRandom  = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            stringBuilder.append(secureRandom.nextInt(10));
        }
        String nonce = stringBuilder.toString();
        nonce = getSHA256Hash(nonce);
        logger.trace("nonce " + nonce);
        return nonce;
    }

    private String getSHA256Hash(String data) {
        String result = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[]        hash   = digest.digest(data.getBytes("UTF-8"));
            return bytesToHex(hash); // make it printable
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private String bytesToHex(byte[] hash) {
        return DatatypeConverter.printHexBinary(hash);
    }
}