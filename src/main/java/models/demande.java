package models;

import java.sql.Timestamp;

public class demande {
    private int id;
    private Timestamp date_demande;
    private String niveau_etudes;
    private String statut;
    private String lettre_motivation;
    private String note;
    private int etudiant_id;
    private int bourse_id;

    public demande() {
    }

    public demande(int id, Timestamp date_demande, String niveau_etudes, String statut, String lettre_motivation, String note, int etudiant_id, int bourse_id) {
        this.id = id;
        this.date_demande = date_demande;
        this.niveau_etudes = niveau_etudes;
        this.statut = statut;
        this.lettre_motivation = lettre_motivation;
        this.note = note;
        this.etudiant_id = etudiant_id;
        this.bourse_id = bourse_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getDate_demande() {
        return date_demande;
    }

    public void setDate_demande(Timestamp date_demande) {
        this.date_demande = date_demande;
    }

    public String getNiveau_etudes() {
        return niveau_etudes;
    }

    public void setNiveau_etudes(String niveau_etudes) {
        this.niveau_etudes = niveau_etudes;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getLettre_motivation() {
        return lettre_motivation;
    }

    public void setLettre_motivation(String lettre_motivation) {
        this.lettre_motivation = lettre_motivation;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getEtudiant_id() {
        return etudiant_id;
    }

    public void setEtudiant_id(int etudiant_id) {
        this.etudiant_id = etudiant_id;
    }

    public int getBourse_id() {
        return bourse_id;
    }

    public void setBourse_id(int bourse_id) {
        this.bourse_id = bourse_id;
    }

    @Override
    public String toString() {
        return "demande{" +
                "id=" + id +
                ", date_demande=" + date_demande +
                ", niveau_etudes='" + niveau_etudes + '\'' +
                ", statut='" + statut + '\'' +
                ", lettre_motivation='" + lettre_motivation + '\'' +
                ", note='" + note + '\'' +
                ", etudiant_id=" + etudiant_id +
                ", bourse_id=" + bourse_id +
                '}';
    }
}
