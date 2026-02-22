package edu.Loopi.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;

public class AIImageGenerationService {

    private static final String CONFIG_FILE = "src/main/resources/config/api.properties";
    private static final String PROJECT_ROOT = System.getProperty("user.dir");

    // 📁 Dossier pour les images générées par IA
    private static final String AI_IMAGE_STORAGE_DIR = "src" + File.separator + "main" + File.separator +
            "resources" + File.separator + "uploads" + File.separator +
            "ai_generated" + File.separator;
    private static final String FULL_AI_IMAGE_PATH = PROJECT_ROOT + File.separator + AI_IMAGE_STORAGE_DIR;
    private static final String DB_AI_IMAGE_PATH = "uploads/ai_generated/";

    private String apiKey;
    private String apiUrl;
    private int width;
    private int height;
    private boolean useDemoMode = false;

    public AIImageGenerationService() {
        loadConfiguration();
        createUploadDirectory();
    }

    private void loadConfiguration() {
        Properties properties = new Properties();
        File configFile = new File(CONFIG_FILE);

        try {
            // Créer le dossier config s'il n'existe pas
            configFile.getParentFile().mkdirs();

            if (configFile.exists()) {
                try (InputStream input = new FileInputStream(configFile)) {
                    properties.load(input);
                }

                this.apiKey = properties.getProperty("stability.api.key");
                this.apiUrl = properties.getProperty("stability.api.url");
                this.width = Integer.parseInt(properties.getProperty("ai.image.default.width", "1024"));
                this.height = Integer.parseInt(properties.getProperty("ai.image.default.height", "1024"));

                if (apiKey == null || apiKey.isEmpty() || apiKey.equals("votre_cle_api_stability_ai")) {
                    System.out.println("⚠️ Mode démo - Clé API Stability AI non configurée");
                    this.useDemoMode = true;
                } else {
                    System.out.println("✅ API Stability AI configurée avec la clé: " + apiKey.substring(0, 10) + "...");
                }
            } else {
                System.err.println("❌ Fichier de configuration non trouvé: " + CONFIG_FILE);
                System.out.println("📝 Veuillez créer le fichier avec votre clé API Stability AI");
                this.useDemoMode = true;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement configuration: " + e.getMessage());
            this.useDemoMode = true;
        }
    }

    private void createUploadDirectory() {
        File directory = new File(FULL_AI_IMAGE_PATH);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("✅ Dossier créé pour les images IA: " + FULL_AI_IMAGE_PATH);
            } else {
                System.err.println("❌ Impossible de créer le dossier: " + FULL_AI_IMAGE_PATH);
            }
        }
    }

    public String generateEventImage(String titre, String description) {
        if (useDemoMode) {
            System.out.println("🎨 Mode démo - Génération d'un placeholder");
            return generateLocalPlaceholder(titre);
        }

        try {
            // Construire un prompt simple en anglais sans mention de "poster" ou "text"
            String prompt = buildImagePrompt(titre, description);
            System.out.println("🎨 Génération d'image avec Stability AI...");
            System.out.println("📝 Prompt: " + prompt);

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(90000);
            connection.setDoOutput(true);

            JsonObject requestBody = new JsonObject();
            JsonObject textPrompt = new JsonObject();
            textPrompt.addProperty("text", prompt);

            JsonArray textPrompts = new JsonArray();
            textPrompts.add(textPrompt);

            requestBody.add("text_prompts", textPrompts);
            requestBody.addProperty("cfg_scale", 7);
            requestBody.addProperty("height", height);
            requestBody.addProperty("width", width);
            requestBody.addProperty("samples", 1);
            requestBody.addProperty("steps", 50);

            String jsonBody = requestBody.toString();

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("📥 Réponse reçue - Code: " + responseCode);

            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    String responseStr = response.toString();
                    System.out.println("✅ Réponse JSON reçue, longueur: " + responseStr.length());

                    return saveImageFromResponse(responseStr);
                }
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                    System.err.println("❌ Erreur API Stability AI: " + errorResponse.toString());
                }
                return generateLocalPlaceholder(titre);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur génération image: " + e.getMessage());
            e.printStackTrace();
            return generateLocalPlaceholder(titre);
        }
    }

    private String buildImagePrompt(String titre, String description) {
        // Traduire en anglais
        String englishText = translateToEnglish(titre + " " + description);

        // Prompt simple sans mention de texte ou poster
        return String.format("Realistic photograph of %s, natural lighting, high quality, 4k, detailed, no text, no writing",
                englishText);
    }

    private String translateToEnglish(String text) {
        String result = text.toLowerCase();

        String[][] translations = {
                {"nettoyage", "cleanup"},
                {"plage", "beach"},
                {"mer", "sea"},
                {"forêt", "forest"},
                {"jardin", "garden"},
                {"atelier", "workshop"},
                {"formation", "training"},
                {"recyclage", "recycling"},
                {"écologique", "ecological"},
                {"nature", "nature"},
                {"balade", "walk"},
                {"randonnée", "hike"},
                {"découverte", "discovery"},
                {"plantes", "plants"},
                {"aromatiques", "aromatic"},
                {"médicinales", "medicinal"},
                {"sauvage", "wild"},
                {"cueillette", "harvesting"},
                {"conseils", "tips"},
                {"bienfaits", "benefits"},
                {"guidée", "guided"},
                {"parc", "park"},
                {"national", "national"},
                {"botanique", "botanical"},
                {"romarin", "rosemary"},
                {"thym", "thyme"},
                {"lavande", "lavender"},
                {"hello", "hello"},
                {"test", "test"}
        };

        for (String[] pair : translations) {
            result = result.replace(pair[0], pair[1]);
        }

        return result;
    }

    private String generateLocalPlaceholder(String titre) {
        String uniqueFileName = "event_placeholder_" + UUID.randomUUID().toString() + ".jpg";
        String fullPath = FULL_AI_IMAGE_PATH + uniqueFileName;

        try {
            String[] fallbackImages = {
                    "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?w=600&auto=format",
                    "https://images.unsplash.com/photo-1618477462146-050d2416e273?w=600&auto=format",
                    "https://images.unsplash.com/photo-1621451537084-482db730ca22?w=600&auto=format",
                    "https://images.unsplash.com/photo-1507525425510-1e2d6a4a9f5a?w=600&auto=format",
                    "https://images.unsplash.com/photo-1519046904884-53103b34b206?w=600&auto=format"
            };

            String imageUrl = fallbackImages[(int)(Math.random() * fallbackImages.length)];

            System.out.println("📥 Téléchargement image de secours: " + imageUrl);

            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            System.out.println("📥 Code réponse: " + responseCode);

            if (responseCode == 200) {
                try (InputStream in = connection.getInputStream()) {
                    Files.copy(in, Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);
                }

                File savedFile = new File(fullPath);
                if (savedFile.exists() && savedFile.length() > 0) {
                    System.out.println("✅ Image de secours sauvegardée: " + fullPath + " (" + savedFile.length() + " bytes)");
                    return DB_AI_IMAGE_PATH + uniqueFileName;
                }
            }

            return null;

        } catch (Exception e) {
            System.err.println("❌ Erreur téléchargement placeholder: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String saveImageFromResponse(String jsonResponse) throws Exception {
        JsonObject response = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray artifacts = response.getAsJsonArray("artifacts");

        if (artifacts != null && artifacts.size() > 0) {
            String base64Image = artifacts.get(0).getAsJsonObject().get("base64").getAsString();

            // Décoder l'image base64
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // Sauvegarder l'image en JPEG
            String uniqueFileName = "event_ai_" + UUID.randomUUID().toString() + ".jpg";
            String fullPath = FULL_AI_IMAGE_PATH + uniqueFileName;

            Files.write(Paths.get(fullPath), imageBytes);

            File savedFile = new File(fullPath);
            if (savedFile.exists() && savedFile.length() > 0) {
                System.out.println("✅ Image IA sauvegardée: " + fullPath + " (" + savedFile.length() + " bytes)");
                return DB_AI_IMAGE_PATH + uniqueFileName;
            }
        }

        System.err.println("❌ Aucun artifact trouvé dans la réponse");
        return null;
    }

    public boolean isConfigured() {
        return !useDemoMode;
    }

    public String getModelName() {
        return useDemoMode ? "Mode Démo" : "Stability AI";
    }
}