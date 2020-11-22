package com.itextpdf.adapters.ndi.signing;

import com.itextpdf.adapters.ndi.signing.api.INonceGenerator;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * The default  implementation of INonceGenerator
 */
public class NonceGenerator implements INonceGenerator {

    private static Logger logger = LoggerFactory.getLogger(NonceGenerator.class);


    @Override
    public String generate() {
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

    /**
     * Calculates sha256 hash of the given input string
     * @param aInput
     * @return
     */
    private String getSHA256Hash(String aInput) {
        String result = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[]        hash   = digest.digest(aInput.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash); // make it printable
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private String bytesToHex(byte[] hash) {
        return Hex.toHexString(hash);
    }
}