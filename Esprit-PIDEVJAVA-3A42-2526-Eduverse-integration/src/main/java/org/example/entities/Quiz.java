package org.example.entities;

public class Quiz {
    private int id;
    private String titre;
    private String typeQuiz;
    private int duree;
    private float scoreMinimum;
    private int coursAssocieId;

    public Quiz() {
    }

    public Quiz(String titre, String typeQuiz, int duree, float scoreMinimum, int coursAssocieId) {
        this.titre = titre;
        this.typeQuiz = typeQuiz;
        this.duree = duree;
        this.scoreMinimum = scoreMinimum;
        this.coursAssocieId = coursAssocieId;
    }

    public Quiz(int id, String titre, String typeQuiz, int duree, float scoreMinimum, int coursAssocieId) {
        this.id = id;
        this.titre = titre;
        this.typeQuiz = typeQuiz;
        this.duree = duree;
        this.scoreMinimum = scoreMinimum;
        this.coursAssocieId = coursAssocieId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getTypeQuiz() {
        return typeQuiz;
    }

    public void setTypeQuiz(String typeQuiz) {
        this.typeQuiz = typeQuiz;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public float getScoreMinimum() {
        return scoreMinimum;
    }

    public void setScoreMinimum(float scoreMinimum) {
        this.scoreMinimum = scoreMinimum;
    }

    public int getCoursAssocieId() {
        return coursAssocieId;
    }

    public void setCoursAssocieId(int coursAssocieId) {
        this.coursAssocieId = coursAssocieId;
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", typeQuiz='" + typeQuiz + '\'' +
                ", duree=" + duree +
                ", scoreMinimum=" + scoreMinimum +
                ", coursAssocieId=" + coursAssocieId +
                '}';
    }
}