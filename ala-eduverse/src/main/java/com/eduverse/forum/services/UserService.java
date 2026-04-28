package com.eduverse.forum.services;

import com.eduverse.forum.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    public List<User> findActiveUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, roles, nom, prenom, email, is_active FROM user WHERE is_active = 1 ORDER BY username";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger les utilisateurs", e);
        }
        return users;
    }

    public User findById(int id) {
        String sql = "SELECT id, username, roles, nom, prenom, email, is_active FROM user WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger l'utilisateur", e);
        }
        return null;
    }

    public boolean isAdmin(User user) {
        return user != null && user.hasRole("ROLE_ADMIN");
    }

    private User mapRow(ResultSet rs) throws Exception {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("roles"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getBoolean("is_active")
        );
    }
}