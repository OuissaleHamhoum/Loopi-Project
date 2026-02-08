package edu.Loopi.services;

import edu.Loopi.entities.Participation;
import edu.Loopi.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationService {

    public void addParticipation(Participation participation) {
        String query = "INSERT INTO participation (id_user, id_evenement, contact, age, statut) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = MyConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, participation.getIdUser());
            ps.setInt(2, participation.getIdEvenement());
            ps.setString(3, participation.getContact());
            ps.setObject(4, participation.getAge(), Types.INTEGER);
            ps.setString(5, participation.getStatut());

            ps.executeUpdate();
            System.out.println("✅ Participation enregistrée avec succès");

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de la participation: " + e.getMessage());
        }
    }

    public void updateParticipationStatut(int idParticipation, String statut) {
        String query = "UPDATE participation SET statut=? WHERE id=?";

        try (Connection conn = MyConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, statut);
            ps.setInt(2, idParticipation);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Statut de participation mis à jour");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du statut: " + e.getMessage());
        }
    }

    public void deleteParticipation(int idParticipation) {
        String query = "DELETE FROM participation WHERE id=?";

        try (Connection conn = MyConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idParticipation);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Participation supprimée avec succès");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de la participation: " + e.getMessage());
        }
    }

    public List<Participation> getParticipationsByEvenement(int idEvenement) {
        List<Participation> participations = new ArrayList<>();
        String query = "SELECT * FROM participation WHERE id_evenement=? ORDER BY date_inscription DESC";

        try (Connection conn = MyConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idEvenement);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    participations.add(mapResultSetToParticipation(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des participations: " + e.getMessage());
        }

        return participations;
    }

    public List<Participation> getParticipationsByUser(int idUser) {
        List<Participation> participations = new ArrayList<>();
        String query = "SELECT * FROM participation WHERE id_user=? ORDER BY date_inscription DESC";

        try (Connection conn = MyConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    participations.add(mapResultSetToParticipation(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des participations: " + e.getMessage());
        }

        return participations;
    }

    public boolean isUserAlreadyRegistered(int idUser, int idEvenement) {
        String query = "SELECT COUNT(*) FROM participation WHERE id_user=? AND id_evenement=?";

        try (Connection conn = MyConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idUser);
            ps.setInt(2, idEvenement);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de l'inscription: " + e.getMessage());
        }

        return false;
    }

    public int getNombreParticipants(int idEvenement) {
        String query = "SELECT COUNT(*) as nb_participants FROM participation " +
                "WHERE id_evenement=? AND statut != 'absent'";

        try (Connection conn = MyConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idEvenement);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("nb_participants");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des participants: " + e.getMessage());
        }

        return 0;
    }

    private Participation mapResultSetToParticipation(ResultSet rs) throws SQLException {
        Participation participation = new Participation();
        participation.setId(rs.getInt("id"));
        participation.setIdUser(rs.getInt("id_user"));
        participation.setIdEvenement(rs.getInt("id_evenement"));
        participation.setContact(rs.getString("contact"));
        participation.setAge(rs.getInt("age"));

        Timestamp dateInscriptionTimestamp = rs.getTimestamp("date_inscription");
        if (dateInscriptionTimestamp != null) {
            participation.setDateInscription(dateInscriptionTimestamp.toLocalDateTime());
        }

        participation.setStatut(rs.getString("statut"));
        return participation;
    }
}