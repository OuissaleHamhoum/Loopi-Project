package edu.Loopi.tools;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class QRCodeGenerator {

    // Générer un QR code simple (simulation)
    public static ImageView generateQRCode(String data, int size) {
        WritableImage image = new WritableImage(size, size);
        PixelWriter pixelWriter = image.getPixelWriter();

        Random random = new Random(data.hashCode());

        // Créer un fond blanc
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                pixelWriter.setColor(x, y, Color.WHITE);
            }
        }

        // Dessiner les motifs de position (comme les vrais QR codes)
        drawPositionMarker(pixelWriter, 10, 10, 7, size);
        drawPositionMarker(pixelWriter, size - 17, 10, 7, size);
        drawPositionMarker(pixelWriter, 10, size - 17, 7, size);

        // Générer un pattern basé sur les données
        drawDataPattern(pixelWriter, data, size);

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);

        return imageView;
    }

    // Dessiner un marqueur de position
    private static void drawPositionMarker(PixelWriter pw, int startX, int startY, int moduleSize, int totalSize) {
        // Cadre noir extérieur
        for (int y = startY; y < startY + moduleSize; y++) {
            for (int x = startX; x < startX + moduleSize; x++) {
                if (y >= 0 && y < totalSize && x >= 0 && x < totalSize) {
                    pw.setColor(x, y, Color.BLACK);
                }
            }
        }

        // Cadre blanc intérieur
        for (int y = startY + 1; y < startY + moduleSize - 1; y++) {
            for (int x = startX + 1; x < startX + moduleSize - 1; x++) {
                if (y >= 0 && y < totalSize && x >= 0 && x < totalSize) {
                    pw.setColor(x, y, Color.WHITE);
                }
            }
        }

        // Point noir central
        int centerX = startX + moduleSize / 2;
        int centerY = startY + moduleSize / 2;
        for (int y = centerY - 1; y <= centerY + 1; y++) {
            for (int x = centerX - 1; x <= centerX + 1; x++) {
                if (y >= 0 && y < totalSize && x >= 0 && x < totalSize) {
                    pw.setColor(x, y, Color.BLACK);
                }
            }
        }
    }

    // Dessiner le pattern de données
    private static void drawDataPattern(PixelWriter pw, String data, int size) {
        Random random = new Random(data.hashCode());
        int moduleSize = 4;

        for (int y = 0; y < size; y += moduleSize) {
            for (int x = 0; x < size; x += moduleSize) {
                // Éviter les marqueurs de position
                if ((x < 20 && y < 20) ||
                        (x > size - 25 && y < 20) ||
                        (x < 20 && y > size - 25)) {
                    continue;
                }

                // Décider aléatoirement mais de manière déterministe si le module est noir
                boolean isBlack = random.nextBoolean();

                for (int dy = 0; dy < moduleSize && y + dy < size; dy++) {
                    for (int dx = 0; dx < moduleSize && x + dx < size; dx++) {
                        if (isBlack) {
                            pw.setColor(x + dx, y + dy, Color.BLACK);
                        }
                    }
                }
            }
        }
    }

    // Générer un code de vérification textuel
    public static String generateVerificationCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    // Générer une URL pour un QR code en ligne (corrigé)
    public static String generateQRCodeURL(String data) {
        try {
            // Utiliser StandardCharsets.UTF_8 au lieu de la méthode dépréciée
            String encodedData = URLEncoder.encode(data, StandardCharsets.UTF_8);
            return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encodedData;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'encodage URL: " + e.getMessage());
            return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=LOOPI";
        }
    }

    // Télécharger une image QR code
    public static Image downloadQRCodeImage(String data) {
        try {
            String url = generateQRCodeURL(data);
            return new Image(url);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du téléchargement du QR code: " + e.getMessage());
            return null;
        }
    }

    // Méthode alternative simplifiée
    public static ImageView generateSimpleQRCode(String text) {
        // Créer une image simple avec le texte
        int size = 200;
        WritableImage image = new WritableImage(size, size);
        PixelWriter pw = image.getPixelWriter();

        // Fond blanc
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                pw.setColor(x, y, Color.WHITE);
            }
        }

        // Dessiner un cadre
        for (int i = 0; i < size; i++) {
            pw.setColor(i, 0, Color.BLACK);
            pw.setColor(i, size-1, Color.BLACK);
            pw.setColor(0, i, Color.BLACK);
            pw.setColor(size-1, i, Color.BLACK);
        }

        // Texte simplifié
        String displayText = text.length() > 20 ? text.substring(0, 20) + "..." : text;

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);

        return imageView;
    }
}