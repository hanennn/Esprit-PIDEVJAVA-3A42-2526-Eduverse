package org.example.services;

import org.example.entities.Certification;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CertificationServiceTest {

    static CertificationService service;
    static int idCertifTest;

    @BeforeAll
    static void setup() {
        service = new CertificationService();
    }

    @Test
    @Order(1)
    void testAjouterCertification() throws Exception {
        Certification c = new Certification(
                80,
                "VALIDE",
                "BadgeTest",
                new Timestamp(System.currentTimeMillis()),
                1,
                11
        );

        service.ajouter(c);

        List<Certification> list = service.afficher();
        assertFalse(list.isEmpty());
        assertTrue(
                list.stream().anyMatch(cert ->
                        cert.getBadge().equals("BadgeTest"))
        );

        Certification certif = list.stream()
                .filter(cert -> cert.getBadge().equals("BadgeTest"))
                .reduce((first, second) -> second)
                .orElse(null);

        assertNotNull(certif);
        idCertifTest = certif.getId();
        System.out.println("Certification ajoutée avec ID : " + idCertifTest);
    }

    @Test
    @Order(2)
    void testModifierCertification() throws Exception {
        if (idCertifTest == 0) {
            List<Certification> list = service.afficher();
            Certification found = list.stream()
                    .filter(c -> c.getBadge().equals("BadgeTest"))
                    .findFirst().orElse(null);
            if (found != null) idCertifTest = found.getId();
        }

        Certification c = new Certification();
        c.setId(idCertifTest);
        c.setScoreObtenu(90);
        c.setStatut("EXCELLENT");
        c.setBadge("BadgeModifie");
        c.setDateAttribution(new Timestamp(System.currentTimeMillis()));
        c.setUserId(1);
        c.setQuizId(11);

        service.modifier(c);

        List<Certification> list = service.afficher();
        boolean trouve = list.stream()
                .anyMatch(cert ->
                        cert.getId() == idCertifTest &&
                                cert.getBadge().equals("BadgeModifie")
                );

        assertTrue(trouve);
        System.out.println("Certification modifiée avec succès");
    }

    @Test
    @Order(3)
    void testSupprimerCertification() throws Exception {
        if (idCertifTest == 0) {
            List<Certification> list = service.afficher();
            Certification found = list.stream()
                    .filter(c -> c.getBadge().equals("BadgeModifie"))
                    .findFirst().orElse(null);
            if (found != null) idCertifTest = found.getId();
        }

        service.supprimer(idCertifTest);

        List<Certification> list = service.afficher();
        boolean existe = list.stream()
                .anyMatch(cert -> cert.getId() == idCertifTest);

        assertFalse(existe);
        System.out.println("Certification supprimée avec succès");
    }
}