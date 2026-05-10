package org.example.services;

import org.example.entities.Quiz;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QuizServiceTest {

    static QuizService service;
    static int idQuizTest;

    @BeforeAll
    static void setup() {
        service = new QuizService();
    }

    @Test
    @Order(1)
    void testAjouterQuiz() throws Exception {
        List<Quiz> avant = service.afficher();
        avant.stream()
                .filter(q -> q.getTitre().equals("QuizTestJUnit"))
                .forEach(q -> {
                    try { service.supprimer(q.getId()); }
                    catch (Exception ignored) {}
                });

        Quiz q = new Quiz("QuizTestJUnit", "Final", 30, 60, 1);
        service.ajouter(q);

        List<Quiz> quizzes = service.afficher();

        assertFalse(quizzes.isEmpty());
        assertTrue(
                quizzes.stream().anyMatch(quiz ->
                        quiz.getTitre().equals("QuizTestJUnit"))
        );

        Quiz quizAjoute = quizzes.stream()
                .filter(quiz -> quiz.getTitre().equals("QuizTestJUnit"))
                .reduce((first, second) -> second)
                .orElse(null);

        assertNotNull(quizAjoute);
        idQuizTest = quizAjoute.getId();
        System.out.println("Quiz ajouté avec ID : " + idQuizTest);
    }

    @Test
    @Order(2)
    void testDoublonQuiz() {

        Exception exception = assertThrows(Exception.class, () -> {
            Quiz doublon = new Quiz("QuizTestJUnit", "Final", 30, 60, 1);
            service.ajouter(doublon);
        });
        assertTrue(exception.getMessage().contains("existe déjà"));
        System.out.println("Doublon détecté : " + exception.getMessage());
    }

    @Test
    @Order(3)
    void testModifierQuiz() throws Exception {

        if (idQuizTest == 0) {
            List<Quiz> quizzes = service.afficher();
            Quiz found = quizzes.stream()
                    .filter(q -> q.getTitre().equals("QuizTestJUnit"))
                    .findFirst().orElse(null);
            if (found != null) idQuizTest = found.getId();
        }

        Quiz q = new Quiz();
        q.setId(idQuizTest);
        q.setTitre("QuizModifieJUnit");
        q.setTypeQuiz("Intermédiaire");
        q.setDuree(40);
        q.setScoreMinimum(70);
        q.setCoursAssocieId(1);

        service.modifier(q);

        List<Quiz> quizzes = service.afficher();
        boolean trouve = quizzes.stream()
                .anyMatch(quiz ->
                        quiz.getId() == idQuizTest &&
                                quiz.getTitre().equals("QuizModifieJUnit") &&
                                quiz.getTypeQuiz().equals("Intermédiaire") &&
                                quiz.getDuree() == 40 &&
                                quiz.getScoreMinimum() == 70
                );

        assertTrue(trouve);
        System.out.println("Quiz modifié avec succès");
    }

    @Test
    @Order(4)
    void testSupprimerQuiz() throws Exception {
        if (idQuizTest == 0) {
            List<Quiz> quizzes = service.afficher();
            Quiz found = quizzes.stream()
                    .filter(q -> q.getTitre().equals("QuizModifieJUnit"))
                    .findFirst().orElse(null);
            if (found != null) idQuizTest = found.getId();
        }

        service.supprimer(idQuizTest);

        List<Quiz> quizzes = service.afficher();
        boolean existe = quizzes.stream()
                .anyMatch(quiz -> quiz.getId() == idQuizTest);

        assertFalse(existe);
        System.out.println("Quiz supprimé avec succès");
    }
}