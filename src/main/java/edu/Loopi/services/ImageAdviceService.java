// ImageAdviceService.java
package edu.Loopi.services;

import edu.Loopi.config.ApiConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ImageAdviceService {

    private static final String OPENAI_VISION_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_API_KEY = ApiConfig.OPENAI_API_KEY;

    /**
     * Analyse une image et fournit des conseils professionnels via OpenAI Vision
     */
    public Map<String, String> getImageAdvice(String imagePath) {
        Map<String, String> advice = new HashMap<>();

        try {
            // Lire et encoder l'image en Base64
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Déterminer le type MIME
            String mimeType = getMimeType(imagePath);

            // Appeler l'API OpenAI Vision
            String response = callOpenAIVision(base64Image, mimeType);

            // Parser la réponse
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            String content = jsonResponse.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            // Extraire les conseils de la réponse
            advice.put("analysis", content);

            // Ajouter des conseils structurés
            if (content.toLowerCase().contains("qualité")) {
                advice.put("quality", extractQuality(content));
            }
            if (content.toLowerCase().contains("recommandation")) {
                advice.put("tips", extractTips(content));
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur API Vision: " + e.getMessage());
            // Fallback avec analyse basique
            return getBasicAdvice(imagePath);
        }

        return advice;
    }

    /**
     * Appel réel à l'API OpenAI Vision
     */
    private String callOpenAIVision(String base64Image, String mimeType) throws IOException {
        URL url = new URL(OPENAI_VISION_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Construction de la requête pour Vision API
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-4-vision-preview"); // Modèle Vision
        requestBody.addProperty("max_tokens", 500);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");

        JsonArray content = new JsonArray();

        // Texte de la requête
        JsonObject textPart = new JsonObject();
        textPart.addProperty("type", "text");
        textPart.addProperty("text", "Analyse cette image d'un produit artisanal/recyclé. " +
                "Donne des conseils professionnels sur: la qualité de l'image, la composition, " +
                "l'éclairage, et comment améliorer la photo pour la mise en vente. " +
                "Sois constructif et donne 3-4 recommandations spécifiques.");

        // Image encodée
        JsonObject imagePart = new JsonObject();
        imagePart.addProperty("type", "image_url");
        JsonObject imageUrl = new JsonObject();
        imageUrl.addProperty("url", "data:" + mimeType + ";base64," + base64Image);
        imagePart.add("image_url", imageUrl);

        content.add(textPart);
        content.add(imagePart);
        message.add("content", content);
        messages.add(message);

        requestBody.add("messages", messages);

        // Envoi de la requête
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Lecture de la réponse
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        return response.toString();
    }

    /**
     * Version fallback avec analyse basique
     */
    public Map<String, String> getBasicAdvice(String imagePath) {
        Map<String, String> advice = new HashMap<>();
        File file = new File(imagePath);

        if (file.exists()) {
            long fileSize = file.length();
            String fileName = file.getName().toLowerCase();

            StringBuilder tips = new StringBuilder();

            // Vérifications de base
            if (fileSize < 100 * 1024) {
                tips.append("⚠️ Image trop petite (<100KB). La qualité pourrait être médiocre.\n");
                advice.put("quality", "Faible résolution");
            } else if (fileSize > 5 * 1024 * 1024) {
                tips.append("⚠️ Image trop grande (>5MB). Pensez à la compresser.\n");
                advice.put("quality", "Fichier volumineux");
            } else {
                tips.append("✅ Taille d'image adaptée.\n");
                advice.put("quality", "Bonne");
            }

            // Conseils sur le format
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                tips.append("✅ Format d'image approprié.\n");
                advice.put("format", "OK");
            } else {
                tips.append("⚠️ Format non standard. Préférez JPG ou PNG.\n");
                advice.put("format", "À améliorer");
            }

            // Conseils généraux
            tips.append("\n💡 Conseils généraux pour vos photos:\n");
            tips.append("• Utilisez un fond neutre (blanc ou gris clair)\n");
            tips.append("• Assurez un bon éclairage (lumière naturelle de préférence)\n");
            tips.append("• Montrez le produit sous différents angles\n");
            tips.append("• Évitez les ombres portées");

            advice.put("tips", tips.toString());
        }

        return advice;
    }

    private String getMimeType(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "image/jpeg";
        }
    }

    private String extractQuality(String content) {
        if (content.contains("bonne qualité") || content.contains("excellente")) {
            return "Excellente";
        } else if (content.contains("mauvaise") || content.contains("faible")) {
            return "À améliorer";
        }
        return "Moyenne";
    }

    private String extractTips(String content) {
        // Extraire les lignes contenant des conseils
        String[] lines = content.split("\n");
        StringBuilder tips = new StringBuilder();
        for (String line : lines) {
            if (line.contains("conseil") || line.contains("recommand") ||
                    line.contains("suggère") || line.contains("améliorer")) {
                tips.append(line).append("\n");
            }
        }
        return tips.length() > 0 ? tips.toString() : content;
    }
}