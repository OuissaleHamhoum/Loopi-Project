package edu.Loopi.services;

import edu.Loopi.entities.User;
import edu.Loopi.tools.MyConnection;
import edu.Loopi.tools.PasswordUtil;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AuthService {
    private Connection connection;
    private Map<String, String> resetTokens = new HashMap<>();
    private Random random = new Random();

    public AuthService() {
        this.connection = MyConnection.getInstance().getConnection();
    }

    public User login(String email, String password) {
        String query = "SELECT u.*, g.sexe FROM users u " +
                "LEFT JOIN genre g ON u.id_genre = g.id_genre " +
                "WHERE u.email = ? AND u.password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password); // À crypter en production

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la connexion: " + e.getMessage());
        }

        return null;
    }

    public boolean register(User user) {
        // Vérifier si l'email existe déjà
        if (emailExists(user.getEmail())) {
            return false;
        }

        String query = "INSERT INTO users (nom, prenom, email, password, photo, role, id_genre) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword()); // À crypter en production
            stmt.setString(5, user.getPhoto());
            stmt.setString(6, user.getRole());
            stmt.setInt(7, user.getIdGenre());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'inscription: " + e.getMessage());
            return false;
        }
    }

    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification email: " + e.getMessage());
        }

        return false;
    }

    public String generateResetCode(String email) {
        // Générer un code à 6 chiffres
        String code = String.format("%06d", random.nextInt(1000000));

        // Stocker le code avec une expiration (simulé)
        resetTokens.put(email, code);

        // Pour la démo, afficher le code dans la console
        System.out.println("🔑 Code de réinitialisation pour " + email + ": " + code);

        return code;
    }

    public boolean verifyResetCode(String email, String code) {
        String storedCode = resetTokens.get(email);
        return storedCode != null && storedCode.equals(code);
    }

    public boolean resetPassword(String email, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            // En production, crypter le mot de passe
            stmt.setString(1, newPassword);
            stmt.setString(2, email);

            int rowsAffected = stmt.executeUpdate();

            // Supprimer le token après utilisation
            if (rowsAffected > 0) {
                resetTokens.remove(email);
            }

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la réinitialisation: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUserPassword(int userId, String currentPassword, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE id = ? AND password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);
            stmt.setString(3, currentPassword);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du changement de mot de passe: " + e.getMessage());
            return false;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhoto(rs.getString("photo"));
        user.setRole(rs.getString("role"));
        user.setIdGenre(rs.getInt("id_genre"));

        // Set the sexe from the joined table
        user.setSexe(rs.getString("sexe"));

        // Set timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return user;
    }
}