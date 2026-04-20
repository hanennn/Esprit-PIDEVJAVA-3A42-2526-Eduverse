package org.example.services;

import org.example.entities.chapitres;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class chapitresservices implements ICrud<chapitres> {
    Connection con;

    public chapitresservices() {
        con = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(chapitres chapitre) throws SQLException {
        // Vérification doublon (même titre dans le même cours)
        String check = "SELECT COUNT(*) FROM chapitres WHERE LOWER(titre_chap) = LOWER(?) AND cours_id = ?";
        PreparedStatement ps = con.prepareStatement(check);
        ps.setString(1, chapitre.getTitre_chap());
        ps.setInt(2, chapitre.getCours_id());
        ResultSet rs = ps.executeQuery();
        rs.next();
        if (rs.getInt(1) > 0) { // il ya une col
            throw new SQLException("Ce chapitre existe déjà dans ce cours !");
        }

        // Insertion
        String sql = "INSERT INTO chapitres (titre_chap, desc_chap, ordre_chap, duree_chap, statut_chap, contenu_chap, type_contenu, cours_id) VALUES(?,?,?,?,?,?,?,?)";
        PreparedStatement insert = con.prepareStatement(sql);
        insert.setString(1, chapitre.getTitre_chap());
        insert.setString(2, chapitre.getDesc_chap());
        insert.setInt(3, chapitre.getOrdre_chap());
        insert.setString(4, chapitre.getDuree_chap());
        insert.setString(5, chapitre.getStatut_chap());
        insert.setString(6, chapitre.getContenu_chap());
        insert.setString(7, chapitre.getType_contenu());
        insert.setInt(8, chapitre.getCours_id());
        insert.executeUpdate();
        System.out.println("Chapitre ajouté avec succès !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM chapitres WHERE id=" + id;
        Statement statement = con.createStatement();
        statement.executeUpdate(sql);
        System.out.println("Chapitre supprimé avec succès !");
    }

    @Override
    public void modifier(int id, chapitres chapitre) throws SQLException {
        String sql = "UPDATE chapitres SET titre_chap='" + chapitre.getTitre_chap()
                + "', desc_chap='" + chapitre.getDesc_chap()
                + "', ordre_chap=" + chapitre.getOrdre_chap()
                + ", duree_chap='" + chapitre.getDuree_chap()
                + "', statut_chap='" + chapitre.getStatut_chap()
                + "', contenu_chap='" + chapitre.getContenu_chap()
                + "', type_contenu='" + chapitre.getType_contenu()
                + "', cours_id=" + chapitre.getCours_id()
                + " WHERE id=" + id;
        Statement statement = con.createStatement();
        statement.executeUpdate(sql);
        System.out.println("Chapitre modifié avec succès !");
    }

    @Override
    public List<chapitres> afficher() throws SQLException {
        List<chapitres> liste = new ArrayList<>();
        String sql = "SELECT * FROM chapitres";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            chapitres c = new chapitres();
            c.setId(rs.getInt("id"));
            c.setTitre_chap(rs.getString("titre_chap"));
            c.setDesc_chap(rs.getString("desc_chap"));
            c.setOrdre_chap(rs.getInt("ordre_chap"));
            c.setDuree_chap(rs.getString("duree_chap"));
            c.setStatut_chap(rs.getString("statut_chap"));
            c.setContenu_chap(rs.getString("contenu_chap"));
            c.setType_contenu(rs.getString("type_contenu"));
            c.setCours_id(rs.getInt("cours_id"));
            liste.add(c);
        }
        return liste;
    }

    public List<chapitres> getChapitresByCours(int idCours) throws SQLException {
        List<chapitres> list = new ArrayList<>();
        String sql = "SELECT * FROM chapitres WHERE cours_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idCours);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            chapitres ch = new chapitres();
            ch.setId(rs.getInt("id"));
            ch.setTitre_chap(rs.getString("titre_chap"));
            ch.setDesc_chap(rs.getString("desc_chap"));
            ch.setOrdre_chap(rs.getInt("ordre_chap"));
            ch.setDuree_chap(rs.getString("duree_chap"));
            ch.setStatut_chap(rs.getString("statut_chap"));
            ch.setContenu_chap(rs.getString("contenu_chap"));
            ch.setType_contenu(rs.getString("type_contenu"));
            ch.setCours_id(rs.getInt("cours_id"));
            list.add(ch);
        }
        return list;
    }
}