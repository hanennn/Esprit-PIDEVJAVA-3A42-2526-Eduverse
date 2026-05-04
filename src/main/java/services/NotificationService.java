package services;

import models.Notification;
import utils.DataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    private final Connection connection;

    public NotificationService() {
        this.connection = DataBase.getInstance().getConnection();
    }

    public void creer(int demandeId, String message) {
        String query = "INSERT INTO notification (demande_id, message, lu, date_creation) VALUES (?, ?, false, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, demandeId);
            pstmt.setString(2, message);
            pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> getNonLues() {
        String query = "SELECT * FROM notification WHERE lu = false ORDER BY date_creation DESC";
        return execQuery(query);
    }

    public List<Notification> getAll() {
        String query = "SELECT * FROM notification ORDER BY date_creation DESC";
        return execQuery(query);
    }

    public int compterNonLues() {
        String query = "SELECT COUNT(*) FROM notification WHERE lu = false";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void marquerLue(int id) {
        String query = "UPDATE notification SET lu = true WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void marquerToutesLues() {
        String query = "UPDATE notification SET lu = true WHERE lu = false";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Notification> execQuery(String query) {
        List<Notification> list = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getInt("id"));
                n.setDemandeId(rs.getInt("demande_id"));
                n.setMessage(rs.getString("message"));
                n.setLu(rs.getBoolean("lu"));
                n.setDateCreation(rs.getTimestamp("date_creation"));
                list.add(n);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
