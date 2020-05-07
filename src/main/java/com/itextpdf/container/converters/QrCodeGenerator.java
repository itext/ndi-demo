package com.itextpdf.container.converters;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class QrCodeGenerator {

    private int size = 300;

    /**
     * Generates  a qrCode with side lengths of {@code aSideLength} from {@code aContentToEncode}
     *
     * @param aContentToEncode into qrCode;
     * @return a byte array representing a qrCode in png format.
     */
    public String generatePNG(String aContentToEncode) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix    bitMatrix    = null;
        try {
            bitMatrix = qrCodeWriter.encode(aContentToEncode, BarcodeFormat.QR_CODE, size, size);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] bytes = pngOutputStream.toByteArray();
            return "data:image/png;base64, " + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException | WriterException e) {
            throw new RuntimeException("Cannot generate qr code ", e);
        }
    }
}
