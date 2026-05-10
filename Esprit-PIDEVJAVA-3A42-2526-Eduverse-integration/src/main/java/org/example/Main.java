package org.example;

import org.example.entities.cours;
import org.example.entities.chapitres;
import org.example.services.coursservices;
import org.example.services.chapitresservices;
import java.sql.SQLException;
import org.example.entities.Quiz;
import org.example.entities.Question;
import org.example.services.QuizService;
import org.example.services.QuestionService;
import org.example.entities.Certification;
import org.example.services.CertificationService;
import org.example.entities.CertificationFinale;
import org.example.services.CertificationFinaleService;

import java.sql.Timestamp;

import java.util.List;

public class Main {
    public static void main(String[] args) {


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
            QuizService quizService = new QuizService();
            QuestionService questionService = new QuestionService();
            CertificationService certificationService = new CertificationService();
            CertificationFinaleService certificationFinaleService = new CertificationFinaleService();

/*
            // 1. AJOUT
            Quiz quiz = new Quiz(
                    "Quiz Java",
                    "Final",
                    30,
                    60,
                    1
            );
            quizService.ajouter(quiz);

            // 2. AFFICHAGE APRES AJOUT
            System.out.println("=== LISTE DES QUIZ APRES AJOUT ===");
            List<Quiz> liste = quizService.afficher();
            for (Quiz q : liste) {
                System.out.println(q);
            }

            // 3. MODIFICATION
            Quiz quizModifie = new Quiz(
                    1,
                    "Quiz Java Modifié",
                    "Intermédiaire",
                    45,
                    70,
                    1
            );
            quizService.modifier(quizModifie);

            // 4. AFFICHAGE APRES MODIFICATION
            System.out.println("=== LISTE DES QUIZ APRES MODIFICATION ===");
            for (Quiz q : quizService.afficher()) {
                System.out.println(q);
            }
            chapServ.ajouter(new chapitres("interpolation", "chap intro",1 "1h", "ouvert","intro.pdf", "pdf",2));
            System.out.println(chapServ.afficher());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

            // 5. SUPPRESSION
            quizService.supprimer(1);

            // 6. AFFICHAGE APRES SUPPRESSION
            System.out.println("=== LISTE DES QUIZ APRES SUPPRESSION ===");
            for (Quiz q : quizService.afficher()) {
                System.out.println(q);
            }
            //  3. AJOUT QUESTION
            Question question = new Question(
                    3, // ⚠ID du quiz existant
                    "Java est-il orienté objet ?",
                    10,
                    "[{\"texte\":\"Oui\",\"correct\":true},{\"texte\":\"Non\",\"correct\":false}]"
            );
            questionService.ajouter(question);
            // 4. AFFICHAGE QUESTIONS APRES AJOUT
            System.out.println("=== LISTE DES QUESTIONS APRES AJOUT ===");
            List<Question> listeQuestions = questionService.afficher();
            for (Question qu : listeQuestions) {
                System.out.println(qu);
            }

            // 5. RECUPERER LE DERNIER ID DE QUESTION AJOUTEE
            int lastQuestionId = listeQuestions.get(listeQuestions.size() - 1).getId();

            // 6. MODIFICATION QUESTION
            Question questionModifiee = new Question(
                    lastQuestionId,
                    3,
                    "Java est-il 100% orienté objet ?",
                    15,
                    "[{\"texte\":\"Oui\",\"correct\":true},{\"texte\":\"Non\",\"correct\":false}]"
            );
            questionService.modifier(questionModifiee);

            // 7. AFFICHAGE QUESTIONS APRES MODIFICATION
            System.out.println("=== LISTE DES QUESTIONS APRES MODIFICATION ===");
            for (Question qu : questionService.afficher()) {
                System.out.println(qu);
            }

            // 8. SUPPRESSION QUESTION
            questionService.supprimer(lastQuestionId);

            // 9. AFFICHAGE QUESTIONS APRES SUPPRESSION
            System.out.println("=== LISTE DES QUESTIONS APRES SUPPRESSION ===");
            for (Question qu : questionService.afficher()) {
                System.out.println(qu);
            }

            // 1. AJOUT CERTIFICATION
            Certification certification = new Certification(
                    85.5f,
                    "Réussi",
                    "Or",
                    new Timestamp(System.currentTimeMillis()),
                    1, // user_id
                    3  // quiz_id
            );
            certificationService.ajouter(certification);

            // 2. AFFICHAGE APRES AJOUT
            System.out.println("=== LISTE DES CERTIFICATIONS APRES AJOUT ===");
            List<Certification> listeCertifications = certificationService.afficher();
            for (Certification c : listeCertifications) {
                System.out.println(c);
            }
              // 3. RECUPERER LE DERNIER ID AJOUTE
            int lastCertificationId = listeCertifications.get(listeCertifications.size() - 1).getId();

            // 4. MODIFICATION
            Certification certificationModifiee = new Certification(
                    lastCertificationId,
                    92.0f,
                    "Réussi",
                    "Platine",
                    new Timestamp(System.currentTimeMillis()),
                    1,
                    3
            );
            certificationService.modifier(certificationModifiee);

            // 5. AFFICHAGE APRES MODIFICATION
            System.out.println("=== LISTE DES CERTIFICATIONS APRES MODIFICATION ===");
            for (Certification c : certificationService.afficher()) {
                System.out.println(c);
            }

            // 6. SUPPRESSION
            certificationService.supprimer(lastCertificationId);

            // 7. AFFICHAGE APRES SUPPRESSION
            System.out.println("=== LISTE DES CERTIFICATIONS APRES SUPPRESSION ===");
            for (Certification c : certificationService.afficher()) {
                System.out.println(c);
            }


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

            // 1. AJOUT CERTIFICATION FINALE
            CertificationFinale certificationFinale = new CertificationFinale(
                    new Timestamp(System.currentTimeMillis()),
                    "Or",
                    1, // user_id existant
                    3, // quiz_id existant
                    1  // tentative_id existant dans certification
            );
            certificationFinaleService.ajouter(certificationFinale);

            // 2. AFFICHAGE APRES AJOUT
            System.out.println("=== LISTE DES CERTIFICATIONS FINALES APRES AJOUT ===");
            List<CertificationFinale> listeFinales = certificationFinaleService.afficher();
            for (CertificationFinale cf : listeFinales) {
                System.out.println(cf);
            }
            int lastFinaleId = listeFinales.get(listeFinales.size() - 1).getId();
            // 4. MODIFICATION
            CertificationFinale certificationFinaleModifiee = new CertificationFinale(
                    lastFinaleId,
                    new Timestamp(System.currentTimeMillis()),
                    "Platine",
                    1,
                    3,
                    1
            );
            certificationFinaleService.modifier(certificationFinaleModifiee);

            // 5. AFFICHAGE APRES MODIFICATION
            System.out.println("=== LISTE DES CERTIFICATIONS FINALES APRES MODIFICATION ===");
            for (CertificationFinale cf : certificationFinaleService.afficher()) {
                System.out.println(cf);
            }

            // 6. SUPPRESSION
            certificationFinaleService.supprimer(lastFinaleId);

            // 7. AFFICHAGE APRES SUPPRESSION
            System.out.println("=== LISTE DES CERTIFICATIONS FINALES APRES SUPPRESSION ===");
            for (CertificationFinale cf : certificationFinaleService.afficher()) {
                System.out.println(cf);
            }




        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }



/*
        try {
            chapServ.supprimer(4);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    */
    }
}