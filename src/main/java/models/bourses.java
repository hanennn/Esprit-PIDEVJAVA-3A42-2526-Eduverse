package models;

import java.sql.Timestamp;

public class bourses {
    private int id;
    private String titre;
    private String description;
    private String image;
    private Timestamp date_attribution;
    private Timestamp date_fin;
    private double montant;

    public bourses() {
    }

    public bourses(int id, String titre, String description, String image, Timestamp date_attribution, Timestamp date_fin, double montant) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.image = image;
        this.date_attribution = date_attribution;
        this.date_fin = date_fin;
        this.montant = montant;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Timestamp getDate_attribution() {
        return date_attribution;
    }

    public void setDate_attribution(Timestamp date_attribution) {
        this.date_attribution = date_attribution;
    }

    public Timestamp getDate_fin() {
        return date_fin;
    }

    public void setDate_fin(Timestamp date_fin) {
        this.date_fin = date_fin;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    @Override
    public String toString() {
        return "bourses{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", date_attribution=" + date_attribution +
                ", date_fin=" + date_fin +
                ", montant=" + montant +
                '}';
    }
}
