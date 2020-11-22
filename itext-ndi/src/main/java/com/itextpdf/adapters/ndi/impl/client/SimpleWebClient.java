package com.itextpdf.adapters.ndi.impl.client;

import com.itextpdf.adapters.ndi.client.IWebClient;
import com.itextpdf.adapters.ndi.client.models.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;


public class SimpleWebClient implements IWebClient {

    private static final Logger logger = LoggerFactory.getLogger(SimpleWebClient.class);


    @Override
    public HttpResponse get(String aUrl, String aAuthHeader) {
        HttpsURLConnection conn = null;
        try {
            URL url = new URL(aUrl);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", aAuthHeader);
            conn.setDoOutput(true);

            return getHttpResponse(conn);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @Override
    public HttpResponse post(String aUrl, String aAuthHeader, Map<String, String> headers, String payload) {
        HttpsURLConnection conn = null;
        try {
            URL url = new URL(aUrl);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", aAuthHeader);
            headers.forEach(conn::setRequestProperty);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(payload);
            os.flush();
            os.close();

            return getHttpResponse(conn);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    private HttpResponse getHttpResponse(HttpURLConnection aConnection) throws IOException {

        int            status = aConnection.getResponseCode();
        InputStream    is     = (isSuccessful(status)) ? aConnection.getInputStream() : aConnection.getErrorStream();
        BufferedReader br     = new BufferedReader(new InputStreamReader(is));
        String         output = br.lines().collect(Collectors.joining());
        br.close();
        logger.info("output "+output);
        return new HttpResponse(status, output);

    }


    private boolean isSuccessful(int status) {
        return status == HTTP_OK || status == HTTP_CREATED;
    }


}
