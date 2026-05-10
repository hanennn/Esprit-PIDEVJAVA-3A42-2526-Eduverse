package org.example.entities;

public class Badword {
    public static final String ACTION_MASK = "MASK";
    public static final String ACTION_BLOCK = "BLOCK";
    public static final String ACTION_ALERT = "ALERT";

    private int id;
    private String word;
    private String action; // MASK, BLOCK, ALERT
    private boolean active;

    public Badword() {}

    public Badword(int id, String word, String action, boolean active) {
        this.id = id;
        this.word = word;
        this.action = action;
        this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return word + " (" + action + ")";
    }
}
