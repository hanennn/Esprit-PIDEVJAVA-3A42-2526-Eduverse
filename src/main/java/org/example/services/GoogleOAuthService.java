package org.example.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import okhttp3.*;
import org.example.utils.GoogleOAuthConfig;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class GoogleOAuthService {

    private int findAvailablePort() {
        int[] portsToTry = {8888, 9090, 9191, 9292, 7777, 7878, 6666};
        for (int port : portsToTry) {
            try (ServerSocket s = new ServerSocket(port)) {
                return port;
            } catch (IOException ignored) {}
        }
        throw new RuntimeException("No available port found for OAuth callback");
    }

    public CompletableFuture<GoogleUserInfo> startOAuthFlow() {
        CompletableFuture<GoogleUserInfo> future = new CompletableFuture<>();

        try {
            // 1. Find a free port dynamically
            int port = findAvailablePort();
            String redirectUri = "http://localhost:" + port + "/callback";

            // 2. Build the Google authorization URL
            String authUrl = GoogleOAuthConfig.AUTH_URL
                    + "?client_id="     + URLEncoder.encode(GoogleOAuthConfig.CLIENT_ID, StandardCharsets.UTF_8)
                    + "&redirect_uri="  + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                    + "&response_type=code"
                    + "&scope="         + URLEncoder.encode(GoogleOAuthConfig.SCOPES, StandardCharsets.UTF_8)
                    + "&access_type=offline"
                    + "&prompt=consent";

            // 3. Start local HTTP server on the free port
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/callback", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String code  = extractParam(query, "code");

                // Send response to browser
                String html = "<html><body><h2>Login successful! You can close this tab.</h2></body></html>";
                exchange.sendResponseHeaders(200, html.length());
                OutputStream os = exchange.getResponseBody();
                os.write(html.getBytes());
                os.close();
                server.stop(0);

                if (code != null) {
                    try {
                        String accessToken = exchangeCodeForToken(code, redirectUri);
                        GoogleUserInfo userInfo = fetchUserInfo(accessToken);
                        future.complete(userInfo);
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                } else {
                    future.completeExceptionally(new RuntimeException("No code in callback"));
                }
            });

            server.start();

            // 4. Open the browser
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

    /**
     * Exchange the authorization code for an access token.
     */
    private String exchangeCodeForToken(String code, String redirectUri) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("code",          code)
                .add("client_id",     GoogleOAuthConfig.CLIENT_ID)
                .add("client_secret", GoogleOAuthConfig.CLIENT_SECRET)
                .add("redirect_uri",  redirectUri)
                .add("grant_type",    "authorization_code")
                .build();

        Request request = new Request.Builder()
                .url(GoogleOAuthConfig.TOKEN_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            return obj.get("access_token").getAsString();
        }
    }

    /**
     * Use the access token to fetch the user's Google profile.
     */
    private GoogleUserInfo fetchUserInfo(String accessToken) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(GoogleOAuthConfig.USER_INFO_URL)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            String googleId  = obj.get("sub").getAsString();
            String email     = obj.get("email").getAsString();
            String prenom    = obj.has("given_name") ? obj.get("given_name").getAsString() : "";
            String fullName  = obj.has("name")       ? obj.get("name").getAsString()       : "";

            return new GoogleUserInfo(googleId, email, prenom, fullName);
        }
    }

    /**
     * Extracts a query parameter value from a query string.
     */
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