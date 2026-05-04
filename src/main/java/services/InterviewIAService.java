package services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.AnalyseInterview;
import utils.DataBase;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.sql.*;
import java.util.UUID;

public class InterviewIAService {

    private static final String API_URL = "http://localhost:8001/analyser-interview";
    private final Connection connection;
    private final Gson gson = new Gson();

    public InterviewIAService() {
        this.connection = DataBase.getInstance().getConnection();
    }

    public AnalyseInterview analyserAudio(File audioFile, int demandeId) throws IOException, InterruptedException {
        String boundary = UUID.randomUUID().toString();

        byte[] fileBytes = Files.readAllBytes(audioFile.toPath());
        String fileName = audioFile.getName();

        byte[] bodyBytes = buildMultipartBody(boundary, "audio", fileName, fileBytes);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                .timeout(java.time.Duration.ofSeconds(120))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Erreur API IA: " + response.statusCode() + " - " + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

        AnalyseInterview analyse = new AnalyseInterview();
        analyse.setDemandeId(demandeId);
        analyse.setTranscription(json.get("transcription").getAsString());
        analyse.setScoresEmotions(gson.toJson(json.get("scores_emotions")));
        analyse.setFeaturesAudio(gson.toJson(json.get("features_audio")));
        analyse.setProfilGlobal(json.get("profil_global").getAsString());
        analyse.setRecommandation(json.get("recommandation").getAsString());
        analyse.setDateAnalyse(new Timestamp(System.currentTimeMillis()));

        return analyse;
    }

    public void save(AnalyseInterview a) {
        String query = "INSERT INTO analyse_interview (demande_id, transcription, scores_emotions, features_audio, profil_global, recommandation, date_analyse) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, a.getDemandeId());
            pstmt.setString(2, a.getTranscription());
            pstmt.setString(3, a.getScoresEmotions());
            pstmt.setString(4, a.getFeaturesAudio());
            pstmt.setString(5, a.getProfilGlobal());
            pstmt.setString(6, a.getRecommandation());
            pstmt.setTimestamp(7, a.getDateAnalyse());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                a.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public AnalyseInterview getByDemandeId(int demandeId) {
        String query = "SELECT * FROM analyse_interview WHERE demande_id = ? ORDER BY date_analyse DESC LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, demandeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                AnalyseInterview a = new AnalyseInterview();
                a.setId(rs.getInt("id"));
                a.setDemandeId(rs.getInt("demande_id"));
                a.setTranscription(rs.getString("transcription"));
                a.setScoresEmotions(rs.getString("scores_emotions"));
                a.setFeaturesAudio(rs.getString("features_audio"));
                a.setProfilGlobal(rs.getString("profil_global"));
                a.setRecommandation(rs.getString("recommandation"));
                a.setDateAnalyse(rs.getTimestamp("date_analyse"));
                return a;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] buildMultipartBody(String boundary, String fieldName, String fileName, byte[] fileBytes) throws IOException {
        String CRLF = "\r\n";
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append(CRLF);
        sb.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"; filename=\"").append(fileName).append("\"").append(CRLF);
        sb.append("Content-Type: application/octet-stream").append(CRLF);
        sb.append(CRLF);

        byte[] header = sb.toString().getBytes();
        byte[] footer = (CRLF + "--" + boundary + "--" + CRLF).getBytes();

        byte[] body = new byte[header.length + fileBytes.length + footer.length];
        System.arraycopy(header, 0, body, 0, header.length);
        System.arraycopy(fileBytes, 0, body, header.length, fileBytes.length);
        System.arraycopy(footer, 0, body, header.length + fileBytes.length, footer.length);

        return body;
    }
}
