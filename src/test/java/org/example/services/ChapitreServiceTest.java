package org.example.services;

import org.example.entities.chapitres;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChapitresServiceTest {

    static chapitresservices service;

    @BeforeAll
    static void setup() {
        service = new chapitresservices();
    }

    /*
    @AfterEach
    void cleanUp() throws SQLException {
        List<chapitres> liste = service.afficher();

        if (!liste.isEmpty()) {
            chapitres last = liste.get(liste.size() - 1);

            service.supprimer(last.getId());
        }
    }
    */

    @Test
    @Order(1)
    void addChapitreToCourse15() throws SQLException {
        int coursId = 15; // 🔥 cours existant en base
        chapitres ch = new chapitres("Introduction Java", "Les bases du langage Java", 1, "1h30", "actif", "contenu test", "pdf", ""
        );
        ch.setCours_id(coursId);
        chapitresservices service = new chapitresservices();
        service.ajouter(ch);

        // vérification
        List<chapitres> liste = service.afficher();

        assertTrue(liste.stream().anyMatch(x -> x.getTitre_chap().equals("Introduction Java") &&
                        x.getCours_id() == 15
                )
        );
    }

    @Test
    @Order(2)
    void updateChapitre() throws SQLException {

        int coursId = 15;
        chapitres ch = new chapitres();
        ch.setTitre_chap("Java Avancé");
        ch.setDesc_chap("Cours avancé Java");
        ch.setCours_id(coursId);

        chapitresservices service = new chapitresservices();
        service.modifier(9, ch);
        List<chapitres> liste = service.afficher();

        assertTrue(liste.stream().anyMatch(x -> x.getTitre_chap().equals("Java Avancé"))
        );
    }

    @Test
    @Order(3)
    void deleteChapitre() throws SQLException {
        service.supprimer(12);
        List<chapitres> liste = service.afficher();
        boolean existe = liste.stream().anyMatch(ch -> ch.getId() == 1);
        assertFalse(existe);
    }
}