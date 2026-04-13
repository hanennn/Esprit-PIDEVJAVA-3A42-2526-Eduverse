package services;

import models.demande;
import org.junit.jupiter.api.*;
import java.sql.Timestamp;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DemandeServiceTest {
    static demandeService service = new demandeService();
    static int testId;

    @Test @Order(1)
    void testAdd() {
        demande d = new demande();
        d.setNiveau_etudes("Licence");
        d.setStatut("En attente");
        d.setLettre_motivation("Ceci est une lettre de motivation de test ...");
        d.setNote("15");
        d.setEtudiant_id(1);
        d.setBourse_id(1);
        d.setDate_demande(new Timestamp(System.currentTimeMillis()));
        
        int before = service.getAll().size();
        service.add(d);
        assertEquals(before + 1, service.getAll().size());
    }

    @Test @Order(2)
    void testGetAll() {
        assertNotNull(service.getAll());
        assertFalse(service.getAll().isEmpty());
    }

    @Test @Order(3)
    void testUpdate() {
        demande last = service.getAll().get(service.getAll().size() - 1);
        testId = last.getId();
        last.setStatut("Acceptée");
        service.update(last);
        assertEquals("Acceptée", service.getById(testId).getStatut());
    }

    @Test @Order(4)
    void testDelete() {
        service.delete(testId);
        assertNull(service.getById(testId));
    }
}
