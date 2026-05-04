package org.example.services;

import org.example.entities.Professor;
import org.example.utils.DataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfessorService {
    Connection connection;

    public ProfessorService() {
        this.connection = DataBase.getInstance().getConnection();
    }

    public void AjouterProfessor(Professor professor) {
        String sql = "INSERT INTO `user`(`username`, `roles`, `google_id`, `password`, `nom`, `prenom`, `email`, `is_active`, `date_inscription`, `date_derniere_connexion`, `specialite`, `experience`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, professor.getUserName());
            ps.setString(2, "[\"ROLE_PROFESSOR\"]");
            ps.setString(3, professor.getGoogleId());
            ps.setString(4, professor.getPassword());
            ps.setString(5, professor.getLastName());
            ps.setString(6, professor.getFirstName());
            ps.setString(7, professor.getEmail());
            ps.setBoolean(8, professor.getIsActive());
            ps.setString(9, professor.getDateLastConnexion());
            ps.setString(10, professor.getSpecialty());
            ps.setString(11, professor.getExperience());

            ps.executeUpdate();
            System.out.println("Professor ajouté!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Professor> AfficherProfessors() throws SQLException {
        List<Professor> professors = new ArrayList<>();
        String sql = "SELECT * FROM `user` WHERE `roles` LIKE '%ROLE_PROFESSOR%'";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Professor professor = new Professor(
                    rs.getInt("id"),
                    rs.getString("prenom"),
                    rs.getString("nom"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getBoolean("is_active"),
                    rs.getString("date_inscription"),
                    rs.getString("google_id"),
                    rs.getString("specialite"),
                    rs.getString("experience"),
                    rs.getString("date_derniere_connexion")
            );
            professors.add(professor);
        }
        return professors;
    }

    public void ModifierProfessor(Professor professor) throws SQLException {
        String sql = "UPDATE `user` SET `username`=?, `password`=?, `nom`=?, `prenom`=?, `email`=?, `is_active`=?, `google_id`=?, `specialite`=?, `experience`=? WHERE `id`=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, professor.getUserName());
            ps.setString(2, professor.getPassword());
            ps.setString(3, professor.getLastName());
            ps.setString(4, professor.getFirstName());
            ps.setString(5, professor.getEmail());
            ps.setBoolean(6, professor.getIsActive());
            ps.setString(7, professor.getGoogleId());
            ps.setString(8, professor.getSpecialty());
            ps.setString(9, professor.getExperience());
            ps.setInt(10, professor.getId());

            ps.executeUpdate();
            System.out.println("Professor modifié!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void SupprimerProfessor(int id) throws  SQLException {
        String sql = "DELETE FROM `user` WHERE `id`=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);

            ps.executeUpdate();
            System.out.println("Professor supprimé!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public Professor FindProfessorByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM `user` WHERE `username` = ? AND `roles` LIKE '%ROLE_PROFESSOR%'";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Professor(
                    rs.getInt("id"), rs.getString("prenom"), rs.getString("nom"),
                    rs.getString("username"), rs.getString("email"), rs.getString("password"),
                    rs.getBoolean("is_active"), rs.getString("date_inscription"),
                    rs.getString("google_id"), rs.getString("specialite"),
                    rs.getString("experience"), rs.getString("date_derniere_connexion")
            );
        }
        return null;
    }


    public void updateDateLastConnexion(int id) throws SQLException {
        String sql = "UPDATE `user` SET `date_derniere_connexion` = NOW() WHERE `id` = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}