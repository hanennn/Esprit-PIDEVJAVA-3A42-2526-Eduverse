package com.eduverse.forum.services;

import com.eduverse.forum.models.Message;
import com.eduverse.forum.models.Sujet;
import com.eduverse.forum.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageService {
    private final UserService userService = new UserService();
    private final SujetService sujetService = new SujetService();

    public List<Message> findBySujetId(int sujetId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, contenu, gif_url, date_publication, auteur_id, sujet_id FROM message WHERE sujet_id = ? ORDER BY date_publication ASC";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sujetId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger les messages", e);
        }
        return messages;
    }

    public List<Message> findAll() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, contenu, gif_url, date_publication, auteur_id, sujet_id FROM message ORDER BY date_publication DESC";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                messages.add(mapRow(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger tous les messages", e);
        }
        return messages;
    }

    public void save(Message message) {
        ensureGifUrlColumnExists();
        String sql = "INSERT INTO message (contenu, gif_url, date_publication, auteur_id, sujet_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, message.getContenu());
            stmt.setString(2, message.getGifUrl());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(4, message.getAuteurId());
            stmt.setInt(5, message.getSujetId());
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'enregistrer le message", e);
        }
    }

    public void update(Message message) {
        ensureGifUrlColumnExists();
        String sql = "UPDATE message SET contenu = ?, gif_url = ? WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, message.getContenu());
            stmt.setString(2, message.getGifUrl());
            stmt.setInt(3, message.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Impossible de modifier le message", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM message WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Impossible de supprimer le message", e);
        }
    }

    public int countAll() {
        return countQuery("SELECT COUNT(*) FROM message");
    }

    public int countByAuteur(int auteurId) {
        return countPreparedQuery("SELECT COUNT(*) FROM message WHERE auteur_id = ?", auteurId);
    }

    private Message mapRow(ResultSet rs) throws Exception {
        Message message = new Message();
        message.setId(rs.getInt("id"));
        message.setContenu(rs.getString("contenu"));
        if (hasColumn(rs, "gif_url")) {
            message.setGifUrl(rs.getString("gif_url"));
        }
        Timestamp timestamp = rs.getTimestamp("date_publication");
        if (timestamp != null) {
            message.setDatePublication(timestamp.toLocalDateTime());
        }
        message.setAuteurId(rs.getInt("auteur_id"));
        message.setSujetId(rs.getInt("sujet_id"));
        User auteur = userService.findById(message.getAuteurId());
        message.setAuteur(auteur);
        Sujet sujet = sujetService.findById(message.getSujetId());
        message.setSujet(sujet);
        return message;
    }

    private synchronized void ensureGifUrlColumnExists() {
        try (Connection conn = MyDB.getInstance().getConnection(); java.sql.Statement stmt = conn.createStatement()) {
            ResultSet rs = conn.getMetaData().getColumns(null, null, "message", "gif_url");
            if (!rs.next()) {
                stmt.execute("ALTER TABLE message ADD COLUMN gif_url VARCHAR(255) AFTER contenu");
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private boolean hasColumn(ResultSet rs, String name) {
        try {
            rs.findColumn(name);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private int countQuery(String sql) {
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int countPreparedQuery(String sql, int value) {
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}