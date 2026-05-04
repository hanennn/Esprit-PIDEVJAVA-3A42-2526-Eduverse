package org.example.services;

import org.example.entities.Certification;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CertificationService {

    private final Connection cnx;

    public CertificationService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    // ─────────── VÉRIFIER DOUBLON ───────────
    public boolean existeDeja(int userId, int quizId) {
        try {
            String sql =
                    "SELECT COUNT(*) FROM certification " +
                            "WHERE user_id = ? AND quiz_id = ? " +
                            "AND DATE(date_attribution) = CURDATE()";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, quizId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) {
            System.err.println("Erreur doublon certification : "
                    + e.getMessage());
        }
        return false;
    }

    // ─────────── AJOUTER ───────────
    public void ajouter(Certification certif) throws Exception {
        String sql =
                "INSERT INTO certification " +
                        "(score_obtenu, statut, badge, date_attribution, " +
                        "user_id, quiz_id) VALUES (?,?,?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setFloat(1, certif.getScoreObtenu());
        ps.setString(2, certif.getStatut());
        ps.setString(3, certif.getBadge());
        ps.setTimestamp(4, certif.getDateAttribution());
        ps.setInt(5, certif.getUserId());
        ps.setInt(6, certif.getQuizId());
        ps.executeUpdate();
        System.out.println("Certification ajoutée avec succès");
    }

    // ─────────── MODIFIER ───────────
    public void modifier(Certification certif) throws Exception {
        String sql =
                "UPDATE certification SET " +
                        "score_obtenu=?, statut=?, badge=?, " +
                        "date_attribution=?, user_id=?, quiz_id=? " +
                        "WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setFloat(1, certif.getScoreObtenu());
        ps.setString(2, certif.getStatut());
        ps.setString(3, certif.getBadge());
        ps.setTimestamp(4, certif.getDateAttribution());
        ps.setInt(5, certif.getUserId());
        ps.setInt(6, certif.getQuizId());
        ps.setInt(7, certif.getId());
        ps.executeUpdate();
        System.out.println("Certification modifiée avec succès");
    }

    // ─────────── SUPPRIMER ───────────
    public void supprimer(int id) throws Exception {
        String sql = "DELETE FROM certification WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Certification supprimée avec succès");
    }

    // ─────────── AFFICHER ───────────
    public List<Certification> afficher() throws Exception {
        List<Certification> list = new ArrayList<>();
        String sql = "SELECT * FROM certification";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(new Certification(
                    rs.getInt("id"),
                    rs.getFloat("score_obtenu"),
                    rs.getString("statut"),
                    rs.getString("badge"),
                    rs.getTimestamp("date_attribution"),
                    rs.getInt("user_id"),
                    rs.getInt("quiz_id")
            ));
        }
        return list;
    }

    // ─────────── AFFICHER PAR USER ───────────
    public List<Certification> afficherParUser(int userId) throws Exception {
        List<Certification> list = new ArrayList<>();
        String sql =
                "SELECT * FROM certification WHERE user_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Certification(
                    rs.getInt("id"),
                    rs.getFloat("score_obtenu"),
                    rs.getString("statut"),
                    rs.getString("badge"),
                    rs.getTimestamp("date_attribution"),
                    rs.getInt("user_id"),
                    rs.getInt("quiz_id")
            ));
        }
        return list;
    }
}