package com.eduverse.forum.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class KlipyUtil {
    private static final String API_KEY = "K80t0xlmw6nD6r4rLUknjNaOb1setZ4Tx8SqXW61qn4krGvLH4yhok2Qbp6NGOJv";
    // Correction : Utilisation du domaine .com et de l'API v2 (compatible Tenor)
    private static final String SEARCH_URL = "https://api.klipy.com/v2/search";

    public static List<String> searchGifs(String query) {
        List<String> urls = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            // L'API v2 utilise 'key' comme paramètre de requête
            String url = SEARCH_URL + "?q=" + query.replace(" ", "+") + "&key=" + API_KEY + "&limit=10";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                if (json.has("results")) {
                    JSONArray results = json.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject item = results.getJSONObject(i);
                        // Dans la v2, on peut récupérer l'URL directe du GIF
                        if (item.has("url")) {
                            urls.add(item.getString("url"));
                        } else if (item.has("media_formats")) {
                            JSONObject media = item.getJSONObject("media_formats");
                            if (media.has("gif")) {
                                urls.add(media.getJSONObject("gif").getString("url"));
                            }
                        }
                    }
                }
            } else {
                System.err.println("Klipy error: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urls;
    }
}
