package org.example.social;

import java.util.*;

/**
 * Tout en mémoire — 0 table, 0 attribut ajouté
 * Les données durent le temps de la session
 */
public class QuizSocialManager {

    // Map<quizId, List<commentaire>>
    private static final Map<Integer, List<Commentaire>> commentaires = new HashMap<>();
    // Map<quizId, Set<userId qui ont liké>>
    private static final Map<Integer, Set<Integer>> likes    = new HashMap<>();
    private static final Map<Integer, Set<Integer>> dislikes = new HashMap<>();

    // ─── COMMENTAIRES ───
    public static void ajouterCommentaire(int quizId, int userId, String nomUser, String texte) {
        commentaires.computeIfAbsent(quizId, k -> new ArrayList<>())
                .add(new Commentaire(userId, nomUser, texte,
                        java.time.LocalDateTime.now().toString().substring(0, 16)));
    }

    public static List<Commentaire> getCommentaires(int quizId) {
        return commentaires.getOrDefault(quizId, new ArrayList<>());
    }

    // ─── LIKES ───
    public static void toggleLike(int quizId, int userId) {
        Set<Integer> l = likes.computeIfAbsent(quizId, k -> new HashSet<>());
        Set<Integer> d = dislikes.computeIfAbsent(quizId, k -> new HashSet<>());
        if (l.contains(userId)) l.remove(userId);
        else { l.add(userId); d.remove(userId); } // like enlève dislike
    }

    public static void toggleDislike(int quizId, int userId) {
        Set<Integer> d = dislikes.computeIfAbsent(quizId, k -> new HashSet<>());
        Set<Integer> l = likes.computeIfAbsent(quizId, k -> new HashSet<>());
        if (d.contains(userId)) d.remove(userId);
        else { d.add(userId); l.remove(userId); }
    }

    public static int getLikes(int quizId) {
        return likes.getOrDefault(quizId, new HashSet<>()).size();
    }

    public static int getDislikes(int quizId) {
        return dislikes.getOrDefault(quizId, new HashSet<>()).size();
    }

    public static boolean aLiké(int quizId, int userId) {
        return likes.getOrDefault(quizId, new HashSet<>()).contains(userId);
    }

    public static boolean aDisliké(int quizId, int userId) {
        return dislikes.getOrDefault(quizId, new HashSet<>()).contains(userId);
    }

    // ─── CLASSE INTERNE ───
    public static class Commentaire {
        public int    userId;
        public String nomUser, texte, date;
        public Commentaire(int u, String n, String t, String d) {
            userId = u; nomUser = n; texte = t; date = d;
        }
    }
}