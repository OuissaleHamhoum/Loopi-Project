package edu.Loopi.entities;

import java.time.LocalDateTime;

public class Favoris {
    private int idFavoris;
    private int idUser;
    private int idProduit;
    private LocalDateTime dateAjout;

    // Constructeurs
    public Favoris() {}

    public Favoris(int idUser, int idProduit) {
        this.idUser = idUser;
        this.idProduit = idProduit;
    }

    // Getters et Setters
    public int getIdFavoris() {
        return idFavoris;
    }

    public void setIdFavoris(int idFavoris) {
        this.idFavoris = idFavoris;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public LocalDateTime getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDateTime dateAjout) {
        this.dateAjout = dateAjout;
    }

    @Override
    public String toString() {
        return "Favoris{" +
                "idFavoris=" + idFavoris +
                ", idUser=" + idUser +
                ", idProduit=" + idProduit +
                ", dateAjout=" + dateAjout +
                '}';
    }
}