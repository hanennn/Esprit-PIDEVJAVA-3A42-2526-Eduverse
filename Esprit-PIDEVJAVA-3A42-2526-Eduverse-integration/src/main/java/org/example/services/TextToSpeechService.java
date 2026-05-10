package org.example.services;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.security.cert.X509Certificate;

public class TextToSpeechService {

    private static final String API_KEY = "00a3dbaf461645aea2fd0dae97e6cdc7";

    public static File genererAudio(String texte) {
        try {
            String encodedText = java.net.URLEncoder.encode(texte, "UTF-8");
            String url = "https://api.voicerss.org/?key=" + API_KEY
                    + "&hl=fr-fr&src=" + encodedText
                    + "&c=MP3&f=44khz_16bit_stereo";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            // Désactiver la vérification SSL
            TrustManager[] trustAll = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] c, String a) {}
                        public void checkServerTrusted(X509Certificate[] c, String a) {}
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAll, new java.security.SecureRandom());

            HttpClient client = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();

            HttpResponse<byte[]> response = client
                    .send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                File audioFile = File.createTempFile("tts_", ".mp3");
                Files.write(audioFile.toPath(), response.body());
                System.out.println("Audio généré : " + audioFile.getAbsolutePath());
                return audioFile;
            } else {
                System.err.println("Erreur TTS : " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
            return null;
        }
    }
}