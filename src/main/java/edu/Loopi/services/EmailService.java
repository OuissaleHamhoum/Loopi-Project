package edu.Loopi.services;

import edu.Loopi.entities.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EmailService {
    private Map<String, String> verificationCodes = new HashMap<>();
    private Random random = new Random();

    public boolean sendResetCode(String toEmail, String code) {
        try {
            // Simulation d'envoi d'email
            System.out.println("======================================");
            System.out.println("📧 SIMULATION D'ENVOI D'EMAIL");
            System.out.println("======================================");
            System.out.println("Destinataire: " + toEmail);
            System.out.println("Sujet: Réinitialisation de mot de passe - LOOPI");
            System.out.println("Message:");
            System.out.println("Bonjour,");
            System.out.println("Voici votre code de réinitialisation : " + code);
            System.out.println("Ce code est valable pendant 15 minutes.");
            System.out.println("Si vous n'avez pas demandé cette réinitialisation,");
            System.out.println("vous pouvez ignorer cet email.");
            System.out.println("======================================");

            // Stocker le code pour vérification
            verificationCodes.put(toEmail, code);

            // Simuler un délai d'envoi
            Thread.sleep(1000);

            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public boolean sendWelcomeEmail(User user) {
        System.out.println("======================================");
        System.out.println("📧 EMAIL DE BIENVENUE");
        System.out.println("======================================");
        System.out.println("Destinataire: " + user.getEmail());
        System.out.println("Sujet: Bienvenue sur LOOPI !");
        System.out.println("Message:");
        System.out.println("Bonjour " + user.getPrenom() + " " + user.getNom() + ",");
        System.out.println("Merci de vous être inscrit sur LOOPI.");
        System.out.println("Votre compte a été créé avec succès.");
        System.out.println("Rôle: " + user.getRole());
        System.out.println("======================================");
        return true;
    }

    public boolean verifyCode(String email, String code) {
        String storedCode = verificationCodes.get(email);
        return storedCode != null && storedCode.equals(code);
    }

    public void removeCode(String email) {
        verificationCodes.remove(email);
    }
}