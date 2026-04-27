package org.example.services;

import org.example.utils.MyConnection;

import java.sql.*;

public class UserService {

    private final Connection cnx;

    public UserService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    // ─────────── EMAIL PAR ID ───────────
    public String getEmailParId(int userId) {
        try {
            String sql = "SELECT email FROM `user` WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("email");
        } catch (Exception e) {
            System.err.println("Erreur getEmail : " + e.getMessage());
        }
        return null;
    }

    // ─────────── NOM COMPLET PAR ID ───────────
    public String getNomParId(int userId) {
        try {
            String sql = "SELECT nom, prenom FROM `user` WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String nom    = rs.getString("nom");
                String prenom = rs.getString("prenom");
                return prenom + " " + nom;
            }
        } catch (Exception e) {
            System.err.println("Erreur getNom : " + e.getMessage());
        }
        return "Étudiant";
    }

    // ─────────── USERNAME PAR ID ───────────
    public String getUsernameParId(int userId) {
        try {
            String sql = "SELECT username FROM `user` WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (Exception e) {
            System.err.println("Erreur getUsername : " + e.getMessage());
        }
        return "Étudiant";
    }
}