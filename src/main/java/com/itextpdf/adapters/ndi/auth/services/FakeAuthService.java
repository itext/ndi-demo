package com.itextpdf.adapters.ndi.auth.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.adapters.ndi.auth.TokenConverter;
import com.itextpdf.adapters.ndi.auth.models.Token;
import com.itextpdf.adapters.ndi.auth.models.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;

import javax.inject.Inject;

import static play.mvc.Controller.session;

/**
 * {"id_token":"eyJ0eXAiOiJKV1QiLCJraWQiOiJlNDYwOGVhMC1hYzVkLTExZTktYTk0YS0zYjAwZTc0MjRlZTAiLCJhbGciOiJFUzI1NiJ9
 * .eyJpc3MiOiJodHRwczovL3NhbmRib3guYXBpLm5kaS5nb3Yuc2cvIiwic3ViIjoiMTMzM2Y3NTAtZTlhMS0xMWU5LThmMjItODkzZjQ0OTY1MzU5IiwiYXVkIjoiaXRleHRhd3MiLCJkbiI6Im5kaS0xMzMyODI3NDc2LnVzLWVhc3QtMi5lbGIuYW1hem9uYXdzLmNvbSIsImlhdCI6MTU3MTI5NzQ4MSwiZXhwIjoxNTcxMjk4MDgxLCJub25jZSI6IkYxN0E4RDZBODIwOTg4NzYyODAyNUI2OTE2QkEwRkNBM0JBQkMyNjI1N0JGQzI0RUMxMzJGQzNGNDk4NjJFRkUiLCJzY29wZSI6Im9wZW5pZCJ9.oo1ZJlKpKYfxVg6QJkvuQlHVZaathjFSCuzxC-Nmd_4WtbvKrgi4n7tZ5wh7PW_n8hoPLjyJ4uXZx8DWKR7Oug",
 * "access_token":"1dcbda00-f0b0-11e9-a4fb-a7bcdb1bf37b",
 * "expires_in":600,
 * "token_type":"Bearer"}
 */
public class FakeAuthService implements IAuthService {

    private static final Logger logger = LoggerFactory.getLogger(FakeAuthService.class);

    private TokenConverter converter = new TokenConverter();

    private String jsonString = "{\"id_token" +
            "\":\"eyJ0eXAiOiJKV1QiLCJraWQiOiJlNDYwOGVhMC1hYzVkLTExZTktYTk0YS0zYjAwZTc0MjRlZTAiLCJhbGciOiJFUzI1NiJ9" +
            ".eyJpc3MiOiJodHRwczovL3NhbmRib3guYXBpLm5kaS5nb3Yuc2cvIiwic3ViIjoiMDI1ZjU0YzAtNDg1YS0xMWVhLWI5ZTItMWZiZTY1ZTY0YWU2IiwiYXVkIjoiaXRleHRjbG91ZCIsImRuIjoibmRpLXBvYy5pdGV4dHBkZi5jb20iLCJpYXQiOjE1ODA5MzYzMDAsImV4cCI6MTU4MDkzNjkwMCwibm9uY2UiOiI5QUQzODg1MTQyNEE2QTM0REU4MzM1MUE4M0U5OTM5M0Y2QTRGQjMwRDdBQzc1Nzc4QjAyRTA4N0FBNzkwMDI2Iiwic2NvcGUiOiJvcGVuaWQifQ.dcCY3D5Q3cqbrP4QJP_jTaPFZFHT7GUxl6HGNBATbZncLnvR5L2mFubwLiJFj0h5svv05UhU1Xlys3dC0UdkVw\",\"access_token\":\"3d845280-485a-11ea-b2d7-c1d1390960a0\",\"expires_in\":600,\"token_type\":\"Bearer\"}";

    @Inject
    public FakeAuthService() {
        logger.info("fake token");
    }

    @Override
    public Token getToken(String aNdiId) {

        JsonNode      node     = Json.parse(jsonString);
        TokenResponse response = Json.fromJson(node, TokenResponse.class);
        return converter.fromResponse(response);
    }

    @Override
    public boolean isAuthorized() {
        return true;
    }

    @Override
    public String getSessionUserId() {
        return "itest5";
    }

    @Override
    public void logoff() {

    }

    @Override
    public void login(String aNdiId) {
        session("connected", "itest5");
    }

    public boolean isAuthorized(String aNdiId) {
        return true;
    }

}
