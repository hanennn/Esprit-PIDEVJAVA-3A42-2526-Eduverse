package models;

import java.sql.Timestamp;

public class AnalyseInterview {
    private int id;
    private int demandeId;
    private String transcription;
    private String scoresEmotions;
    private String featuresAudio;
    private String profilGlobal;
    private String recommandation;
    private Timestamp dateAnalyse;

    public AnalyseInterview() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDemandeId() { return demandeId; }
    public void setDemandeId(int demandeId) { this.demandeId = demandeId; }

    public String getTranscription() { return transcription; }
    public void setTranscription(String transcription) { this.transcription = transcription; }

    public String getScoresEmotions() { return scoresEmotions; }
    public void setScoresEmotions(String scoresEmotions) { this.scoresEmotions = scoresEmotions; }

    public String getFeaturesAudio() { return featuresAudio; }
    public void setFeaturesAudio(String featuresAudio) { this.featuresAudio = featuresAudio; }

    public String getProfilGlobal() { return profilGlobal; }
    public void setProfilGlobal(String profilGlobal) { this.profilGlobal = profilGlobal; }

    public String getRecommandation() { return recommandation; }
    public void setRecommandation(String recommandation) { this.recommandation = recommandation; }

    public Timestamp getDateAnalyse() { return dateAnalyse; }
    public void setDateAnalyse(Timestamp dateAnalyse) { this.dateAnalyse = dateAnalyse; }
}
