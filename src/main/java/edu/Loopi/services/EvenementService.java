package edu.Loopi.services;

import edu.Loopi.entities.Evenement;
import edu.Loopi.tools.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EvenementService {

    public void addEvenement(Evenement evenement) {
        String query = "INSERT INTO evenement (titre, description, date_evenement, lieu, id_organisateur, capacite_max, image_evenement) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = MyConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, evenement.getTitre());
            ps.setString(2, evenement.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(evenement.getDateEvenement()));
            ps.setString(4, evenement.getLieu());
            ps.setInt(5, evenement.getIdOrganisateur());
            ps.setObject(6, evenement.getCapaciteMax(), Types.INTEGER);
            ps.setString(7, evenement.getImageEvenement());

            ps.executeUpdate();
            System.out.println("✅ Événement ajouté avec succès");

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de l'événement: " + e.getMessage());
        }
    }

    public void updateEvenement(Evenement evenement) {
        String query = "UPDATE evenement SET titre=?, description=?, date_evenement=?, lieu=?, " +
                "capacite_max=?, image_evenement=? WHERE id_evenement=?";

        try (Connection conn = MyConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, evenement.getTitre());
            ps.setString(2, evenement.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(evenement.getDateEvenement()));
            ps.setString(4, evenement.getLieu());
            ps.setObject(5, evenement.getCapaciteMax(), Types.INTEGER);
            ps.setString(6, evenement.getImageEvenement());
            ps.setInt(7, evenement.getIdEvenement());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Événement mis à jour avec succès");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de l'événement: " + e.getMessage());
        }
    }

    public void deleteEvenement(int idEvenement) {
        String query = "DELETE FROM evenement WHERE id_evenement=?";

        try (Connection conn = MyConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idEvenement);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Événement supprimé avec succès");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de l'événement: " + e.getMessage());
        }
    }

    public List<Evenement> getAllEvenements() {
        List<Evenement> evenements = new ArrayList<>();
        String query = "SELECT * FROM evenement ORDER BY date_evenement DESC";

        try (Connection conn = MyConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                evenements.add(mapResultSetToEvenement(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des événements: " + e.getMessage());
        }

        return evenements;
    }

    public List<Evenement> getEvenementsByOrganisateur(int idOrganisateur) {
        List<Evenement> evenements = new ArrayList<>();
        String query = "SELECT * FROM evenement WHERE id_organisateur=? ORDER BY date_evenement DESC";

        try (Connection conn = MyConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idOrganisateur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    evenements.add(mapResultSetToEvenement(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des événements: " + e.getMessage());
        }

        return evenements;
    }

    public Evenement getEvenementById(int idEvenement) {
        String query = "SELECT * FROM evenement WHERE id_evenement=?";

        try (Connection conn = MyConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idEvenement);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEvenement(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'événement: " + e.getMessage());
        }

        return null;
    }

    public List<Evenement> searchEvenements(String keyword) {
        List<Evenement> evenements = new ArrayList<>();
        String query = "SELECT * FROM evenement WHERE titre LIKE ? OR description LIKE ? OR lieu LIKE ? ORDER BY date_evenement DESC";

        try (Connection conn = MyConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            String searchTerm = "%" + keyword + "%";
            ps.setString(1, searchTerm);
            ps.setString(2, searchTerm);
            ps.setString(3, searchTerm);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    evenements.add(mapResultSetToEvenement(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche des événements: " + e.getMessage());
        }

        return evenements;
    }

    private Evenement mapResultSetToEvenement(ResultSet rs) throws SQLException {
        Evenement evenement = new Evenement();
        evenement.setIdEvenement(rs.getInt("id_evenement"));
        evenement.setTitre(rs.getString("titre"));
        evenement.setDescription(rs.getString("description"));

        Timestamp dateTimestamp = rs.getTimestamp("date_evenement");
        if (dateTimestamp != null) {
            evenement.setDateEvenement(dateTimestamp.toLocalDateTime());
        }

        evenement.setLieu(rs.getString("lieu"));
        evenement.setIdOrganisateur(rs.getInt("id_organisateur"));
        evenement.setCapaciteMax(rs.getInt("capacite_max"));
        evenement.setImageEvenement(rs.getString("image_evenement"));

        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        if (createdAtTimestamp != null) {
            evenement.setCreatedAt(createdAtTimestamp.toLocalDateTime());
        }

        return evenement;
    }

    // Méthode pour vérifier si un événement est complet
    public boolean isEvenementComplet(int idEvenement) {
        String query = "SELECT COUNT(*) as nb_participants, e.capacite_max " +
                "FROM participation p " +
                "JOIN evenement e ON p.id_evenement = e.id_evenement " +
                "WHERE p.id_evenement = ? AND p.statut != 'absent' " +
                "GROUP BY e.capacite_max";

        try (Connection conn = MyConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idEvenement);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int nbParticipants = rs.getInt("nb_participants");
                    int capaciteMax = rs.getInt("capacite_max");
                    return nbParticipants >= capaciteMax;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de la capacité: " + e.getMessage());
        }

        return false;
    }
}