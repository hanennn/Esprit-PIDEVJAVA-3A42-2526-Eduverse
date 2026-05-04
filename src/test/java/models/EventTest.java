package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests du modèle Event")
class EventTest {

    private Event event;
    private java.sql.Date date;
    private java.sql.Time heureDeb;
    private java.sql.Time heureFin;
    private java.sql.Timestamp dateCreation;

    @BeforeEach
    void setUp() {
        date = java.sql.Date.valueOf("2025-06-15");
        heureDeb = java.sql.Time.valueOf("09:00:00");
        heureFin = java.sql.Time.valueOf("11:00:00");
        dateCreation = java.sql.Timestamp.valueOf("2025-01-01 10:00:00");

        event = new Event(1, "JavaFX Workshop", "Atelier sur JavaFX",
                "Webinaire", "https://zoom.us/xyz", "Avancé",
                date, heureDeb, heureFin, dateCreation, "image.png");
    }

    // ── Constructeur complet ───────────────────────────────────────────────

    @Test
    @DisplayName("Le constructeur complet initialise bien tous les champs")
    void testConstructeurComplet() {
        assertAll("Vérification des champs après construction",
                () -> assertEquals(1, event.getId()),
                () -> assertEquals("JavaFX Workshop", event.getTitre()),
                () -> assertEquals("Atelier sur JavaFX", event.getDescription()),
                () -> assertEquals("Webinaire", event.getType()),
                () -> assertEquals("https://zoom.us/xyz", event.getLienWebinaire()),
                () -> assertEquals("Avancé", event.getNiveau()),
                () -> assertEquals(date, event.getDate()),
                () -> assertEquals(heureDeb, event.getHeureDeb()),
                () -> assertEquals(heureFin, event.getHeureFin()),
                () -> assertEquals(dateCreation, event.getDateCreation()),
                () -> assertEquals("image.png", event.getImage())
        );
    }

    // ── Constructeur vide ──────────────────────────────────────────────────

    @Test
    @DisplayName("Le constructeur vide crée un objet avec des valeurs nulles/défaut")
    void testConstructeurVide() {
        Event e = new Event();
        assertAll(
                () -> assertEquals(0, e.getId()),
                () -> assertNull(e.getTitre()),
                () -> assertNull(e.getDescription()),
                () -> assertNull(e.getType()),
                () -> assertNull(e.getLienWebinaire()),
                () -> assertNull(e.getNiveau()),
                () -> assertNull(e.getDate()),
                () -> assertNull(e.getHeureDeb()),
                () -> assertNull(e.getHeureFin()),
                () -> assertNull(e.getDateCreation()),
                () -> assertNull(e.getImage())
        );
    }

    // ── Setters ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("setId modifie correctement l'identifiant")
    void testSetId() {
        event.setId(42);
        assertEquals(42, event.getId());
    }

    @Test
    @DisplayName("setTitre modifie correctement le titre")
    void testSetTitre() {
        event.setTitre("Nouveau Titre");
        assertEquals("Nouveau Titre", event.getTitre());
    }

    @Test
    @DisplayName("setDescription modifie correctement la description")
    void testSetDescription() {
        event.setDescription("Nouvelle description");
        assertEquals("Nouvelle description", event.getDescription());
    }

    @Test
    @DisplayName("setType modifie correctement le type")
    void testSetType() {
        event.setType("Présentiel");
        assertEquals("Présentiel", event.getType());
    }

    @Test
    @DisplayName("setLienWebinaire modifie correctement le lien")
    void testSetLienWebinaire() {
        event.setLienWebinaire("https://teams.ms/abc");
        assertEquals("https://teams.ms/abc", event.getLienWebinaire());
    }

    @Test
    @DisplayName("setNiveau modifie correctement le niveau")
    void testSetNiveau() {
        event.setNiveau("Débutant");
        assertEquals("Débutant", event.getNiveau());
    }

    @Test
    @DisplayName("setDate modifie correctement la date")
    void testSetDate() {
        java.sql.Date nouvelleDate = java.sql.Date.valueOf("2026-12-01");
        event.setDate(nouvelleDate);
        assertEquals(nouvelleDate, event.getDate());
    }

    @Test
    @DisplayName("setHeureDeb modifie correctement l'heure de début")
    void testSetHeureDeb() {
        java.sql.Time nouvelleHeure = java.sql.Time.valueOf("14:30:00");
        event.setHeureDeb(nouvelleHeure);
        assertEquals(nouvelleHeure, event.getHeureDeb());
    }

    @Test
    @DisplayName("setHeureFin modifie correctement l'heure de fin")
    void testSetHeureFin() {
        java.sql.Time nouvelleHeure = java.sql.Time.valueOf("16:00:00");
        event.setHeureFin(nouvelleHeure);
        assertEquals(nouvelleHeure, event.getHeureFin());
    }

    @Test
    @DisplayName("setDateCreation modifie correctement le timestamp de création")
    void testSetDateCreation() {
        java.sql.Timestamp ts = java.sql.Timestamp.valueOf("2026-01-15 08:30:00");
        event.setDateCreation(ts);
        assertEquals(ts, event.getDateCreation());
    }

    @Test
    @DisplayName("setImage modifie correctement le chemin de l'image")
    void testSetImage() {
        event.setImage("photo.jpg");
        assertEquals("photo.jpg", event.getImage());
    }

    // ── toString ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("toString contient les informations clés de l'événement")
    void testToString() {
        java.lang.String result = event.toString();
        assertAll(
                () -> assertTrue(result.contains("JavaFX Workshop")),
                () -> assertTrue(result.contains("Atelier sur JavaFX")),
                () -> assertTrue(result.contains("Webinaire")),
                () -> assertTrue(result.contains("Avancé")),
                () -> assertTrue(result.contains("id=1"))
        );
    }
}
