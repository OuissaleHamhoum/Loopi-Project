package edu.Loopi.entities;

public class Genre {
    private int id;
    private String sexe;

    public Genre() {}

    public Genre(int id, String sexe) {
        this.id = id;
        this.sexe = sexe;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    @Override
    public String toString() {
        return sexe;
    }
}