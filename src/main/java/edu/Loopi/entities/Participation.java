package edu.Loopi.entities;

import java.time.LocalDateTime;

public class Participation {
    private int id;
    private int idUser;
    private int idEvenement;
    private String contact;
    private Integer age;
    private LocalDateTime dateInscription;
    private String statut;

    // Constructeurs
    public Participation() {}

    public Participation(int idUser, int idEvenement, String contact, Integer age, String statut) {
        this.idUser = idUser;
        this.idEvenement = idEvenement;
        this.contact = contact;
        this.age = age;
        this.statut = statut;
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

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Participation{" +
                "idUser=" + idUser +
                ", idEvenement=" + idEvenement +
                ", statut='" + statut + '\'' +
                '}';
    }
}