package com.eduverse.forum.models;

import java.time.LocalDateTime;

public class Sujet {
    private int id;
    private String titre;
    private String contenu;
    private LocalDateTime dateCreation;
    private int auteurId;
    private User auteur;
    private int nbMessages;
    private int likesCount;
    private int dislikesCount;
    private int userRating;
    private String imageUrl;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public int getAuteurId() { return auteurId; }
    public void setAuteurId(int auteurId) { this.auteurId = auteurId; }
    public User getAuteur() { return auteur; }
    public void setAuteur(User auteur) { this.auteur = auteur; }
    public int getNbMessages() { return nbMessages; }
    public void setNbMessages(int nbMessages) { this.nbMessages = nbMessages; }
    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public int getDislikesCount() { return dislikesCount; }
    public void setDislikesCount(int dislikesCount) { this.dislikesCount = dislikesCount; }
    public int getUserRating() { return userRating; }
    public void setUserRating(int userRating) { this.userRating = userRating; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}