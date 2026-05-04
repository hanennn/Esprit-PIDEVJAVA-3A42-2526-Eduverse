package org.example.services;

import org.example.entities.Message;
import org.example.entities.Sujet;
import org.example.entities.User;
import org.example.utils.MyConnection;
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
        String sql = "SELECT m.id, m.contenu, m.gif_url, m.date_publication, m.auteur_id, m.sujet_id, " +
                "u.username AS auteur_username, u.email AS auteur_email, u.nom AS auteur_nom, u.prenom AS auteur_prenom " +
                "FROM message m " +
                "JOIN `user` u ON u.id = m.auteur_id " +
                "WHERE m.sujet_id = ? ORDER BY m.date_publication ASC";
        try (PreparedStatement stmt = MyConnection.getInstance().getCnx().prepareStatement(sql)) {
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
        String sql = "SELECT m.id, m.contenu, m.gif_url, m.date_publication, m.auteur_id, m.sujet_id, " +
                "u.username AS auteur_username, u.email AS auteur_email, u.nom AS auteur_nom, u.prenom AS auteur_prenom " +
                "FROM message m " +
                "JOIN `user` u ON u.id = m.auteur_id " +
                "ORDER BY m.date_publication DESC";
        try (PreparedStatement stmt = MyConnection.getInstance().getCnx().prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
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
        try (PreparedStatement stmt = MyConnection.getInstance().getCnx().prepareStatement(sql)) {
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
        try (PreparedStatement stmt = MyConnection.getInstance().getCnx().prepareStatement(sql)) {
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
        try (PreparedStatement stmt = MyConnection.getInstance().getCnx().prepareStatement(sql)) {
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
        
        // Map auteur info directly from JOIN if available
        if (hasColumn(rs, "auteur_username")) {
            User auteur = new User();
            auteur.setId(message.getAuteurId());
            auteur.setUserName(rs.getString("auteur_username"));
            auteur.setEmail(rs.getString("auteur_email"));
            auteur.setFirstName(rs.getString("auteur_prenom"));
            auteur.setLastName(rs.getString("auteur_nom"));
            message.setAuteur(auteur);
        } else {
            message.setAuteur(loadUserById(message.getAuteurId()));
        }
        
        // Topic info can be set by the controller to avoid circular re-fetching
        return message;
    }

    private User loadUserById(int userId) {
        String firstAndLastName = userService.getNomParId(userId);
        String username = userService.getUsernameParId(userId);
        String email = userService.getEmailParId(userId);

        if (firstAndLastName == null && username == null && email == null) {
            return null;
        }

        User user = new User();
        user.setId(userId);
        user.setUserName(username != null ? username : "Étudiant");
        user.setEmail(email != null ? email : "");
        if (firstAndLastName != null && firstAndLastName.contains(" ")) {
            String[] parts = firstAndLastName.split(" ", 2);
            user.setFirstName(parts[0]);
            user.setLastName(parts[1]);
        } else if (firstAndLastName != null) {
            user.setFirstName(firstAndLastName);
            user.setLastName("");
        }
        return user;
    }

    private synchronized void ensureGifUrlColumnExists() {
        try (java.sql.Statement stmt = MyConnection.getInstance().getCnx().createStatement()) {
            ResultSet rs = MyConnection.getInstance().getCnx().getMetaData().getColumns(null, null, "message", "gif_url");
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
        try (PreparedStatement stmt = MyConnection.getInstance().getCnx().prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int countPreparedQuery(String sql, int value) {
        try (PreparedStatement stmt = MyConnection.getInstance().getCnx().prepareStatement(sql)) {
            stmt.setInt(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}