package entities;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

import utils.DataBase;
import Services.*;
import utils.Helpers;
import utils.RememberMeManager;

public class Session {

    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void login(String email, String password ) throws SQLException {
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


    public static void loginWithGoogle(GoogleUserInfo info) throws SQLException {
        Connection conn = DataBase.getInstance().getConnection();

        String sql = "SELECT * FROM `user` WHERE `google_id` = ? OR `email` = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, info.googleId);
        ps.setString(2, info.email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            if (rs.getString("google_id") == null) {
                PreparedStatement update = conn.prepareStatement(
                        "UPDATE `user` SET `google_id` = ? WHERE `email` = ?");
                update.setString(1, info.googleId);
                update.setString(2, info.email);
                update.executeUpdate();
            }

            String role = rs.getString("roles");
            if (role != null && role.contains("ROLE_ADMIN")) {
                currentUser = new Admin(
                        rs.getInt("id"), rs.getString("prenom"), rs.getString("nom"),
                        rs.getString("username"), rs.getString("email"), rs.getString("password"),
                        rs.getBoolean("is_active"), rs.getString("date_inscription")
                );
            } else if (role != null && role.contains("ROLE_PROFESSOR")) {
                currentUser = new Professor(
                        rs.getInt("id"), rs.getString("prenom"), rs.getString("nom"),
                        rs.getString("username"), rs.getString("email"), rs.getString("password"),
                        rs.getBoolean("is_active"), rs.getString("date_inscription"),
                        rs.getString("google_id"), rs.getString("specialite"),
                        rs.getString("experience"), rs.getString("date_derniere_connexion")
                );
            } else {
                currentUser = new Student(
                        rs.getInt("id"), rs.getString("prenom"), rs.getString("nom"),
                        rs.getString("username"), rs.getString("email"), rs.getString("password"),
                        rs.getBoolean("is_active"), rs.getString("date_inscription"),
                        rs.getString("google_id"), rs.getString("date_derniere_connexion")
                );
            }
        } else {
            // New user — auto-register as Student
            String insertSql =
                    "INSERT INTO `user` (prenom, nom, username, email, google_id, password, roles, is_active, date_inscription) " +
                            "VALUES (?, ?, ?, ?, ?, '', 'ROLE_STUDENT', true, NOW())";
            PreparedStatement insertPs = conn.prepareStatement(
                    insertSql, Statement.RETURN_GENERATED_KEYS);

            String prenom   = info.prenom   != null ? info.prenom   : "";
            String fullName = info.fullName != null ? info.fullName : "";
            String username = info.email.split("@")[0]; // derive username from email

            insertPs.setString(1, prenom);
            insertPs.setString(2, fullName);
            insertPs.setString(3, username);
            insertPs.setString(4, info.email);
            insertPs.setString(5, info.googleId);
            insertPs.executeUpdate();

            ResultSet keys = insertPs.getGeneratedKeys();
            if (keys.next()) {
                currentUser = new Student(
                        keys.getInt(1), prenom, fullName,
                        username, info.email, null,
                        true, java.time.LocalDate.now().toString(),
                        info.googleId, null
                );
            } else {
                throw new SQLException("Failed to insert Google user");
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
                new StudentService().updateDateLastConnexion(currentUser.getId());
            }
            RememberMeManager.clearCredentials();
            currentUser = null;
        }catch (Exception e) {
            System.out.println(e.getMessage());
            }
        }

    public static void updateLastKnownIp(int userId, String ip) throws SQLException {
        String sql = "UPDATE `user` SET `last_known_ip` = ? WHERE `id` = ?";
        PreparedStatement ps = DataBase.getInstance().getConnection().prepareStatement(sql);
        ps.setString(1, ip);
        ps.setInt(2, userId);
        ps.executeUpdate();
    }


    public static String getLastKnownIp(int userId) throws SQLException {
        String sql = "SELECT `last_known_ip` FROM `user` WHERE `id` = ?";
        PreparedStatement ps = DataBase.getInstance().getConnection().prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("last_known_ip");
        }
        return null;
    }



}