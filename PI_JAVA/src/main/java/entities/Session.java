package entities;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

import utils.DataBase;
import Services.*;
import utils.Helpers;

public class Session {

    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM `user` WHERE `email` = ? AND `password` = ?";
        PreparedStatement ps = DataBase.getInstance().getConnection().prepareStatement(sql);
        ps.setString(1, email);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String role = rs.getString("roles");
            if (role.contains("ROLE_ADMIN")) {
                currentUser = new Admin(
                        rs.getInt("id"), rs.getString("prenom"), rs.getString("nom"),
                        rs.getString("username"), rs.getString("email"), rs.getString("password"),
                        rs.getBoolean("is_active"), rs.getString("date_inscription")
                );
            } else if (role.contains("ROLE_PROFESSOR")) {
                currentUser = new Professor(
                        rs.getInt("id"), rs.getString("prenom"), rs.getString("nom"),
                        rs.getString("username"), rs.getString("email"), rs.getString("password"),
                        rs.getBoolean("is_active"), rs.getString("date_inscription"),
                        rs.getString("google_id"), rs.getString("specialite"),
                        rs.getString("experience"), rs.getString("date_derniere_connexion")
                );
            } else if (role.contains("ROLE_STUDENT")) {
                currentUser = new Student(
                        rs.getInt("id"), rs.getString("prenom"), rs.getString("nom"),
                        rs.getString("username"), rs.getString("email"), rs.getString("password"),
                        rs.getBoolean("is_active"), rs.getString("date_inscription"),
                        rs.getString("google_id"), rs.getString("date_derniere_connexion")
                );
            }
        }
    }

    public static void logout() throws SQLException {

        try {
            if (currentUser == null) {
                return;
            }
            if (currentUser instanceof Professor) {
                new ProfessorService().updateDateLastConnexion(currentUser.getId());
            }
            if (currentUser instanceof Student) {
                new ProfessorService().updateDateLastConnexion(currentUser.getId());
            }
            currentUser = null;
        }catch (Exception e) {
            System.out.println(e.getMessage());
            }
        }
}