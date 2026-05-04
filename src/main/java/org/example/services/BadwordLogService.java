package org.example.services;

import org.example.utils.MyConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class BadwordLogService {
    public void logViolation(int userId, String violatedWord, String action, String content) {
        String sql = "INSERT INTO badword_log (user_id, violated_word, action, content, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = MyConnection.getInstance().getCnx().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, violatedWord);
            stmt.setString(3, action);
            stmt.setString(4, content);
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        } catch (Exception e) {
            // Log silencieusement pour ne pas bloquer la fonctionnalité
            System.err.println("Impossible de enregistrer le log de badword: " + e.getMessage());
        }
    }
}
