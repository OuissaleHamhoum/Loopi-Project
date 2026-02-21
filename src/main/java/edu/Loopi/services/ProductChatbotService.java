// ProductChatbotService.java
package edu.Loopi.services;

import edu.Loopi.config.ApiConfig;
import edu.Loopi.entities.Produit;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ProductChatbotService {

    private static final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_API_KEY = ApiConfig.OPENAI_API_KEY;

    private Map<String, String> responseCache = new HashMap<>();

    /**
     * Pose une question sur un produit et obtient une réponse d'OpenAI
     */
    public String askAboutProduct(Produit produit, String question) {
        String cacheKey = produit.getId() + "_" + question.hashCode();

        if (responseCache.containsKey(cacheKey)) {
            return responseCache.get(cacheKey);
        }

        try {
            String productInfo = String.format(
                    "Produit: %s\nCatégorie: %s\nDescription: %s\n",
                    produit.getNom(),
                    getCategoryName(produit.getIdCategorie()),
                    produit.getDescription()
            );

            String prompt = String.format(
                    "Tu es un assistant virtuel pour LOOPI, une plateforme d'art recyclé. " +
                            "Voici les informations sur un produit:\n%s\n\n" +
                            "Question de l'utilisateur: \"%s\"\n\n" +
                            "Réponds de façon amicale, professionnelle et concise (max 3 phrases). " +
                            "Mets en avant les aspects écologiques et uniques du produit.",
                    productInfo, question
            );

            String response = callOpenAIChat(prompt);
            responseCache.put(cacheKey, response);
            return response;

        } catch (Exception e) {
            System.err.println("❌ Erreur chatbot OpenAI: " + e.getMessage());
            return generateFallbackResponse(produit, question);
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

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", ApiConfig.GPT_MODEL);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 200);

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "Tu es un assistant amical et professionnel spécialisé dans les produits d'art recyclé. Tu es concis et enthousiaste.");
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);

        requestBody.add("messages", messages);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        return jsonResponse.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString()
                .trim();
    }

    /**
     * Version fallback
     */
    public String generateFallbackResponse(Produit produit, String question) {
        question = question.toLowerCase();
        String productName = produit.getNom();

        Map<String, String> responses = new HashMap<>();
        responses.put("écologique",
                "🌱 Ce produit est fabriqué à partir de matériaux recyclés, réduisant ainsi l'impact environnemental. " +
                        "Chaque achat contribue à l'économie circulaire !");

        responses.put("durable",
                "⏳ Les produits recyclés sont généralement très durables car ils sont conçus pour durer. " +
                        "Avec un entretien simple, vous en profiterez longtemps !");

        responses.put("unique",
                "✨ Chaque pièce est unique ! Les variations dans les matériaux recyclés rendent chaque création originale.");

        responses.put("prix",
                "💰 Le prix reflète le travail artisanal et la qualité des matériaux. C'est un investissement dans un objet unique et durable.");

        responses.put("entretien",
                "🧹 Pour l'entretien, un chiffon doux et sec suffit. Évitez l'eau et les produits chimiques.");

        responses.put("livraison",
                "📦 La livraison est effectuée sous 3-5 jours ouvrés avec suivi. Vous serez notifié à chaque étape !");

        responses.put("garantie",
                "✅ Tous nos produits bénéficient d'une garantie satisfait ou remboursé de 14 jours.");

        for (Map.Entry<String, String> entry : responses.entrySet()) {
            if (question.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "🤖 " + productName + " est une création unique alliant art et écologie. " +
                "Puis-je vous aider avec une question plus spécifique sur ses caractéristiques, " +
                "son entretien ou sa livraison ?";
    }

    private String getCategoryName(int idCat) {
        switch(idCat) {
            case 1: return "Objets décoratifs";
            case 2: return "Art mural";
            case 3: return "Mobilier artistique";
            case 4: return "Installations artistiques";
            default: return "Artisanat";
        }
    }
}