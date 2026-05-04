package org.example.entities;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Event {
    private int id;
    private String titre;
    private String description;
    private String type;
    private String lienWebinaire;
    private String niveau;
    private Date date;
    private Time heureDeb;
    private Time heureFin;
    private Timestamp dateCreation;
    private String image;
    private String lieu;

    public Event() {
    }

    public Event(int id, String titre, String description, String type, String lienWebinaire, String niveau, Date date, Time heureDeb, Time heureFin, Timestamp dateCreation, String image, String lieu) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.lienWebinaire = lienWebinaire;
        this.niveau = niveau;
        this.date = date;
        this.heureDeb = heureDeb;
        this.heureFin = heureFin;
        this.dateCreation = dateCreation;
        this.image = image;
        this.lieu = lieu;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLienWebinaire() { return lienWebinaire; }
    public void setLienWebinaire(String lienWebinaire) { this.lienWebinaire = lienWebinaire; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Time getHeureDeb() { return heureDeb; }
    public void setHeureDeb(Time heureDeb) { this.heureDeb = heureDeb; }

    public Time getHeureFin() { return heureFin; }
    public void setHeureFin(Time heureFin) { this.heureFin = heureFin; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", lienWebinaire='" + lienWebinaire + '\'' +
                ", niveau='" + niveau + '\'' +
                ", date=" + date +
                ", heureDeb=" + heureDeb +
                ", heureFin=" + heureFin +
                ", dateCreation=" + dateCreation +
                ", image='" + image + '\'' +
                ", lieu='" + lieu + '\'' +
                '}';
    }
}
