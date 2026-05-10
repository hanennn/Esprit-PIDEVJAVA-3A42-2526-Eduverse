package org.example.services;

import org.example.entities.cours;
import org.example.entities.Session;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class coursservices implements ICrud<cours> {
    Connection con;

    public coursservices() {
        con = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(cours cours) throws SQLException {
        // Vérification doublon
        String check = "SELECT COUNT(*) FROM cours WHERE LOWER(titre_cours) = LOWER(?)";
        PreparedStatement ps = con.prepareStatement(check);
        ps.setString(1, cours.getTitre_cours());
        ResultSet rs = ps.executeQuery();
        rs.next();
        if (rs.getInt(1) > 0) {
            throw new SQLException("Ce cours existe déjà !");
        }

        // Insertion avec createur_id du formateur connecté
        String sql = "INSERT INTO cours (titre_cours, description, niv_cours, matiere_cours, langue_cours, createur_id) VALUES (?,?,?,?,?,?)";
        PreparedStatement insert = con.prepareStatement(sql);
        insert.setString(1, cours.getTitre_cours());
        insert.setString(2, cours.getDescription());
        insert.setString(3, cours.getNiv_cours());
        insert.setString(4, cours.getMatiere_cours());
        insert.setString(5, cours.getLangue_cours());
        insert.setInt(6, Session.getCurrentUser().getId()); // ← formateur connecté
        insert.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM cours WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Cours supprimé.");
    }

    @Override
    public void modifier(int id, cours cours) throws SQLException {
        String sql = "UPDATE cours SET titre_cours=?, niv_cours=?, matiere_cours=?, langue_cours=?, description=? WHERE id=? AND createur_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, cours.getTitre_cours());
        ps.setString(2, cours.getNiv_cours());
        ps.setString(3, cours.getMatiere_cours());
        ps.setString(4, cours.getLangue_cours());
        ps.setString(5, cours.getDescription());
        ps.setInt(6, id);
        ps.setInt(7, Session.getCurrentUser().getId()); // ← sécurité : seulement ses cours
        ps.executeUpdate();
        System.out.println("Cours modifié avec succès !");
    }

    @Override
    public List<cours> afficher() throws SQLException {
        // Retourne UNIQUEMENT les cours du formateur connecté
        List<cours> liste = new ArrayList<>();
        String sql = "SELECT * FROM cours WHERE createur_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, Session.getCurrentUser().getId()); // ← filtre par formateur
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            cours c = new cours();
            c.setCoursId(rs.getInt("id"));
            c.setTitre_cours(rs.getString("titre_cours"));
            c.setNiv_cours(rs.getString("niv_cours"));
            c.setMatiere_cours(rs.getString("matiere_cours"));
            c.setLangue_cours(rs.getString("langue_cours"));
            c.setDescription(rs.getString("description"));
            c.setCreateur_id(rs.getInt("createur_id")); // ← récupérer aussi
            liste.add(c);
        }
        return liste;
    }

    // Méthode pour afficher TOUS les cours (ex: côté étudiant)
    public List<cours> afficherTous() throws SQLException {
        List<cours> liste = new ArrayList<>();
        String sql = "SELECT * FROM cours";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            cours c = new cours();
            c.setCoursId(rs.getInt("id"));
            c.setTitre_cours(rs.getString("titre_cours"));
            c.setNiv_cours(rs.getString("niv_cours"));
            c.setMatiere_cours(rs.getString("matiere_cours"));
            c.setLangue_cours(rs.getString("langue_cours"));
            c.setDescription(rs.getString("description"));
            c.setCreateur_id(rs.getInt("createur_id"));
            liste.add(c);
        }
        return liste;
    }
}