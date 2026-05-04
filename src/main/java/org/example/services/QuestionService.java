package org.example.services;

import org.example.entities.Question;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {

    private final Connection cnx;

    public QuestionService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    // ─────────── VÉRIFIER DOUBLON ───────────
    public boolean existeDeja(String questionTexte, int quizId) {
        try {
            String sql =
                    "SELECT COUNT(*) FROM question " +
                            "WHERE question = ? AND quiz_id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, questionTexte.trim());
            ps.setInt(2, quizId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) {
            System.err.println("Erreur doublon question : "
                    + e.getMessage());
        }
        return false;
    }

    public boolean existeDejaSaufId(String questionTexte,
                                    int quizId,
                                    int id) {
        try {
            String sql =
                    "SELECT COUNT(*) FROM question " +
                            "WHERE question = ? AND quiz_id = ? AND id != ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, questionTexte.trim());
            ps.setInt(2, quizId);
            ps.setInt(3, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─────────── AJOUTER ───────────
    public void ajouter(Question question) throws Exception {
        if (existeDeja(question.getQuestion(), question.getQuizId())) {
            throw new Exception(
                    "Cette question existe déjà dans ce quiz !");
        }
        String sql =
                "INSERT INTO question " +
                        "(quiz_id, question, points, reponses) VALUES (?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, question.getQuizId());
        ps.setString(2, question.getQuestion());
        ps.setInt(3, question.getPoints());
        ps.setString(4, question.getReponses());
        ps.executeUpdate();
        System.out.println("Question ajoutée avec succès");
    }

    // ─────────── MODIFIER ───────────
    public void modifier(Question question) throws Exception {
        if (existeDejaSaufId(
                question.getQuestion(),
                question.getQuizId(),
                question.getId())) {
            throw new Exception(
                    "Une autre question identique existe déjà dans ce quiz !");
        }
        String sql =
                "UPDATE question SET quiz_id=?, question=?, " +
                        "points=?, reponses=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, question.getQuizId());
        ps.setString(2, question.getQuestion());
        ps.setInt(3, question.getPoints());
        ps.setString(4, question.getReponses());
        ps.setInt(5, question.getId());
        ps.executeUpdate();
        System.out.println("Question modifiée avec succès");
    }

    // ─────────── SUPPRIMER ───────────
    public void supprimer(int id) throws Exception {
        String sql = "DELETE FROM question WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Question supprimée avec succès");
    }

    // ─────────── AFFICHER ───────────
    public List<Question> afficher() throws Exception {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM question";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(new Question(
                    rs.getInt("id"),
                    rs.getInt("quiz_id"),
                    rs.getString("question"),
                    rs.getInt("points"),
                    rs.getString("reponses")
            ));
        }
        return list;
    }
}