package org.example.services;

import org.example.entities.Quiz;
import org.example.entities.Session;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizService {

    private final Connection cnx;

    public QuizService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    // ─────────── VÉRIFIER DOUBLON ───────────
    public boolean existeDeja(String titre, int coursId) {
        try {
            String sql = "SELECT COUNT(*) FROM quiz WHERE titre = ? AND cours_associe_id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, titre.trim());
            ps.setInt(2, coursId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) {
            System.err.println("Erreur doublon quiz : " + e.getMessage());
        }
        return false;
    }

    public boolean existeDejaSaufId(String titre, int coursId, int id) {
        try {
            String sql = "SELECT COUNT(*) FROM quiz WHERE titre = ? AND cours_associe_id = ? AND id != ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, titre.trim());
            ps.setInt(2, coursId);
            ps.setInt(3, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─────────── AJOUTER ───────────
    public void ajouter(Quiz quiz) throws Exception {
        if (existeDeja(quiz.getTitre(), quiz.getCoursAssocieId())) {
            throw new Exception(
                    "Un quiz avec le titre \"" + quiz.getTitre() +
                            "\" existe déjà pour ce cours !");
        }
        String sql = "INSERT INTO quiz (titre, type_quiz, duree, score_minimum, cours_associe_id) VALUES (?,?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, quiz.getTitre());
        ps.setString(2, quiz.getTypeQuiz());
        ps.setInt(3, quiz.getDuree());
        ps.setFloat(4, quiz.getScoreMinimum());
        ps.setInt(5, quiz.getCoursAssocieId());
        ps.executeUpdate();
        System.out.println("Quiz ajouté avec succès");
    }

    // ─────────── MODIFIER ───────────
    public void modifier(Quiz quiz) throws Exception {
        if (existeDejaSaufId(quiz.getTitre(), quiz.getCoursAssocieId(), quiz.getId())) {
            throw new Exception(
                    "Un autre quiz avec le titre \"" + quiz.getTitre() +
                            "\" existe déjà pour ce cours !");
        }
        String sql = "UPDATE quiz SET titre=?, type_quiz=?, duree=?, score_minimum=?, cours_associe_id=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, quiz.getTitre());
        ps.setString(2, quiz.getTypeQuiz());
        ps.setInt(3, quiz.getDuree());
        ps.setFloat(4, quiz.getScoreMinimum());
        ps.setInt(5, quiz.getCoursAssocieId());
        ps.setInt(6, quiz.getId());
        ps.executeUpdate();
        System.out.println("Quiz modifié avec succès");
    }

    // ─────────── SUPPRIMER ───────────
    public void supprimer(int id) throws Exception {
        String sql = "DELETE FROM quiz WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Quiz supprimé avec succès");
    }

    // ─────────── AFFICHER (filtrés par formateur connecté) ───────────
    public List<Quiz> afficher() throws Exception {
        List<Quiz> list = new ArrayList<>();
        // ← JOIN avec cours pour filtrer par createur_id du formateur connecté
        String sql =
                "SELECT q.* FROM quiz q " +
                        "JOIN cours c ON q.cours_associe_id = c.id " +
                        "WHERE c.createur_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, Session.getCurrentUser().getId()); // ← seulement ses quiz
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Quiz(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("type_quiz"),
                    rs.getInt("duree"),
                    rs.getFloat("score_minimum"),
                    rs.getInt("cours_associe_id")
            ));
        }
        return list;
    }

    // ─────────── AFFICHER TOUS (côté étudiant) ───────────
    public List<Quiz> afficherTous() throws Exception {
        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(new Quiz(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("type_quiz"),
                    rs.getInt("duree"),
                    rs.getFloat("score_minimum"),
                    rs.getInt("cours_associe_id")
            ));
        }
        return list;
    }

    // ─────────── NOM COURS ───────────
    public String getNomCours(int coursId) {
        try {
            String sql = "SELECT titre_cours FROM cours WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, coursId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("titre_cours");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "—";
    }
    // ─────────── AFFICHER TOUS LES QUIZ (ADMIN) ───────────
    public List<Quiz> afficherTousAdmin() throws Exception {

        List<Quiz> list = new ArrayList<>();

        String sql = "SELECT * FROM quiz";

        Statement st = cnx.createStatement();

        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {

            list.add(new Quiz(

                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("type_quiz"),
                    rs.getInt("duree"),
                    rs.getFloat("score_minimum"),
                    rs.getInt("cours_associe_id")
            ));
        }

        return list;
    }
    // ─────────── SUPPRIMER QUIZ + QUESTIONS ───────────
    public void supprimerQuiz(int idQuiz) throws Exception {

        // =========================
        // supprimer questions
        // =========================

        String sqlQuestions =
                "DELETE FROM question WHERE quiz_id=?";

        PreparedStatement psQ =
                cnx.prepareStatement(sqlQuestions);

        psQ.setInt(1, idQuiz);

        psQ.executeUpdate();

        // =========================
        // supprimer quiz
        // =========================

        String sqlQuiz =
                "DELETE FROM quiz WHERE id=?";

        PreparedStatement psQuiz =
                cnx.prepareStatement(sqlQuiz);

        psQuiz.setInt(1, idQuiz);

        psQuiz.executeUpdate();
    }
}