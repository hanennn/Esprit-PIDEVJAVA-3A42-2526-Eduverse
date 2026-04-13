package org.example;

import org.example.entities.cours;
import org.example.entities.chapitres;
import org.example.services.coursservices;
import org.example.services.chapitresservices;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {

        /*
        coursservices cs = new coursservices();
        try {
            //cs.ajouter(new cours("analyse numérique", "1ere cycle ing","math", "ce cours couvre les notions de lAN", "Français",5));
            //cs.supprimer(1);

            cours c = new cours();
            c.setTitre_cours("interpolationn");
            c.setNiv_cours("Avancé");
            c.setMatiere_cours("Mathématiques");
            c.setLangue_cours("Français");
            c.setDescription("Nouvelle description");

            coursservices service = new coursservices();
            service.modifier(2, c);
            System.out.println(service.afficher());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        */

/*



        try {
            chapServ.ajouter(new chapitres("interpolation", "chap intro",1 "1h", "ouvert","intro.pdf", "pdf",2));
            System.out.println(chapServ.afficher());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

*/
        chapitresservices chapServ = new chapitresservices();
        chapitres ch = new chapitres();
        ch.setTitre_chap("Introduction");
        ch.setDesc_chap("Chapitre introduction");
        ch.setOrdre_chap(1);
        ch.setDuree_chap("1 heure");
        ch.setStatut_chap("Ouvert");
        ch.setContenu_chap("intro.pdf");
        ch.setType_contenu("pdf");
        ch.setCours_id(2);
        chapServ.modifier(5,ch);
        System.out.println(chapServ.afficher());

/*
        try {
            chapServ.supprimer(4);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }*/
}}