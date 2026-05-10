package org.example.services;

import org.example.entities.CertificationFinale;
import org.example.entities.Student;
import org.example.utils.DataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
public class StudentService{
    Connection connection;

    public StudentService() {
        this.connection = DataBase.getInstance().getConnection();
    }

    public void AjouterStudent(Student student) {

        String sql = "INSERT INTO `user`(`username`, `roles`, `google_id`, `password`, `nom`, `prenom`, `email`, `is_active`, `date_inscription`, `date_derniere_connexion`, `specialite`, `experience`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?)";

        try {

            PreparedStatement ps = connection.prepareStatement(sql);

            String hashedPassword =
                    BCrypt.hashpw(student.getPassword(), BCrypt.gensalt());

            ps.setString(1, student.getUserName());
            ps.setString(2, "[\"ROLE_STUDENT\"]");
            ps.setString(3, student.getGoogleId());
            ps.setString(4, hashedPassword);
            ps.setString(5, student.getLastName());
            ps.setString(6, student.getFirstName());
            ps.setString(7, student.getEmail());
            ps.setBoolean(8, student.getIsActive());
            ps.setString(9, student.getDateLastConnexion());
            ps.setString(10, null);
            ps.setString(11, null);

            ps.executeUpdate();

            System.out.println("Student ajouté");

        } catch (Exception e) {

            System.out.println(e.getMessage());
        }
    }

    public List<Student> AfficherStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM `user` WHERE `roles` = '[\"ROLE_STUDENT\"]'";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Student student = new Student(
                    rs.getInt("id"),
                    rs.getString("prenom"),
                    rs.getString("nom"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getBoolean("is_active"),
                    rs.getString("date_inscription"),
                    rs.getString("google_id"),
                    rs.getString("date_derniere_connexion")
            );
            students.add(student);
        }
        return students;
    }

    public void ModifierStudent(Student student) {
        String sql = "UPDATE `user` SET `username`=?, `password`=?, `nom`=?, `prenom`=?, `email`=?, `is_active`=?, `google_id`=? WHERE `id`=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, student.getUserName());
            ps.setString(2, student.getPassword());
            ps.setString(3, student.getLastName());
            ps.setString(4, student.getFirstName());
            ps.setString(5, student.getEmail());
            ps.setBoolean(6, student.getIsActive());
            ps.setString(7, student.getGoogleId());
            ps.setInt(8, student.getId());

            ps.executeUpdate();
            System.out.println("Student modifié");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void SupprimerStudent(int id) {
        String sql = "DELETE FROM `user` WHERE `id`=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);

            ps.executeUpdate();
            System.out.println("Student supprimé");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public Student FindStudentByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM `user` WHERE `username` = ? AND `roles` LIKE '%ROLE_PROFESSOR%'";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Student(
                    rs.getInt("id"), rs.getString("prenom"), rs.getString("nom"),
                    rs.getString("username"), rs.getString("email"), rs.getString("password"),
                    rs.getBoolean("is_active"), rs.getString("date_inscription"),
                    rs.getString("google_id"), rs.getString("date_derniere_connexion")
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

    public List<CertificationFinale> getBadgesForStudent(int userId) {
        List<CertificationFinale> badges = new ArrayList<>();
        String sql = "SELECT * FROM certification_finale WHERE user_id = ? ORDER BY date_emission DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                badges.add(new CertificationFinale(
                        rs.getInt("id"),
                        rs.getTimestamp("date_emission"),
                        rs.getString("badge"),
                        rs.getInt("user_id"),
                        rs.getInt("quiz_id"),
                        rs.getInt("tentative_id")
                ));
            }
        } catch (Exception e) {
            System.err.println("Erreur getBadgesForStudent: " + e.getMessage());
        }
        return badges;
    }

}