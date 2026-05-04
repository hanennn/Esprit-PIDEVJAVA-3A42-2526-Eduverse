package services;

import models.demande;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class demandeService extends AbstractService<demande> {

    @Override
    public void add(demande d) {
        String query = "INSERT INTO demande (date_demande, niveau_etudes, statut, lettre_motivation, note, etudiant_id, bourse_id, cv_analyse, cv_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setTimestamp(1, d.getDate_demande());
            pstmt.setString(2, d.getNiveau_etudes());
            pstmt.setString(3, d.getStatut());
            pstmt.setString(4, d.getLettre_motivation());
            pstmt.setString(5, d.getNote());
            pstmt.setInt(6, d.getEtudiant_id());
            pstmt.setInt(7, d.getBourse_id());
            pstmt.setString(8, d.getCvAnalyse());
            pstmt.setString(9, d.getCvPath());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(demande d) {
        String query = "UPDATE demande SET date_demande=?, niveau_etudes=?, statut=?, lettre_motivation=?, note=?, etudiant_id=?, bourse_id=?, cv_analyse=?, cv_path=? WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setTimestamp(1, d.getDate_demande());
            pstmt.setString(2, d.getNiveau_etudes());
            pstmt.setString(3, d.getStatut());
            pstmt.setString(4, d.getLettre_motivation());
            pstmt.setString(5, d.getNote());
            pstmt.setInt(6, d.getEtudiant_id());
            pstmt.setInt(7, d.getBourse_id());
            pstmt.setString(8, d.getCvAnalyse());
            pstmt.setString(9, d.getCvPath());
            pstmt.setInt(10, d.getId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("⚠️ Aucune demande mise à jour. ID introuvable : " + d.getId());
            } else {
                System.out.println("✅ Mise à jour de la demande réussie !");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'UPDATE de la demande : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM demande WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public demande getById(int id) {
        String query = "SELECT * FROM demande WHERE id=?";
        demande d = null;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    d = new demande();
                    d.setId(rs.getInt("id"));
                    d.setDate_demande(rs.getTimestamp("date_demande"));
                    d.setNiveau_etudes(rs.getString("niveau_etudes"));
                    d.setStatut(rs.getString("statut"));
                    d.setLettre_motivation(rs.getString("lettre_motivation"));
                    d.setNote(rs.getString("note"));
                    d.setEtudiant_id(rs.getInt("etudiant_id"));
                    d.setBourse_id(rs.getInt("bourse_id"));
                    d.setCvAnalyse(rs.getString("cv_analyse"));
                    d.setCvPath(rs.getString("cv_path"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return d;
    }

    @Override
    public List<demande> getAll() {
        String query = "SELECT * FROM demande";
        List<demande> list = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    demande d = new demande();
                    d.setId(rs.getInt("id"));
                    d.setDate_demande(rs.getTimestamp("date_demande"));
                    d.setNiveau_etudes(rs.getString("niveau_etudes"));
                    d.setStatut(rs.getString("statut"));
                    d.setLettre_motivation(rs.getString("lettre_motivation"));
                    d.setNote(rs.getString("note"));
                    d.setEtudiant_id(rs.getInt("etudiant_id"));
                    d.setBourse_id(rs.getInt("bourse_id"));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
