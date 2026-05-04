package models;

import java.sql.Timestamp;

public class EventInscription {
    private int id;
    private Timestamp dateInscription;
    private String statut;
    private int participantId;
    private int eventId;

    public EventInscription() {
    }

    public EventInscription(int id, Timestamp dateInscription, String statut, int participantId, int eventId) {
        this.id = id;
        this.dateInscription = dateInscription;
        this.statut = statut;
        this.participantId = participantId;
        this.eventId = eventId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(Timestamp dateInscription) {
        this.dateInscription = dateInscription;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return "EventInscription{" +
                "id=" + id +
                ", dateInscription=" + dateInscription +
                ", statut='" + statut + '\'' +
                ", participantId=" + participantId +
                ", eventId=" + eventId +
                '}';
    }
}
