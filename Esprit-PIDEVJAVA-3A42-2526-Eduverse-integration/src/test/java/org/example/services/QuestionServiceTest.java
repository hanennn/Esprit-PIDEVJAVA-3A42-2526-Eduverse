package org.example.services;

import org.example.entities.Question;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QuestionServiceTest {

    static QuestionService service;
    static int idQuestionTest;
    static final int QUIZ_ID = 11;

    @BeforeAll
    static void setup() {
        service = new QuestionService();
    }

    @Test
    @Order(1)
    void testAjouterQuestion() throws Exception {
        List<Question> avant = service.afficher();
        avant.stream()
                .filter(q -> q.getQuestion().equals("QuestionTestJUnit")
                        && q.getQuizId() == QUIZ_ID)
                .forEach(q -> {
                    try { service.supprimer(q.getId()); }
                    catch (Exception ignored) {}
                });

        Question q = new Question(
                QUIZ_ID,
                "QuestionTestJUnit",
                10,
                "[{\"texte\":\"Oui\",\"correct\":true}," +
                        "{\"texte\":\"Non\",\"correct\":false}]"
        );

        service.ajouter(q);

        List<Question> questions = service.afficher();
        assertFalse(questions.isEmpty());
        assertTrue(
                questions.stream().anyMatch(quest ->
                        quest.getQuestion().equals("QuestionTestJUnit"))
        );

        Question questionAjoute = questions.stream()
                .filter(quest -> quest.getQuestion().equals("QuestionTestJUnit"))
                .reduce((first, second) -> second)
                .orElse(null);

        assertNotNull(questionAjoute);
        idQuestionTest = questionAjoute.getId();
        System.out.println("Question ajoutée avec ID : " + idQuestionTest);
    }

    @Test
    @Order(2)
    void testDoublonQuestion() {
        // Tenter d'ajouter un doublon → doit lever Exception
        Exception exception = assertThrows(Exception.class, () -> {
            Question doublon = new Question(
                    QUIZ_ID,
                    "QuestionTestJUnit",
                    10,
                    "[{\"texte\":\"Oui\",\"correct\":true}]"
            );
            service.ajouter(doublon);
        });
        assertTrue(exception.getMessage().contains("existe déjà"));
        System.out.println("Doublon détecté : " + exception.getMessage());
    }

    @Test
    @Order(3)
    void testModifierQuestion() throws Exception {
        if (idQuestionTest == 0) {
            List<Question> questions = service.afficher();
            Question found = questions.stream()
                    .filter(q -> q.getQuestion().equals("QuestionTestJUnit"))
                    .findFirst().orElse(null);
            if (found != null) idQuestionTest = found.getId();
        }

        Question q = new Question();
        q.setId(idQuestionTest);
        q.setQuestion("QuestionModifieJUnit");
        q.setPoints(20);
        q.setQuizId(QUIZ_ID);
        q.setReponses(
                "[{\"texte\":\"Non\",\"correct\":false}," +
                        "{\"texte\":\"Oui\",\"correct\":true}]");

        service.modifier(q);

        List<Question> questions = service.afficher();
        boolean trouve = questions.stream()
                .anyMatch(quest ->
                        quest.getId() == idQuestionTest &&
                                quest.getQuestion().equals("QuestionModifieJUnit") &&
                                quest.getPoints() == 20
                );

        assertTrue(trouve);
        System.out.println("Question modifiée avec succès");
    }

    @Test
    @Order(4)
    void testSupprimerQuestion() throws Exception {
        if (idQuestionTest == 0) {
            List<Question> questions = service.afficher();
            Question found = questions.stream()
                    .filter(q -> q.getQuestion().equals("QuestionModifieJUnit"))
                    .findFirst().orElse(null);
            if (found != null) idQuestionTest = found.getId();
        }

        service.supprimer(idQuestionTest);

        List<Question> questions = service.afficher();
        boolean existe = questions.stream()
                .anyMatch(quest -> quest.getId() == idQuestionTest);

        assertFalse(existe);
        System.out.println("Question supprimée avec succès");
    }
}