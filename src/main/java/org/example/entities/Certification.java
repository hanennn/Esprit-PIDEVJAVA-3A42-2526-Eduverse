package org.example.entities;

import java.sql.Timestamp;

public class Certification {
    private int id;
    private float scoreObtenu;
    private String statut;
    private String badge;
    private Timestamp dateAttribution;
    private int userId;
    private int quizId;

    public Certification() {
    }

    public Certification(float scoreObtenu, String statut, String badge, Timestamp dateAttribution, int userId, int quizId) {
        this.scoreObtenu = scoreObtenu;
        this.statut = statut;
        this.badge = badge;
        this.dateAttribution = dateAttribution;
        this.userId = userId;
        this.quizId = quizId;
    }

    public Certification(int id, float scoreObtenu, String statut, String badge, Timestamp dateAttribution, int userId, int quizId) {
        this.id = id;
        this.scoreObtenu = scoreObtenu;
        this.statut = statut;
        this.badge = badge;
        this.dateAttribution = dateAttribution;
        this.userId = userId;
        this.quizId = quizId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public float getScoreObtenu() {
        return scoreObtenu;
    }

    public void setScoreObtenu(float scoreObtenu) {
        this.scoreObtenu = scoreObtenu;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public Timestamp getDateAttribution() {
        return dateAttribution;
    }

    public void setDateAttribution(Timestamp dateAttribution) {
        this.dateAttribution = dateAttribution;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    @Override
    public String toString() {
        return "Certification{" +
                "id=" + id +
                ", scoreObtenu=" + scoreObtenu +
                ", statut='" + statut + '\'' +
                ", badge='" + badge + '\'' +
                ", dateAttribution=" + dateAttribution +
                ", userId=" + userId +
                ", quizId=" + quizId +
                '}';
    }
}