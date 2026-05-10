package org.example.services;

import org.example.entities.CertificationFinale;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CertificationFinaleServiceTest {

    static CertificationFinaleService service;
    static int idFinaleTest;
    static final int TENTATIVE_ID = 3;

    @BeforeAll
    static void setup() {
        service = new CertificationFinaleService();
    }

    @Test
    @Order(1)
    void testAjouterCertificationFinale() throws Exception {
        // Nettoyer doublon éventuel
        if (service.existeDeja(TENTATIVE_ID)) {
            List<CertificationFinale> list = service.afficher();
            list.stream()
                    .filter(c -> c.getTentativeId() == TENTATIVE_ID)
                    .forEach(c -> {
                        try { service.supprimer(c.getId()); }
                        catch (Exception ignored) {}
                    });
        }

        CertificationFinale c = new CertificationFinale(
                new Timestamp(System.currentTimeMillis()),
                "BadgeTestFinal",
                1,
                11,
                TENTATIVE_ID
        );

        service.ajouter(c);

        List<CertificationFinale> list = service.afficher();
        assertFalse(list.isEmpty());
        assertTrue(
                list.stream().anyMatch(cert ->
                        cert.getBadge().equals("BadgeTestFinal"))
        );

        CertificationFinale finale = list.stream()
                .filter(cert -> cert.getBadge().equals("BadgeTestFinal"))
                .reduce((first, second) -> second)
                .orElse(null);

        assertNotNull(finale);
        idFinaleTest = finale.getId();
        System.out.println(
                "CertificationFinale ajoutée avec ID : " + idFinaleTest);
    }

    @Test
    @Order(2)
    void testDoublonCertificationFinale() {
        // Tenter d'ajouter un doublon → doit lever Exception
        Exception exception = assertThrows(Exception.class, () -> {
            CertificationFinale doublon = new CertificationFinale(
                    new Timestamp(System.currentTimeMillis()),
                    "AutreBadge",
                    1,
                    11,
                    TENTATIVE_ID
            );
            service.ajouter(doublon);
        });
        assertTrue(exception.getMessage().contains("existe déjà"));
        System.out.println("Doublon détecté : " + exception.getMessage());
    }

    @Test
    @Order(3)
    void testModifierCertificationFinale() throws Exception {
        if (idFinaleTest == 0) {
            List<CertificationFinale> list = service.afficher();
            CertificationFinale found = list.stream()
                    .filter(c -> c.getBadge().equals("BadgeTestFinal"))
                    .findFirst().orElse(null);
            if (found != null) idFinaleTest = found.getId();
        }

        CertificationFinale c = new CertificationFinale();
        c.setId(idFinaleTest);
        c.setDateEmission(new Timestamp(System.currentTimeMillis()));
        c.setBadge("BadgeModifieFinal");
        c.setUserId(1);
        c.setQuizId(11);
        c.setTentativeId(TENTATIVE_ID);

        service.modifier(c);

        List<CertificationFinale> list = service.afficher();
        boolean trouve = list.stream()
                .anyMatch(cert ->
                        cert.getId() == idFinaleTest &&
                                cert.getBadge().equals("BadgeModifieFinal")
                );

        assertTrue(trouve);
        System.out.println("CertificationFinale modifiée avec succès");
    }

    @Test
    @Order(4)
    void testSupprimerCertificationFinale() throws Exception {
        if (idFinaleTest == 0) {
            List<CertificationFinale> list = service.afficher();
            CertificationFinale found = list.stream()
                    .filter(c -> c.getBadge().equals("BadgeModifieFinal"))
                    .findFirst().orElse(null);
            if (found != null) idFinaleTest = found.getId();
        }

        service.supprimer(idFinaleTest);

        List<CertificationFinale> list = service.afficher();
        boolean existe = list.stream()
                .anyMatch(cert -> cert.getId() == idFinaleTest);

        assertFalse(existe);
        System.out.println("CertificationFinale supprimée avec succès");
    }
}