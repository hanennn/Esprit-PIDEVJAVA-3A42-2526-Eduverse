package services;

import models.EventInscription;
import java.sql.*;
import java.util.*;

public class EventInscriptionService extends AbstractService<EventInscription> {

    @Override
    public void add(EventInscription inscription) {
        String req = "INSERT INTO event_inscription "
                + "(date_inscription, statut, participant_id, event_id) "
                + "VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setTimestamp(1, inscription.getDateInscription());
            ps.setString(2, inscription.getStatut());
            ps.setInt(3, inscription.getParticipantId());
            ps.setInt(4, inscription.getEventId());
            ps.executeUpdate();
            System.out.println("Inscription ajoutée avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @Override
    public void update(EventInscription inscription) {
        String req = "UPDATE event_inscription SET date_inscription=?, statut=?, "
                + "participant_id=?, event_id=? WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setTimestamp(1, inscription.getDateInscription());
            ps.setString(2, inscription.getStatut());
            ps.setInt(3, inscription.getParticipantId());
            ps.setInt(4, inscription.getEventId());
            ps.setInt(5, inscription.getId());
            ps.executeUpdate();
            System.out.println("Inscription mise à jour avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM event_inscription WHERE id=" + id;
        try {
            Statement st = connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Inscription supprimée avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @Override
    public EventInscription getById(int id) {
        EventInscription inscription = null;
        String req = "SELECT * FROM event_inscription WHERE id=" + id;
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            if (rs.next()) {
                inscription = mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return inscription;
    }

    @Override
    public List<EventInscription> getAll() {
        List<EventInscription> inscriptions = new ArrayList<>();
        String req = "SELECT * FROM event_inscription";
        try {
            if (connection == null) return inscriptions;
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                inscriptions.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la liste: " + e.getMessage());
        }
        return inscriptions;
    }

    public int getTotalInscriptions() {
        String req = "SELECT COUNT(*) FROM event_inscription";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur getTotalInscriptions: " + e.getMessage());
        }
        return 0;
    }

    public Map<String, Integer> getInscriptionCountByStatus() {
        Map<String, Integer> result = new HashMap<>();
        String req = "SELECT statut, COUNT(*) as count FROM event_inscription GROUP BY statut";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                result.put(rs.getString("statut"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getInscriptionCountByStatus: " + e.getMessage());
        }
        return result;
    }

    private EventInscription mapRow(ResultSet rs) throws SQLException {
        return new EventInscription(
                rs.getInt("id"),
                rs.getTimestamp("date_inscription"),
                rs.getString("statut"),
                rs.getInt("participant_id"),
                rs.getInt("event_id")
        );
    }
}
