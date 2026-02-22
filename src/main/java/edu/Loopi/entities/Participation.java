package edu.Loopi.entities;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Participation {
    private int id;
    private int idUser;
    private int idEvenement;
    private String contact;
    private Integer age;
    private Timestamp dateInscription;
    private String statut; // inscrit, present, absent

    // Informations supplémentaires pour l'affichage
    private String eventTitre;
    private String eventLieu;
    private LocalDateTime eventDate;
    private String organisateurNom;

    // Informations utilisateur
    private String userNom;
    private String userPrenom;
    private String userEmail;

    public Participation() {}

    public Participation(int idUser, int idEvenement, String contact, Integer age) {
        this.idUser = idUser;
        this.idEvenement = idEvenement;
        this.contact = contact;
        this.age = age;
        this.statut = "inscrit";
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public int getIdEvenement() { return idEvenement; }
    public void setIdEvenement(int idEvenement) { this.idEvenement = idEvenement; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Timestamp getDateInscription() { return dateInscription; }
    public void setDateInscription(Timestamp dateInscription) { this.dateInscription = dateInscription; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getEventTitre() { return eventTitre; }
    public void setEventTitre(String eventTitre) { this.eventTitre = eventTitre; }

    public String getEventLieu() { return eventLieu; }
    public void setEventLieu(String eventLieu) { this.eventLieu = eventLieu; }

    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }

    public String getOrganisateurNom() { return organisateurNom; }
    public void setOrganisateurNom(String organisateurNom) { this.organisateurNom = organisateurNom; }

    public String getUserNom() { return userNom; }
    public void setUserNom(String userNom) { this.userNom = userNom; }

    public String getUserPrenom() { return userPrenom; }
    public void setUserPrenom(String userPrenom) { this.userPrenom = userPrenom; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserNomComplet() {
        return userPrenom + " " + userNom;
    }

    public String getFormattedDate() {
        if (dateInscription == null) return "Date inconnue";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateInscription.toLocalDateTime().format(formatter);
    }

    public String getStatutColor() {
        switch (statut.toLowerCase()) {
            case "inscrit": return "#3b82f6";
            case "present": return "#10b981";
            case "absent": return "#ef4444";
            default: return "#6c757d";
        }
    }

    public String getStatutFr() {
        switch (statut.toLowerCase()) {
            case "inscrit": return "Inscrit";
            case "present": return "Présent";
            case "absent": return "Absent";
            default: return statut;
        }
    }
}