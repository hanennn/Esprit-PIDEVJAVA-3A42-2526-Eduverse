package org.example.services;

import org.example.entities.CertificationFinale;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CertificationFinaleService {

    private final Connection cnx;

    public CertificationFinaleService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    // ─────────── VÉRIFIER DOUBLON ───────────
    public boolean existeDeja(int tentativeId) {
        try {
            String sql =
                    "SELECT COUNT(*) FROM certification_finale " +
                            "WHERE tentative_id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, tentativeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) {
            System.err.println("Erreur doublon certif finale : "
                    + e.getMessage());
        }
        return false;
    }

    // ─────────── AJOUTER ───────────
    public void ajouter(CertificationFinale cf) throws Exception {
        if (existeDeja(cf.getTentativeId())) {
            throw new Exception(
                    "Une certification finale existe déjà pour " +
                            "la tentative #" + cf.getTentativeId() + " !");
        }
        String sql =
                "INSERT INTO certification_finale " +
                        "(date_emission, badge, user_id, quiz_id, tentative_id) " +
                        "VALUES (?,?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setTimestamp(1, cf.getDateEmission());
        ps.setString(2, cf.getBadge());
        ps.setInt(3, cf.getUserId());
        ps.setInt(4, cf.getQuizId());
        ps.setInt(5, cf.getTentativeId());
        ps.executeUpdate();
        System.out.println("Certification finale ajoutée avec succès");
    }

    // ─────────── MODIFIER ───────────
    public void modifier(CertificationFinale cf) throws Exception {
        String sql =
                "UPDATE certification_finale SET " +
                        "date_emission=?, badge=?, user_id=?, " +
                        "quiz_id=?, tentative_id=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setTimestamp(1, cf.getDateEmission());
        ps.setString(2, cf.getBadge());
        ps.setInt(3, cf.getUserId());
        ps.setInt(4, cf.getQuizId());
        ps.setInt(5, cf.getTentativeId());
        ps.setInt(6, cf.getId());
        ps.executeUpdate();
        System.out.println("Certification finale modifiée avec succès");
    }

    // ─────────── SUPPRIMER ───────────
    public void supprimer(int id) throws Exception {
        String sql = "DELETE FROM certification_finale WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Certification finale supprimée avec succès");
    }

    // ─────────── AFFICHER ───────────
    public List<CertificationFinale> afficher() throws Exception {
        List<CertificationFinale> list = new ArrayList<>();
        String sql = "SELECT * FROM certification_finale";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(new CertificationFinale(
                    rs.getInt("id"),
                    rs.getTimestamp("date_emission"),
                    rs.getString("badge"),
                    rs.getInt("user_id"),
                    rs.getInt("quiz_id"),
                    rs.getInt("tentative_id")
            ));
        }
        return list;
    }
}