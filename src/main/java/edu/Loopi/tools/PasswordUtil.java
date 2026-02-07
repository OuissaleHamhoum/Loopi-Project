package edu.Loopi.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import java.util.regex.Pattern;

public class PasswordUtil {

    // Générer un sel sécurisé
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // Hasher un mot de passe avec SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("❌ Algorithme de hachage non disponible");
            return password; // Fallback (à ne pas faire en production)
        }
    }

    // Valider la force d'un mot de passe
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        // Vérifier la présence d'au moins une majuscule, une minuscule, un chiffre et un caractère spécial
        boolean hasUpper = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasLower = Pattern.compile("[a-z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]").matcher(password).find();

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    // Évaluer la force d'un mot de passe (0-100)
    public static int evaluatePasswordStrength(String password) {
        if (password == null) return 0;

        int score = 0;

        // Longueur
        if (password.length() >= 8) score += 20;
        if (password.length() >= 12) score += 10;
        if (password.length() >= 16) score += 10;

        // Diversité des caractères
        if (Pattern.compile("[A-Z]").matcher(password).find()) score += 15;
        if (Pattern.compile("[a-z]").matcher(password).find()) score += 15;
        if (Pattern.compile("[0-9]").matcher(password).find()) score += 15;
        if (Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]").matcher(password).find()) score += 15;

        // Vérifier les séquences simples
        if (password.matches(".*(123|abc|password|admin|qwerty).*")) {
            score -= 20;
        }

        return Math.min(Math.max(score, 0), 100);
    }

    // Obtenir un texte descriptif de la force
    public static String getStrengthText(String password) {
        int strength = evaluatePasswordStrength(password);

        if (strength < 30) {
            return "Très faible";
        } else if (strength < 50) {
            return "Faible";
        } else if (strength < 70) {
            return "Moyenne";
        } else if (strength < 90) {
            return "Forte";
        } else {
            return "Très forte";
        }
    }

    // Générer un mot de passe aléatoire
    public static String generateRandomPassword(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{};':\"|,.<>?";
        String all = upper + lower + digits + special;

        Random random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Assurer au moins un caractère de chaque type
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // Remplir le reste
        for (int i = 4; i < length; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }

        // Mélanger le mot de passe
        return shuffleString(password.toString());
    }

    // Mélanger une chaîne de caractères
    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        Random random = new SecureRandom();

        for (int i = characters.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = characters[index];
            characters[index] = characters[i];
            characters[i] = temp;
        }

        return new String(characters);
    }

    // Vérifier si un mot de passe correspond à des critères de base
    public static boolean validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        // Longueur minimale
        if (password.length() < 6) {
            return false;
        }

        // Vérifier les espaces
        if (password.contains(" ")) {
            return false;
        }

        return true;
    }

    // Obtenir une couleur pour la force du mot de passe
    public static String getStrengthColor(String password) {
        int strength = evaluatePasswordStrength(password);

        if (strength < 30) {
            return "#ff4444"; // Rouge
        } else if (strength < 50) {
            return "#ff8800"; // Orange
        } else if (strength < 70) {
            return "#ffbb33"; // Jaune-orange
        } else if (strength < 90) {
            return "#00C851"; // Vert clair
        } else {
            return "#007E33"; // Vert foncé
        }
    }
}