package services;

import models.bourses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class boursesService extends AbstractService<bourses> {

    @Override
    public void add(bourses b) {
        String query = "INSERT INTO bourses (titre, description, image, date_attribution, date_fin, montant) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, b.getTitre());
            pstmt.setString(2, b.getDescription());
            pstmt.setString(3, b.getImage());
            pstmt.setTimestamp(4, b.getDate_attribution());
            pstmt.setTimestamp(5, b.getDate_fin());
            pstmt.setDouble(6, b.getMontant());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(bourses b) {
        String query = "UPDATE bourses SET titre=?, description=?, image=?, date_attribution=?, date_fin=?, montant=? WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, b.getTitre());
            pstmt.setString(2, b.getDescription());
            pstmt.setString(3, b.getImage());
            pstmt.setTimestamp(4, b.getDate_attribution());
            pstmt.setTimestamp(5, b.getDate_fin());
            pstmt.setDouble(6, b.getMontant());
            pstmt.setInt(7, b.getId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("⚠️ Aucune ligne mise à jour. ID introuvable : " + b.getId());
            } else {
                System.out.println("✅ Mise à jour réussie en base pour l'ID : " + b.getId());
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'UPDATE : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM bourses WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public bourses getById(int id) {
        String query = "SELECT * FROM bourses WHERE id=?";
        bourses b = null;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    b = new bourses();
                    b.setId(rs.getInt("id"));
                    b.setTitre(rs.getString("titre"));
                    b.setDescription(rs.getString("description"));
                    b.setImage(rs.getString("image"));
                    b.setDate_attribution(rs.getTimestamp("date_attribution"));
                    b.setDate_fin(rs.getTimestamp("date_fin"));
                    b.setMontant(rs.getDouble("montant"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return b;
    }

    @Override
    public List<bourses> getAll() {
        String query = "SELECT * FROM bourses";
        List<bourses> list = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bourses b = new bourses();
                    b.setId(rs.getInt("id"));
                    b.setTitre(rs.getString("titre"));
                    b.setDescription(rs.getString("description"));
                    b.setImage(rs.getString("image"));
                    b.setDate_attribution(rs.getTimestamp("date_attribution"));
                    b.setDate_fin(rs.getTimestamp("date_fin"));
                    b.setMontant(rs.getDouble("montant"));
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
