package org.example.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import org.example.utils.GoogleOAuthConfig;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class GoogleOAuthService {

    public CompletableFuture<GoogleUserInfo> startOAuthFlow() {
        CompletableFuture<GoogleUserInfo> future = new CompletableFuture<>();

        try {
            int port = GoogleOAuthConfig.CALLBACK_PORT;
            String redirectUri = GoogleOAuthConfig.REDIRECT_URI;

            String authUrl = GoogleOAuthConfig.AUTH_URL
                    + "?client_id="    + URLEncoder.encode(GoogleOAuthConfig.CLIENT_ID, StandardCharsets.UTF_8)
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                    + "&response_type=code"
                    + "&scope="        + URLEncoder.encode(GoogleOAuthConfig.SCOPES, StandardCharsets.UTF_8)
                    + "&access_type=offline"
                    + "&prompt=consent";

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/callback", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String code = extractParam(query, "code");

                String html = "<html><body><h2>Login successful! You can close this tab.</h2></body></html>";
                exchange.sendResponseHeaders(200, html.length());
                OutputStream os = exchange.getResponseBody();
                os.write(html.getBytes());
                os.close();

                new Thread(() -> {
                    if (code != null) {
                        try {
                            String accessToken = exchangeCodeForToken(code, redirectUri);
                            GoogleUserInfo userInfo = fetchUserInfo(accessToken);
                            future.complete(userInfo);
                        } catch (Exception e) {
                            e.printStackTrace();
                            future.completeExceptionally(e);
                        }
                    } else {
                        future.completeExceptionally(new RuntimeException("No code in callback"));
                    }
                    server.stop(0);
                }).start();
            });

            server.start();

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                future.completeExceptionally(new RuntimeException("Desktop not supported"));
            }

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    private String exchangeCodeForToken(String code, String redirectUri) throws IOException {
        String params = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(GoogleOAuthConfig.CLIENT_ID, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(GoogleOAuthConfig.CLIENT_SECRET, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code";

        URL url = new URL(GoogleOAuthConfig.TOKEN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream out = conn.getOutputStream()) {
            out.write(params.getBytes(StandardCharsets.UTF_8));
        }

        String json = readResponse(conn);
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        return obj.get("access_token").getAsString();
    }

    private GoogleUserInfo fetchUserInfo(String accessToken) throws IOException {
        URL url = new URL(GoogleOAuthConfig.USER_INFO_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        String json = readResponse(conn);
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

        String googleId = obj.get("sub").getAsString();
        String email    = obj.get("email").getAsString();
        String prenom   = obj.has("given_name") ? obj.get("given_name").getAsString() : "";
        String fullName = obj.has("name")       ? obj.get("name").getAsString()       : "";

        return new GoogleUserInfo(googleId, email, prenom, fullName);
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        InputStream is = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private String extractParam(String query, String paramName) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(paramName)) {
                return kv[1];
            }
        }
        return null;
    }
}