package services;

import models.Event;
import java.sql.*;
import java.util.*;

public class EventService extends AbstractService<Event> {

    public EventService() {
        super();
        try {
            // Tentative d'ajout de la colonne lieu si elle n'existe pas (auto-migration)
            Statement st = connection.createStatement();
            st.executeUpdate("ALTER TABLE event ADD COLUMN lieu VARCHAR(255)");
        } catch (SQLException e) {
            // L'erreur est ignorée car la colonne existe probablement déjà
        }
    }

    @Override
    public void add(Event event) {
        String req = "INSERT INTO event (titre, description, type, lien_webinaire, niveau, date, heure_deb, heure_fin, date_creation, image, lieu) VALUES ('"
                + event.getTitre() + "', '"
                + event.getDescription() + "', '"
                + event.getType() + "', '"
                + event.getLienWebinaire() + "', '"
                + event.getNiveau() + "', '"
                + event.getDate() + "', '"
                + event.getHeureDeb() + "', '"
                + event.getHeureFin() + "', '"
                + event.getDateCreation() + "', '"
                + event.getImage() + "', '"
                + event.getLieu() + "')";
        try {
            Statement st = connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Evénement ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @Override
    public void update(Event event) {
        String req = "UPDATE event SET titre='" + event.getTitre() + "', description='" + event.getDescription()
                + "', type='" + event.getType() + "', lien_webinaire='" + event.getLienWebinaire()
                + "', niveau='" + event.getNiveau() + "', date='" + event.getDate()
                + "', heure_deb='" + event.getHeureDeb() + "', heure_fin='" + event.getHeureFin()
                + "', date_creation='" + event.getDateCreation() + "', image='" + event.getImage()
                + "', lieu='" + event.getLieu() + "' WHERE id=" + event.getId();
        try {
            Statement st = connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Evénement mis à jour avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM event WHERE id=" + id;
        try {
            Statement st = connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Evénement supprimé avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @Override
    public Event getById(int id) {
        Event event = null;
        String req = "SELECT * FROM event WHERE id=" + id;
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
        String req = "SELECT * FROM event";
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

    public int getTotalEvents() {
        String req = "SELECT COUNT(*) FROM event";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur getTotalEvents: " + e.getMessage());
        }
        return 0;
    }

    public Map<String, Integer> getParticipantCountPerEvent() {
        Map<String, Integer> result = new LinkedHashMap<>();
        String req = "SELECT e.titre, COUNT(ei.id) as count FROM event e LEFT JOIN event_inscription ei ON e.id = ei.event_id GROUP BY e.id, e.titre";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                result.put(rs.getString("titre"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getParticipantCountPerEvent: " + e.getMessage());
        }
        return result;
    }

    public List<Map<String, Object>> getHistoricalData() {
        List<Map<String, Object>> result = new ArrayList<>();
        String req =
            "SELECT e.id, e.titre, " +
            "       COUNT(ei.id) AS nb_inscrits " +
            "FROM event e " +
            "LEFT JOIN event_inscription ei ON ei.event_id = e.id " +
            "GROUP BY e.id, e.titre " +
            "ORDER BY e.date DESC";
        try {
            if (connection == null) return result;
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("titre",        rs.getString("titre"));
                row.put("nbInscrits",   rs.getInt("nb_inscrits"));
                result.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getHistoricalData: " + e.getMessage());
        }
        return result;
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        String lieuVal = null;
        try {
            lieuVal = rs.getString("lieu");
        } catch(SQLException ignored) {}
        
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
                lieuVal
        );
    }
}
