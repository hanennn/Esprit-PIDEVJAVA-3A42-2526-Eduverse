package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests du modèle EventInscription")
class EventInscriptionTest {

    private EventInscription inscription;
    private java.sql.Timestamp dateInscription;

    @BeforeEach
    void setUp() {
        dateInscription = java.sql.Timestamp.valueOf("2025-03-10 14:00:00");
        inscription = new EventInscription(1, dateInscription, "Confirmé", 4, "Très bon événement", 101, 5);
    }

    // ── Constructeur complet ───────────────────────────────────────────────

    @Test
    @DisplayName("Le constructeur complet initialise bien tous les champs")
    void testConstructeurComplet() {
        assertAll("Vérification des champs après construction",
                () -> assertEquals(1, inscription.getId()),
                () -> assertEquals(dateInscription, inscription.getDateInscription()),
                () -> assertEquals("Confirmé", inscription.getStatut()),
                () -> assertEquals(4, inscription.getNote()),
                () -> assertEquals("Très bon événement", inscription.getCommentaire()),
                () -> assertEquals(101, inscription.getParticipantId()),
                () -> assertEquals(5, inscription.getEventId())
        );
    }

    // ── Constructeur vide ──────────────────────────────────────────────────

    @Test
    @DisplayName("Le constructeur vide crée un objet avec des valeurs nulles/défaut")
    void testConstructeurVide() {
        EventInscription ei = new EventInscription();
        assertAll(
                () -> assertEquals(0, ei.getId()),
                () -> assertNull(ei.getDateInscription()),
                () -> assertNull(ei.getStatut()),
                () -> assertNull(ei.getNote()),
                () -> assertNull(ei.getCommentaire()),
                () -> assertEquals(0, ei.getParticipantId()),
                () -> assertEquals(0, ei.getEventId())
        );
    }

    // ── Setters ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("setId modifie correctement l'identifiant")
    void testSetId() {
        inscription.setId(99);
        assertEquals(99, inscription.getId());
    }

    @Test
    @DisplayName("setDateInscription modifie correctement la date")
    void testSetDateInscription() {
        java.sql.Timestamp nouvelleDate = java.sql.Timestamp.valueOf("2026-06-01 09:00:00");
        inscription.setDateInscription(nouvelleDate);
        assertEquals(nouvelleDate, inscription.getDateInscription());
    }

    @Test
    @DisplayName("setStatut modifie correctement le statut")
    void testSetStatut() {
        inscription.setStatut("En attente");
        assertEquals("En attente", inscription.getStatut());
    }

    @Test
    @DisplayName("setNote modifie correctement la note")
    void testSetNote() {
        inscription.setNote(5);
        assertEquals(5, inscription.getNote());
    }

    @Test
    @DisplayName("setNote avec valeur nulle est acceptée")
    void testSetNoteNull() {
        inscription.setNote(null);
        assertNull(inscription.getNote());
    }

    @Test
    @DisplayName("setCommentaire modifie correctement le commentaire")
    void testSetCommentaire() {
        inscription.setCommentaire("Moyen");
        assertEquals("Moyen", inscription.getCommentaire());
    }

    @Test
    @DisplayName("setParticipantId modifie correctement l'identifiant du participant")
    void testSetParticipantId() {
        inscription.setParticipantId(202);
        assertEquals(202, inscription.getParticipantId());
    }

    @Test
    @DisplayName("setEventId modifie correctement l'identifiant de l'événement")
    void testSetEventId() {
        inscription.setEventId(10);
        assertEquals(10, inscription.getEventId());
    }

    // ── Valeurs limites ────────────────────────────────────────────────────

    @Test
    @DisplayName("Le statut peut être 'Annulé'")
    void testStatutAnnule() {
        inscription.setStatut("Annulé");
        assertEquals("Annulé", inscription.getStatut());
    }

    @Test
    @DisplayName("Le statut peut être null")
    void testStatutNull() {
        inscription.setStatut(null);
        assertNull(inscription.getStatut());
    }

    // ── toString ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("toString contient les informations clés de l'inscription")
    void testToString() {
        java.lang.String result = inscription.toString();
        assertAll(
                () -> assertTrue(result.contains("id=1")),
                () -> assertTrue(result.contains("Confirmé")),
                () -> assertTrue(result.contains("note=4")),
                () -> assertTrue(result.contains("Très bon événement")),
                () -> assertTrue(result.contains("participantId=101")),
                () -> assertTrue(result.contains("eventId=5"))
        );
    }
}
