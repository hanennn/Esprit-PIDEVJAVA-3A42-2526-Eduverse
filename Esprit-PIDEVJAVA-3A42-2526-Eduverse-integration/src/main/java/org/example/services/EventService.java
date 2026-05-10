package org.example.services;

import org.example.entities.Event;
import java.sql.*;
import java.util.*;

public class EventService extends AbstractService<Event> {

    @Override
    public void add(Event event) {
        String req = "INSERT INTO `event` (titre, description, type, lien_webinaire, niveau, date, heure_deb, heure_fin, date_creation, image, lieu) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setString(1, event.getTitre());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getType());
            ps.setString(4, event.getLienWebinaire());
            ps.setString(5, event.getNiveau());
            ps.setDate(6, event.getDate());
            ps.setTime(7, event.getHeureDeb());
            ps.setTime(8, event.getHeureFin());
            ps.setTimestamp(9, event.getDateCreation());
            ps.setString(10, event.getImage());
            ps.setString(11, event.getLieu());
            ps.executeUpdate();
            System.out.println("Événement ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @Override
    public void update(Event event) {
        String req = "UPDATE `event` SET titre=?, description=?, type=?, lien_webinaire=?, "
                + "niveau=?, date=?, heure_deb=?, heure_fin=?, date_creation=?, image=?, lieu=? WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setString(1, event.getTitre());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getType());
            ps.setString(4, event.getLienWebinaire());
            ps.setString(5, event.getNiveau());
            ps.setDate(6, event.getDate());
            ps.setTime(7, event.getHeureDeb());
            ps.setTime(8, event.getHeureFin());
            ps.setTimestamp(9, event.getDateCreation());
            ps.setString(10, event.getImage());
            ps.setString(11, event.getLieu());
            ps.setInt(12, event.getId());
            ps.executeUpdate();
            System.out.println("Événement mis à jour avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM `event` WHERE id=" + id;
        try {
            Statement st = connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Événement supprimé avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @Override
    public Event getById(int id) {
        Event event = null;
        String req = "SELECT * FROM `event` WHERE id=" + id;
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            if (rs.next()) {
                event = mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return event;
    }

    @Override
    public List<Event> getAll() {
        List<Event> events = new ArrayList<>();
        String req = "SELECT * FROM `event`";
        try {
            if (connection == null) return events;
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                events.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la liste: " + e.getMessage());
        }
        return events;
    }

    public List<Map<String, Object>> getHistoricalData() {
        List<Map<String, Object>> data = new ArrayList<>();
        String req = "SELECT e.titre, COUNT(i.id) as nbInscrits " +
                     "FROM `event` e LEFT JOIN event_inscription i ON e.id = i.event_id " +
                     "GROUP BY e.id";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("titre", rs.getString("titre"));
                row.put("nbInscrits", rs.getInt("nbInscrits"));
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur historical data: " + e.getMessage());
        }
        return data;
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        return new Event(
                rs.getInt("id"),
                rs.getString("titre"),
                rs.getString("description"),
                rs.getString("type"),
                rs.getString("lien_webinaire"),
                rs.getString("niveau"),
                rs.getDate("date"),
                rs.getTime("heure_deb"),
                rs.getTime("heure_fin"),
                rs.getTimestamp("date_creation"),
                rs.getString("image"),
                rs.getString("lieu")
        );
    }
}
