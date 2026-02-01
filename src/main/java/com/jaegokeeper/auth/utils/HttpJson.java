package com.jaegokeeper.auth.utils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public final class HttpJson {
    private HttpJson() {}

    public static String get(String url, String bearerToken) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (bearerToken != null) conn.setRequestProperty("Authorization", "Bearer " + bearerToken);

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String body = readAll(is);
        if (code < 200 || code >= 300) throw new RuntimeException("HTTP " + code + " " + body);
        return body;
    }

    private static String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }
}
