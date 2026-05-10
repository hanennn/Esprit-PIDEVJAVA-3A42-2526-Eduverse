package org.example.entities;

public class Inscription {
    private int id;
    private int userId;
    private int coursId;
    private String dateInscription;

    public Inscription() {}

    public Inscription(int userId, int coursId) {
        this.userId = userId;
        this.coursId = coursId;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getCoursId() { return coursId; }
    public String getDateInscription() { return dateInscription; }

    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setCoursId(int coursId) { this.coursId = coursId; }
    public void setDateInscription(String dateInscription) { this.dateInscription = dateInscription; }
}