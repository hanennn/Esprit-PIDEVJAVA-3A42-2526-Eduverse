package com.eduverse.forum.services;

import com.eduverse.forum.models.Sujet;
import com.eduverse.forum.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SujetService {
    private final UserService userService = new UserService();
    private static boolean ratingTableInitialized;

    public List<Sujet> findAll() {
        return findAll(null);
    }

    public List<Sujet> findAll(Integer currentUserId) {
        ensureRatingTableInitialized();
        List<Sujet> sujets = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT s.id, s.titre, s.contenu, s.image_url, s.date_creation, s.auteur_id, " +
                        "COALESCE(m.nb_messages, 0) AS nb_messages, " +
                        "COALESCE(r.likes_count, 0) AS likes_count, " +
                        "COALESCE(r.dislikes_count, 0) AS dislikes_count");
        if (currentUserId != null) {
            sqlBuilder.append(", COALESCE(ur.rating, 0) AS user_rating");
        }
        sqlBuilder.append(" FROM sujet s " +
                "LEFT JOIN (SELECT sujet_id, COUNT(*) AS nb_messages FROM message GROUP BY sujet_id) m ON m.sujet_id = s.id " +
                "LEFT JOIN (SELECT sujet_id, SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END) AS likes_count, " +
                "SUM(CASE WHEN rating = -1 THEN 1 ELSE 0 END) AS dislikes_count FROM sujet_rating GROUP BY sujet_id) r ON r.sujet_id = s.id ");
        if (currentUserId != null) {
            sqlBuilder.append("LEFT JOIN sujet_rating ur ON ur.sujet_id = s.id AND ur.user_id = ? ");
        }
        sqlBuilder.append("ORDER BY s.date_creation DESC");

        String sql = sqlBuilder.toString();
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (currentUserId != null) {
                stmt.setInt(1, currentUserId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sujets.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger les sujets", e);
        }
        return sujets;
    }

    public Sujet findById(int id) {
        return findById(id, null);
    }

    public Sujet findById(int id, Integer currentUserId) {
        ensureRatingTableInitialized();
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT s.id, s.titre, s.contenu, s.image_url, s.date_creation, s.auteur_id, " +
                        "COALESCE(m.nb_messages, 0) AS nb_messages, " +
                        "COALESCE(r.likes_count, 0) AS likes_count, " +
                        "COALESCE(r.dislikes_count, 0) AS dislikes_count");
        if (currentUserId != null) {
            sqlBuilder.append(", COALESCE(ur.rating, 0) AS user_rating");
        }
        sqlBuilder.append(" FROM sujet s " +
                "LEFT JOIN (SELECT sujet_id, COUNT(*) AS nb_messages FROM message GROUP BY sujet_id) m ON m.sujet_id = s.id " +
                "LEFT JOIN (SELECT sujet_id, SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END) AS likes_count, " +
                "SUM(CASE WHEN rating = -1 THEN 1 ELSE 0 END) AS dislikes_count FROM sujet_rating GROUP BY sujet_id) r ON r.sujet_id = s.id ");
        if (currentUserId != null) {
            sqlBuilder.append("LEFT JOIN sujet_rating ur ON ur.sujet_id = s.id AND ur.user_id = ? ");
        }
        sqlBuilder.append("WHERE s.id = ?");

        String sql = sqlBuilder.toString();
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            int index = 1;
            if (currentUserId != null) {
                stmt.setInt(index++, currentUserId);
            }
            stmt.setInt(index, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger le sujet", e);
        }
        return null;
    }

    public void rateSujet(int sujetId, int userId, int rating) {
        ensureRatingTableInitialized();
        if (rating != 1 && rating != -1) {
            throw new IllegalArgumentException("Le rating doit être 1 (like) ou -1 (dislike)");
        }

        String sql = "INSERT INTO sujet_rating (sujet_id, user_id, rating) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE rating = VALUES(rating), updated_at = CURRENT_TIMESTAMP";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sujetId);
            stmt.setInt(2, userId);
            stmt.setInt(3, rating);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'enregistrer le vote sur le sujet", e);
        }
    }

    public void save(Sujet sujet) {
        ensureImageUrlColumnExists();
        String sql = "INSERT INTO sujet (titre, contenu, image_url, date_creation, auteur_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sujet.getTitre());
            stmt.setString(2, sujet.getContenu());
            stmt.setString(3, sujet.getImageUrl());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, sujet.getAuteurId());
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'enregistrer le sujet", e);
        }
    }

    public void update(Sujet sujet) {
        ensureImageUrlColumnExists();
        String sql = "UPDATE sujet SET titre = ?, contenu = ?, image_url = ? WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sujet.getTitre());
            stmt.setString(2, sujet.getContenu());
            stmt.setString(3, sujet.getImageUrl());
            stmt.setInt(4, sujet.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Impossible de modifier le sujet", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM sujet WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Impossible de supprimer le sujet", e);
        }
    }

    public int countAll() {
        return countQuery("SELECT COUNT(*) FROM sujet");
    }

    public int countByAuteur(int auteurId) {
        return countPreparedQuery("SELECT COUNT(*) FROM sujet WHERE auteur_id = ?", auteurId);
    }

    public int countMessagesBySujet(int sujetId) {
        return countPreparedQuery("SELECT COUNT(*) FROM message WHERE sujet_id = ?", sujetId);
    }

    public List<Map<String, Object>> topAuthors() {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT u.id, u.username, COUNT(DISTINCT s.id) AS nb_sujets, COUNT(DISTINCT m.id) AS nb_messages, " +
                "(COUNT(DISTINCT s.id) + COUNT(DISTINCT m.id)) AS total " +
                "FROM user u LEFT JOIN sujet s ON s.auteur_id = u.id LEFT JOIN message m ON m.auteur_id = u.id " +
                "GROUP BY u.id ORDER BY total DESC LIMIT 3";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("username", rs.getString("username"));
                row.put("nbSujets", rs.getInt("nb_sujets"));
                row.put("nbMessages", rs.getInt("nb_messages"));
                row.put("total", rs.getInt("total"));
                rows.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger les statistiques auteurs", e);
        }
        return rows;
    }

    public Sujet getMostDiscussed() {
        String sql = "SELECT s.id, s.titre, s.contenu, s.image_url, s.date_creation, s.auteur_id, COUNT(m.id) AS nb_messages FROM sujet s " +
                "LEFT JOIN message m ON m.sujet_id = s.id GROUP BY s.id ORDER BY nb_messages DESC, s.date_creation DESC LIMIT 1";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger le sujet le plus discuté", e);
        }
        return null;
    }

    public Sujet getLatest() {
        String sql = "SELECT id, titre, contenu, image_url, date_creation, auteur_id FROM sujet ORDER BY date_creation DESC LIMIT 1";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger le dernier sujet", e);
        }
        return null;
    }

    private Sujet mapRow(ResultSet rs) throws Exception {
        Sujet sujet = new Sujet();
        sujet.setId(rs.getInt("id"));
        sujet.setTitre(rs.getString("titre"));
        sujet.setContenu(rs.getString("contenu"));
        if (hasColumn(rs, "image_url")) {
            sujet.setImageUrl(rs.getString("image_url"));
        }
        Timestamp timestamp = rs.getTimestamp("date_creation");
        if (timestamp != null) {
            sujet.setDateCreation(timestamp.toLocalDateTime());
        }
        sujet.setAuteurId(rs.getInt("auteur_id"));
        if (hasColumn(rs, "nb_messages")) {
            sujet.setNbMessages(rs.getInt("nb_messages"));
        }
        if (hasColumn(rs, "likes_count")) {
            sujet.setLikesCount(rs.getInt("likes_count"));
        }
        if (hasColumn(rs, "dislikes_count")) {
            sujet.setDislikesCount(rs.getInt("dislikes_count"));
        }
        if (hasColumn(rs, "user_rating")) {
            sujet.setUserRating(rs.getInt("user_rating"));
        }
        User auteur = userService.findById(sujet.getAuteurId());
        sujet.setAuteur(auteur);
        return sujet;
    }

    private synchronized void ensureImageUrlColumnExists() {
        try (Connection conn = MyDB.getInstance().getConnection(); Statement stmt = conn.createStatement()) {
            // Check if column exists
            ResultSet rs = conn.getMetaData().getColumns(null, null, "sujet", "image_url");
            if (!rs.next()) {
                stmt.execute("ALTER TABLE sujet ADD COLUMN image_url VARCHAR(255) AFTER contenu");
            }
        } catch (SQLException e) {
            // Handle error or ignore if already exists
        }
    }

    private synchronized void ensureRatingTableInitialized() {
        ensureImageUrlColumnExists();
        if (ratingTableInitialized) {
            return;
        }
        try (Connection conn = MyDB.getInstance().getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS sujet_rating ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "sujet_id INT NOT NULL,"
                    + "user_id INT NOT NULL,"
                    + "rating TINYINT NOT NULL,"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                    + "CONSTRAINT chk_rating_value CHECK (rating IN (1, -1)),"
                    + "UNIQUE KEY uq_sujet_rating_user_sujet (user_id, sujet_id),"
                    + "INDEX idx_sujet_rating_sujet (sujet_id),"
                    + "INDEX idx_sujet_rating_user (user_id),"
                    + "CONSTRAINT fk_sujet_rating_sujet FOREIGN KEY (sujet_id) REFERENCES sujet(id) ON DELETE CASCADE,"
                    + "CONSTRAINT fk_sujet_rating_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE"
                    + ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            ratingTableInitialized = true;
        } catch (SQLException e) {
            throw new RuntimeException("Impossible d'initialiser la table des votes de sujet", e);
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