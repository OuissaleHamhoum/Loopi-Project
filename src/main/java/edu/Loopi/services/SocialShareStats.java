package edu.Loopi.services;

import edu.Loopi.tools.MyConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SocialShareStats {
    private Connection cnx = MyConnection.getInstance().getConnection();

    /**
     * Enregistre un partage sur les réseaux sociaux
     */
    public void logShare(int idProduit, int idUser, String network) {
        String query = "INSERT INTO social_shares (id_produit, id_user, network, share_date) VALUES (?, ?, ?, NOW())";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, idProduit);
            pst.setInt(2, idUser);
            pst.setString(3, network);
            pst.executeUpdate();
            System.out.println("✅ Partage enregistré sur " + network);
        } catch (SQLException e) {
            System.err.println("❌ Erreur enregistrement partage: " + e.getMessage());
        }
    }

    /**
     * Récupère les statistiques de partage pour un produit
     */
    public Map<String, Integer> getShareStatsByProduct(int idProduit) {
        Map<String, Integer> stats = new HashMap<>();
        String query = "SELECT network, COUNT(*) as count FROM social_shares WHERE id_produit = ? GROUP BY network";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, idProduit);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                stats.put(rs.getString("network"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération stats partage: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Récupère le nombre total de partages pour tous les produits
     */
    public int getTotalShares() {
        String query = "SELECT COUNT(*) as total FROM social_shares";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage partages: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Récupère les produits les plus partagés
     */
    public Map<String, Integer> getMostSharedProducts(int limit) {
        Map<String, Integer> products = new HashMap<>();
        String query = """
            SELECT p.nom_produit, COUNT(ss.id_share) as share_count
            FROM produit p
            LEFT JOIN social_shares ss ON p.id_produit = ss.id_produit
            GROUP BY p.id_produit
            ORDER BY share_count DESC
            LIMIT ?
        """;

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, limit);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                products.put(rs.getString("nom_produit"), rs.getInt("share_count"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération produits populaires: " + e.getMessage());
        }

        return products;
    }
}