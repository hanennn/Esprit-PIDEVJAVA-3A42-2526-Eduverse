package org.example;

import models.bourses;
import models.demande;
import services.boursesService;
import services.demandeService;

import java.sql.Timestamp;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- TEST BOURSES ---");
        boursesService bService = new boursesService();

        // 1. ADD
        System.out.println("=> Ajout d'une bourse...");
        bourses b = new bourses();
        b.setTitre("Bourse Excellence 2026");
        b.setDescription("Bourse attribuée pour l'excellence académique.");
        b.setImage("excellence.png");
        b.setDate_attribution(new Timestamp(System.currentTimeMillis()));
        b.setDate_fin(new Timestamp(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 30))); // + 30 jours
        b.setMontant(1500.50);
        bService.add(b);

        // 2. GET ALL
        System.out.println("\n=> Liste des bourses:");
        List<bourses> allBourses = bService.getAll();
        for (bourses bourse : allBourses) {
            System.out.println(bourse);
        }

        if (!allBourses.isEmpty()) {
            int lastBourseId = allBourses.get(allBourses.size() - 1).getId();

            // 3. GET BY ID
            System.out.println("\n=> Recherche de la bourse ID: " + lastBourseId);
            bourses foundBourse = bService.getById(lastBourseId);
            System.out.println(foundBourse);

            if (foundBourse != null) {
                // 4. UPDATE
                System.out.println("\n=> Mise à jour de la bourse ID: " + lastBourseId);
                foundBourse.setMontant(2000.00);
                bService.update(foundBourse);
                System.out.println("Après mise à jour: " + bService.getById(lastBourseId));

                // 5. DELETE
                System.out.println("\n=> Suppression de la bourse ID: " + lastBourseId);
                bService.delete(lastBourseId);
                System.out.println("Résultat de la recherche après suppression: " + bService.getById(lastBourseId));
            }
        }


        System.out.println("\n\n--- TEST DEMANDES ---");
        demandeService dService = new demandeService();

        bourses tempBourse = new bourses();
        tempBourse.setTitre("Bourse de test pour demande");
        tempBourse.setDate_attribution(new Timestamp(System.currentTimeMillis()));
        tempBourse.setDate_fin(new Timestamp(System.currentTimeMillis() + 86400000L));
        tempBourse.setMontant(1200.0);
        bService.add(tempBourse);
        
        List<bourses> bb = bService.getAll();
        int targetBourseId = bb.get(bb.size() - 1).getId();

        // 1. ADD
        System.out.println("=> Ajout d'une demande...");
        demande d = new demande();
        d.setDate_demande(new Timestamp(System.currentTimeMillis()));
        d.setNiveau_etudes("Master 1");
        d.setStatut("En attente");
        d.setLettre_motivation("Je suis très motivé pour obtenir cette bourse.");
        d.setNote("Dossier à étudier");
        d.setEtudiant_id(1); // Pas de clé étrangère configurée pour étudiant
        d.setBourse_id(targetBourseId);   // <-- ICI: Utilise l'ID de la bourse dynamique
        dService.add(d);

        // 2. GET ALL
        System.out.println("\n=> Liste des demandes:");
        List<demande> allDemandes = dService.getAll();
        for (demande demande : allDemandes) {
            System.out.println(demande);
        }

        if (!allDemandes.isEmpty()) {
            int lastDemandeId = allDemandes.get(allDemandes.size() - 1).getId();

            // 3. GET BY ID
            System.out.println("\n=> Recherche de la demande ID: " + lastDemandeId);
            demande foundDemande = dService.getById(lastDemandeId);
            System.out.println(foundDemande);

            if (foundDemande != null) {
                // 4. UPDATE
                System.out.println("\n=> Mise à jour de la demande ID: " + lastDemandeId);
                foundDemande.setStatut("Acceptée");
                dService.update(foundDemande);
                System.out.println("Après mise à jour: " + dService.getById(lastDemandeId));

                // 5. DELETE
                System.out.println("\n=> Suppression de la demande ID: " + lastDemandeId);
                dService.delete(lastDemandeId);
                System.out.println("Résultat de la recherche après suppression: " + dService.getById(lastDemandeId));
            }
        }
    }
}