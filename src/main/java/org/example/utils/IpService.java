package org.example.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

public class IpService {

    private static final String API_KEY = "56cc97bd6753452ea36e5b8c755e0fb7";
    private static final String URL = "https://api.ipgeolocation.io/ipgeo?apiKey=" + API_KEY;

    public static String getCurrentIp() {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(URL).build();

            Response response = client.newCall(request).execute();
            String body = response.body().string();
            JSONObject json = new JSONObject(body);
            return json.getString("ip");

        } catch (Exception e) {
            System.err.println("Could not fetch IP: " + e.getMessage());
            return null;
        }
    }
}