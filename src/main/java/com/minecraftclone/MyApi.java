package com.minecraftclone;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MyApi {
    private static final String BASE_URL = "http://localhost:5051/api";

    public static String login(String email) throws UnsupportedEncodingException {
        return doPost(BASE_URL + "/login?email=" + URLEncoder.encode(email, "UTF-8"));
    }

    public static String register(String email) throws UnsupportedEncodingException {
        return doPost(BASE_URL + "/register?email=" + URLEncoder.encode(email, "UTF-8"));
    }

    private static String doPost(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.connect();
            int code = conn.getResponseCode();
            if (code == 200) {
                return new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            } else {
                return "HTTP_" + code;
            }
        } catch (Exception e) {
            return "ERROR_" + e.getMessage();
        }
    }
}
