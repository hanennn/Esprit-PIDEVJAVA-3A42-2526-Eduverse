package org.example.entities;

public class cours {
    private int id;
    private String titre_cours;
    private String niv_cours;
    private String matiere_cours;
    private String description;
    private String langue_cours;
    private int createur_id; // ← ajouté

    public cours() {}

    public cours(String titre_cours, String description, String niv_cours,
                 String matiere_cours, String langue_cours) {
        this.titre_cours   = titre_cours;
        this.description   = description;
        this.niv_cours     = niv_cours;
        this.matiere_cours = matiere_cours;
        this.langue_cours  = langue_cours;
    }

    // Constructeur avec createur_id
    public cours(String titre_cours, String description, String niv_cours,
                 String matiere_cours, String langue_cours, int createur_id) {
        this.titre_cours   = titre_cours;
        this.description   = description;
        this.niv_cours     = niv_cours;
        this.matiere_cours = matiere_cours;
        this.langue_cours  = langue_cours;
        this.createur_id   = createur_id;
    }

    public int getId() { return id; }
    public void setId(int id) {
        this.id = id;
    }
    public void setCoursId(int id) { this.id = id; }

    public String getTitre_cours() { return titre_cours; }
    public void setTitre_cours(String titre_cours) { this.titre_cours = titre_cours; }

    public String getNiv_cours() { return niv_cours; }
    public void setNiv_cours(String niv_cours) { this.niv_cours = niv_cours; }

    public String getMatiere_cours() { return matiere_cours; }
    public void setMatiere_cours(String matiere_cours) { this.matiere_cours = matiere_cours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLangue_cours() { return langue_cours; }
    public void setLangue_cours(String langue_cours) { this.langue_cours = langue_cours; }

    public int getCreateur_id() { return createur_id; }         // ← ajouté
    public void setCreateur_id(int createur_id) {               // ← ajouté
        this.createur_id = createur_id;
    }


    @Override
    public String toString() {
        return "cours{" +
                "id=" + id +
                ", titre_cours='" + titre_cours + '\'' +
                ", niv_cours='" + niv_cours + '\'' +
                ", matiere_cours='" + matiere_cours + '\'' +
                ", description='" + description + '\'' +
                ", langue_cours='" + langue_cours + '\'' +
                ", createur_id=" + createur_id +            // ← ajouté
                '}';
    }
}