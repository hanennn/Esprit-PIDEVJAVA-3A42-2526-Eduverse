package org.example.services;
import org.example.entities.cours;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CoursServiceTest {

    static coursservices service;

    @BeforeAll
    static void setup() {
        service = new coursservices();
    }
/*
    @AfterEach
    void cleanUp() throws SQLException {
        List<cours> liste = service.afficher();
        if (!liste.isEmpty()) {
            cours last = liste.get(liste.size() - 1);

            // Supprimer chapitres liés avant le cours
            chapitresservices chapitresServices = new chapitresservices();
            chapitresServices.supprimer(last.getId());

            service.supprimer(last.getId());
        }
    }
*/

    @Test
    @Order(1)
    void addCourse() throws SQLException {
        cours c = new cours("PHP", "2éme année prepa", "info", "ce cours est une initiation à PHP","français");
        service.ajouter(c);
        List<cours> liste = service.afficher();
        assertFalse(liste.isEmpty());
        assertTrue(liste.stream().anyMatch(cours -> cours.getTitre_cours().equals("PHP"))
        );
    }

    @Test
    @Order(3)
    void deleteCourse() throws SQLException {
        service.supprimer(13);
        List<cours> liste = service.afficher();
        boolean existe = liste.stream()
                .anyMatch(c -> c.getId() == 13);
        assertFalse(existe);
    }

    @Test
    @Order(2)
    void updateCourse()  throws SQLException {
        cours c = new cours();
        c.setTitre_cours("java");
        c.setDescription("ce cours décrit les notions de JAVA");
        service.modifier(14, c);
        List<cours> liste = service.afficher();
        boolean trouve = liste.stream()
                .anyMatch(cours -> cours.getTitre_cours().equals("java"));
        assertTrue(trouve);
    }




}
