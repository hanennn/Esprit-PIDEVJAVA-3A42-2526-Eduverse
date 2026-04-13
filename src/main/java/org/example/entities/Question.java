package org.example.entities;

public class Question {
    private int id;
    private int quizId;
    private String question;
    private int points;
    private String reponses;

    public Question() {
    }

    public Question(int quizId, String question, int points, String reponses) {
        this.quizId = quizId;
        this.question = question;
        this.points = points;
        this.reponses = reponses;
    }

    public Question(int id, int quizId, String question, int points, String reponses) {
        this.id = id;
        this.quizId = quizId;
        this.question = question;
        this.points = points;
        this.reponses = reponses;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getReponses() {
        return reponses;
    }

    public void setReponses(String reponses) {
        this.reponses = reponses;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", quizId=" + quizId +
                ", question='" + question + '\'' +
                ", points=" + points +
                ", reponses='" + reponses + '\'' +
                '}';
    }
}