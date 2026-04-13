package org.example.entities;

public class chapitres {
    private int id;
    private String titre_chap;
    private String desc_chap;
    private int ordre_chap;
    private String duree_chap;
    private String statut_chap;
    private  String contenu_chap;
    private String type_contenu;
    private String resume_chap;
    private int cours_id;

    public chapitres() {}
    public chapitres(String titre_chap, String desc_chap, int ordre_chap, String duree_chap, String statut_chap, String contenu_chap, String type_contenu, String resume_chap) {
        this.titre_chap = titre_chap;
        this.desc_chap = desc_chap;
        this.ordre_chap = ordre_chap;
        this.duree_chap = duree_chap;
        this.statut_chap = statut_chap;
        this.contenu_chap = contenu_chap;
        this.type_contenu = type_contenu;
        this.resume_chap = resume_chap;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre_chap() {
        return titre_chap;
    }

    public void setTitre_chap(String titre_chap) {
        this.titre_chap = titre_chap;
    }

    public String getDesc_chap() {
        return desc_chap;
    }

    public void setDesc_chap(String desc_chap) {
        this.desc_chap = desc_chap;
    }

    public int getOrdre_chap() {
        return ordre_chap;
    }

    public void setOrdre_chap(int ordre_chap) {
        this.ordre_chap = ordre_chap;
    }

    public String getDuree_chap() {
        return duree_chap;
    }

    public void setDuree_chap(String duree_chap) {
        this.duree_chap = duree_chap;
    }

    public String getStatut_chap() {
        return statut_chap;
    }

    public void setStatut_chap(String statut_chap) {
        this.statut_chap = statut_chap;
    }

    public String getContenu_chap() {
        return contenu_chap;
    }

    public void setContenu_chap(String contenu_chap) {
        this.contenu_chap = contenu_chap;
    }

    public String getType_contenu() {
        return type_contenu;
    }

    public void setType_contenu(String type_contenu) {
        this.type_contenu = type_contenu;
    }

    public String getResume_chap() {
        return resume_chap;
    }

    public void setResume_chap(String resume_chap) {
        this.resume_chap = resume_chap;
    }


    @Override
    public String toString() {
        return "chapitres{" +
                "id=" + id +
                ", titre_chap='" + titre_chap + '\'' +
                ", desc_chap='" + desc_chap + '\'' +
                ", ordre_chap=" + ordre_chap +
                ", duree_chap='" + duree_chap + '\'' +
                ", statut_chap='" + statut_chap + '\'' +
                ", contenu_chap='" + contenu_chap + '\'' +
                ", type_contenu='" + type_contenu + '\'' +
                ", resume_chap='" + resume_chap + '\'' +
                '}';
    }


    public int getCours_id() { return cours_id; }
    public void setCours_id(int cours_id) { this.cours_id = cours_id; }
}

