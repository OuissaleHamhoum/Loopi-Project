package edu.Loopi.interfaces;

import edu.Loopi.entities.Produit;
import java.util.List;

public interface IFavorisService {
    void ajouterFavoris(int idUser, int idProduit);
    void supprimerFavoris(int idUser, int idProduit);
    boolean estDansFavoris(int idUser, int idProduit);
    List<Produit> getFavorisByUser(int idUser);
    int countFavorisByProduit(int idProduit);
    void supprimerTousFavorisUser(int idUser);
}
