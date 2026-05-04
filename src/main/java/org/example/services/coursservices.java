package org.example.services;

import org.example.entities.cours;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class coursservices implements ICrud<cours> {
    Connection con;

    public coursservices() {
        con= MyDataBase.getInstance().getConnection();
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

        // Insertion
        String sql = "INSERT INTO cours (titre_cours, description, niv_cours, matiere_cours, langue_cours) VALUES(?,?,?,?,?)";
        PreparedStatement insert = con.prepareStatement(sql);
        insert.setString(1, cours.getTitre_cours());
        insert.setString(2, cours.getDescription());
        insert.setString(3, cours.getNiv_cours());
        insert.setString(4, cours.getMatiere_cours());
        insert.setString(5, cours.getLangue_cours());
        insert.executeUpdate();
    }


    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM cours WHERE `id`=?";
        PreparedStatement preparedStatement= con.prepareStatement(sql);
        preparedStatement.setInt(1, id );
        preparedStatement.executeUpdate();
        System.out.println("cours supprimé");

    }


    @Override
    public void modifier(int id, cours cours) throws SQLException {
        String sql = "UPDATE cours SET titre_cours='" + cours.getTitre_cours()
                + "', niv_cours='" + cours.getNiv_cours()
                + "', matiere_cours='" + cours.getMatiere_cours()
                + "', langue_cours='" + cours.getLangue_cours()
                + "', description='" + cours.getDescription()
                + "' WHERE id=" + id;
        Statement statement = con.createStatement();
        statement.executeUpdate(sql);
        System.out.println("Cours modifié avec succès !");
    }

    @Override
    public List<cours> afficher() throws SQLException {
        List<cours> cours=new ArrayList<>();
        String sql = "SELECT * FROM cours";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            cours c = new cours();
            c.setCoursId(rs.getInt("id"));
            c.setTitre_cours(rs.getString("titre_cours"));
            c.setNiv_cours(rs.getString("niv_cours"));
            c.setMatiere_cours(rs.getString("matiere_cours"));
            c.setLangue_cours(rs.getString("langue_cours"));
            c.setDescription(rs.getString("description"));

            cours.add(c);
        }
        return cours;

    }
}
