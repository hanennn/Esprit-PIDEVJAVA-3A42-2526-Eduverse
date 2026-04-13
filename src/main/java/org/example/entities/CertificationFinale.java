package org.example.entities;

import java.sql.Timestamp;

public class CertificationFinale {
    private int id;
    private Timestamp dateEmission;
    private String badge;
    private int userId;
    private int quizId;
    private int tentativeId;

    public CertificationFinale() {
    }

    public CertificationFinale(Timestamp dateEmission, String badge, int userId, int quizId, int tentativeId) {
        this.dateEmission = dateEmission;
        this.badge = badge;
        this.userId = userId;
        this.quizId = quizId;
        this.tentativeId = tentativeId;
    }

    public CertificationFinale(int id, Timestamp dateEmission, String badge, int userId, int quizId, int tentativeId) {
        this.id = id;
        this.dateEmission = dateEmission;
        this.badge = badge;
        this.userId = userId;
        this.quizId = quizId;
        this.tentativeId = tentativeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public Timestamp getDateEmission() {
        return dateEmission;
    }

    public void setDateEmission(Timestamp dateEmission) {
        this.dateEmission = dateEmission;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
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

    public int getTentativeId() {
        return tentativeId;
    }

    public void setTentativeId(int tentativeId) {
        this.tentativeId = tentativeId;
    }

    @Override
    public String toString() {
        return "CertificationFinale{" +
                "id=" + id +
                ", dateEmission=" + dateEmission +
                ", badge='" + badge + '\'' +
                ", userId=" + userId +
                ", quizId=" + quizId +
                ", tentativeId=" + tentativeId +
                '}';
    }
}