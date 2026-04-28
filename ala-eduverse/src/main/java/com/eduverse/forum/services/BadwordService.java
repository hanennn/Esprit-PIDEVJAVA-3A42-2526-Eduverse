package com.eduverse.forum.services;

import com.eduverse.forum.models.Badword;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BadwordService {
    public List<Badword> findAllActive() {
        List<Badword> badwords = new ArrayList<>();
        String sql = "SELECT id, word, action, active FROM badword WHERE active = 1 ORDER BY word";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                badwords.add(mapRow(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger les badwords", e);
        }
        return badwords;
    }

    public List<Badword> findAll() {
        List<Badword> badwords = new ArrayList<>();
        String sql = "SELECT id, word, action, active FROM badword ORDER BY word";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                badwords.add(mapRow(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger les badwords", e);
        }
        return badwords;
    }

    public void save(Badword badword) {
        String sql = "INSERT INTO badword (word, action, active) VALUES (?, ?, ?)";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, badword.getWord().toLowerCase().trim());
            stmt.setString(2, badword.getAction());
            stmt.setBoolean(3, badword.isActive());
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'enregistrer le badword", e);
        }
    }

    public void update(Badword badword) {
        String sql = "UPDATE badword SET word = ?, action = ?, active = ? WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, badword.getWord().toLowerCase().trim());
            stmt.setString(2, badword.getAction());
            stmt.setBoolean(3, badword.isActive());
            stmt.setInt(4, badword.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Impossible de modifier le badword", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM badword WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Impossible de supprimer le badword", e);
        }
    }

    private Badword mapRow(ResultSet rs) throws Exception {
        return new Badword(
                rs.getInt("id"),
                rs.getString("word"),
                rs.getString("action"),
                rs.getBoolean("active")
        );
    }
}
