package com.eduverse.forum.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;
import org.json.JSONObject;

public class ImageKitUtil {
    private static final String PRIVATE_KEY = "private_o2rLlYsQMjw4LILubrvF39MZaJ8=";
    private static final String UPLOAD_URL = "https://upload.imagekit.io/api/v1/files/upload";

    public static String uploadImage(File file) {
        try {
            String boundary = "Boundary-" + UUID.randomUUID().toString();
            byte[] multipartBody = createMultipartBody(file, boundary);

            HttpClient client = HttpClient.newHttpClient();
            String authHeader = "Basic " + Base64.getEncoder().encodeToString((PRIVATE_KEY + ":").getBytes());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(UPLOAD_URL))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Authorization", authHeader)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                return json.getString("url");
            } else {
                System.err.println("Upload failed with status " + response.statusCode() + ": " + response.body());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] createMultipartBody(File file, String boundary) throws IOException {
        String fileName = file.getName();
        byte[] fileContent = Files.readAllBytes(file.toPath());
        
        StringBuilder sb = new StringBuilder();
        // File part
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"\r\n");
        sb.append("Content-Type: image/").append(getFileExtension(fileName)).append("\r\n\r\n");
        
        byte[] before = sb.toString().getBytes();
        
        sb = new StringBuilder();
        sb.append("\r\n");
        
        // FileName part
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"fileName\"\r\n\r\n");
        sb.append(fileName).append("\r\n");
        
        // Final boundary
        sb.append("--").append(boundary).append("--\r\n");
        
        byte[] after = sb.toString().getBytes();
        
        byte[] fullBody = new byte[before.length + fileContent.length + after.length];
        System.arraycopy(before, 0, fullBody, 0, before.length);
        System.arraycopy(fileContent, 0, fullBody, before.length, fileContent.length);
        System.arraycopy(after, 0, fullBody, before.length + fileContent.length, after.length);
        
        return fullBody;
    }

    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot == -1) ? "jpeg" : fileName.substring(lastDot + 1);
    }
}
