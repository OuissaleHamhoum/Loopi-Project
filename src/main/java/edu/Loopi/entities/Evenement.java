package edu.Loopi.entities;

import java.time.LocalDateTime;

public class Evenement {
    private int idEvenement;
    private String titre;
    private String description;
    private LocalDateTime dateEvenement;
    private String lieu;
    private int idOrganisateur;
    private Integer capaciteMax;
    private String imageEvenement;
    private LocalDateTime createdAt;

    // Constructeurs
    public Evenement() {}

    public Evenement(String titre, String description, LocalDateTime dateEvenement,
                     String lieu, int idOrganisateur, Integer capaciteMax, String imageEvenement) {
        this.titre = titre;
        this.description = description;
        this.dateEvenement = dateEvenement;
        this.lieu = lieu;
        this.idOrganisateur = idOrganisateur;
        this.capaciteMax = capaciteMax;
        this.imageEvenement = imageEvenement;
    }

    // Getters et Setters
    public int getIdEvenement() { return idEvenement; }
    public void setIdEvenement(int idEvenement) { this.idEvenement = idEvenement; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(LocalDateTime dateEvenement) { this.dateEvenement = dateEvenement; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public int getIdOrganisateur() { return idOrganisateur; }
    public void setIdOrganisateur(int idOrganisateur) { this.idOrganisateur = idOrganisateur; }

    public Integer getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(Integer capaciteMax) { this.capaciteMax = capaciteMax; }

    public String getImageEvenement() { return imageEvenement; }
    public void setImageEvenement(String imageEvenement) { this.imageEvenement = imageEvenement; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Evenement{" +
                "titre='" + titre + '\'' +
                ", date=" + dateEvenement +
                ", lieu='" + lieu + '\'' +
                '}';
    }
}