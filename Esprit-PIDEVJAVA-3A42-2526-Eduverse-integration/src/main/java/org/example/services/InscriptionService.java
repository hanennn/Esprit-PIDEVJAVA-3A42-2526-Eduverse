package org.example.services;

import org.example.entities.Inscription;
import org.example.utils.DataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.example.entities.cours;
import java.util.List;
import java.util.ArrayList;

public class InscriptionService {

    private final Connection conn = DataBase.getInstance().getConnection();

    public boolean isAlreadyInscrit(int userId, int coursId) throws Exception {
        String sql = "SELECT id FROM inscription WHERE user_id = ? AND cours_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, coursId);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    public void inscrire(int userId, int coursId) throws Exception {
        if (isAlreadyInscrit(userId, coursId)) return; // avoid duplicates

        String sql = "INSERT INTO inscription (user_id, cours_id) VALUES (?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, coursId);
        ps.executeUpdate();
    }
    public List<Integer> getCoursIdsForUser(int userId) throws Exception {
        List<Integer> coursIds = new ArrayList<>();
        String sql = "SELECT cours_id FROM inscription WHERE user_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            coursIds.add(rs.getInt("cours_id"));
        }
        return coursIds;
    }

    public List<cours> getCoursForUser(int userId) throws Exception {
        List<cours> result = new ArrayList<>();
        String sql = """
        SELECT c.* FROM cours c
        INNER JOIN inscription i ON c.id = i.cours_id
        WHERE i.user_id = ?
    """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            cours c = new cours();
            c.setCoursId(rs.getInt("id"));
            c.setTitre_cours(rs.getString("titre_cours"));
            c.setNiv_cours(rs.getString("niv_cours"));
            c.setMatiere_cours(rs.getString("matiere_cours"));
            c.setLangue_cours(rs.getString("langue_cours"));
            c.setDescription(rs.getString("description"));
            result.add(c);
        }
        return result;
    }


}