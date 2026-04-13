package Services;

import entities.Admin;
import utils.DataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.List;
import java.util.ArrayList;

public class AdminService {
    Connection connection ;
    public AdminService() {
        this.connection = DataBase.getInstance().getConnection();
    }

    public void AjouterAdmin(Admin admin) throws SQLException {
        String sql = "INSERT INTO `user`(`username`, `roles`, `google_id`, `password`, `nom`, `prenom`, `email`, `is_active`, `date_inscription`, `date_derniere_connexion`, `specialite`, `experience`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, admin.getUserName());
            ps.setString(2, "[\"ROLE_ADMIN\"]");
            ps.setString(3, null);
            ps.setString(4, admin.getPassword());
            ps.setString(5, admin.getLastName());
            ps.setString(6, admin.getFirstName());
            ps.setString(7, admin.getEmail());
            ps.setBoolean(8, admin.getIsActive());
            ps.setString(9, null);
            ps.setString(10, null);
            ps.setString(11, null);

            ps.executeUpdate();
            System.out.println("Admin added successfully!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Admin> AfficherAdmin() throws SQLException {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT * FROM `user` WHERE `roles` LIKE '%ROLE_ADMIN%'";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Admin admin = new Admin(
                    rs.getInt("id"),
            rs.getString("username"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getBoolean("is_active"),
            rs.getString("date_inscription")
        );
            admins.add(admin);
        }
        return admins;
    }


    public void ModifierAdmin(Admin admin) throws SQLException{
        String sql = "UPDATE `user` SET `username`=?, `password`=?, `nom`=?, `prenom`=?, `email`=?, `is_active`=? WHERE `id`=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, admin.getUserName());
            ps.setString(2, admin.getPassword());
            ps.setString(3, admin.getLastName());
            ps.setString(4, admin.getFirstName());
            ps.setString(5, admin.getEmail());
            ps.setBoolean(6, admin.getIsActive());
            ps.setInt(7, admin.getId());

            ps.executeUpdate();
            System.out.println("Admin modifier");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public void SupprimerAdmin(int id) throws SQLException{
        String sql = "DELETE FROM `user` WHERE `id`=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);

            ps.executeUpdate();
            System.out.println("Admin supprimé");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }



    public Admin FindAdminByUsername(String username) throws SQLException{
        String sql = "SELECT * FROM `user` WHERE `roles` LIKE '%ROLE_ADMIN%' AND `username` = ?";
        PreparedStatement request = connection.prepareStatement(sql);
        request.setString(1, username);
        ResultSet response = request.executeQuery();
        if (response.next()) {
            return new Admin(
                    response.getInt("id"),
                    response.getString("username"),
                    response.getString("nom"),
                    response.getString("prenom"),
                    response.getString("email"),
                    response.getString("password"),
                    response.getBoolean("is_active"),
                    response.getString("date_inscription")
            );
        }
        return null;
    }

    public Boolean userNameExists (String username) throws SQLException{
        String sql = "SELECT * FROM `user` WHERE `username` = ?";
        PreparedStatement request = connection.prepareStatement(sql);
        request.setString(1, username);
        ResultSet response = request.executeQuery();
        if (response.next()) {
            return true;
        }
        return false;
    }

    public Boolean EmailExists (String email) throws SQLException{
        String sql = "SELECT * FROM `user` WHERE `email` = ?";
        PreparedStatement request = connection.prepareStatement(sql);
        request.setString(1, email);
        ResultSet response = request.executeQuery();
        if (response.next()) {
            return true;
        }
        return false;
    }

}
