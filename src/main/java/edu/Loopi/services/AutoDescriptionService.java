// AutoDescriptionService.java
package edu.Loopi.services;

import edu.Loopi.config.ApiConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AutoDescriptionService {

    private static final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_API_KEY = ApiConfig.OPENAI_API_KEY;

    /**
     * Génère une description professionnelle avec OpenAI
     */
    public String generateDescription(String productName, String category, String keywords) {
        try {
            String prompt = String.format(
                    "En tant que rédacteur professionnel pour une plateforme d'art recyclé LOOPI, " +
                            "crée une description attrayante et détaillée pour un produit artisanal.\n\n" +
                            "Produit: %s\nCatégorie: %s\nMots-clés: %s\n\n" +
                            "La description doit:\n" +
                            "- Être entre 100 et 200 mots\n" +
                            "- Mettre en avant l'aspect écologique et l'upcycling\n" +
                            "- Être professionnelle et engageante\n" +
                            "- Inclure les matériaux utilisés (à imaginer de façon cohérente)\n" +
                            "- S'adresser à des amateurs d'art et d'écologie\n" +
                            "- Utiliser un ton inspirant et authentique\n\n" +
                            "Description créative et unique:",
                    productName, category, keywords.isEmpty() ? "art recyclé, écologique, unique" : keywords
            );

            return callOpenAIChat(prompt);

        } catch (Exception e) {
            System.err.println("❌ Erreur génération OpenAI: " + e.getMessage());
            return generateFallbackDescription(productName, category);
        }
    }

    /**
     * Appel à l'API ChatGPT
     */
    private String callOpenAIChat(String prompt) throws IOException {
        URL url = new URL(OPENAI_CHAT_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Construction de la requête
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", ApiConfig.GPT_MODEL);
        requestBody.addProperty("temperature", 0.8);
        requestBody.addProperty("max_tokens", 500);

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "Tu es un rédacteur créatif spécialisé dans la description de produits artisanaux et écologiques. Ton style est inspirant, authentique et professionnel.");
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);

        requestBody.add("messages", messages);

        // Envoi
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Lecture
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        // Extraction de la réponse
        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        return jsonResponse.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString()
                .trim();
    }

    /**
     * Fallback avec templates
     */
    public String generateFallbackDescription(String productName, String category) {
        String[] templates = {
                "Découvrez **%s**, une création unique qui transforme des matériaux recyclés en œuvre d'art. " +
                        "Chaque pièce de la catégorie '%s' est soigneusement confectionnée à la main, " +
                        "donnant une seconde vie à des matériaux destinés à être jetés. " +
                        "C'est bien plus qu'un simple objet - c'est une déclaration d'amour à notre planète.",

                "**%s** incarne la beauté de l'upcycling. Cette pièce unique de la catégorie '%s' " +
                        "allie créativité artistique et conscience écologique. Fabriqué à partir de matériaux récupérés, " +
                        "chaque détail raconte une histoire de transformation et de renouveau. " +
                        "Un choix parfait pour ceux qui cherchent à décorer leur intérieur avec authenticité.",

                "Laissez-vous séduire par **%s**, un chef-d'œuvre d'éco-conception dans la catégorie '%s'. " +
                        "Cet objet unique est le fruit d'un travail artisanal méticuleux, utilisant des matériaux recyclés " +
                        "pour créer quelque chose de véritablement spécial. En l'adoptant, vous faites le choix d'un art " +
                        "responsable qui a du sens."
        };

        java.util.Random rand = new java.util.Random();
        String template = templates[rand.nextInt(templates.length)];
        return String.format(template, productName, category);
    }
}