// SentimentAnalysisService.java
package edu.Loopi.services;

import edu.Loopi.config.ApiConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SentimentAnalysisService {

    private static final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_API_KEY = ApiConfig.OPENAI_API_KEY;

    /**
     * Analyse le sentiment d'un commentaire avec OpenAI
     */
    public Map<String, Object> analyzeSentiment(String text) {
        Map<String, Object> result = new HashMap<>();

        try {
            String prompt = String.format(
                    "Analyse le sentiment du commentaire suivant concernant un produit artisanal recyclé. " +
                            "Retourne UNIQUEMENT un objet JSON avec les champs: sentiment (POSITIF/NÉGATIF/NEUTRE), " +
                            "score (entre 0 et 1), et une courte explication.\n\n" +
                            "Commentaire: \"%s\"",
                    text
            );

            String response = callOpenAIChat(prompt);

            // Parser la réponse JSON
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            String sentiment = json.get("sentiment").getAsString();
            double score = json.get("score").getAsDouble();
            String explanation = json.get("explication").getAsString();

            result.put("sentiment", sentiment);
            result.put("score", score);
            result.put("explanation", explanation);
            result.put("emoji", getSentimentEmoji(sentiment));
            result.put("color", getSentimentColor(sentiment));

        } catch (Exception e) {
            System.err.println("❌ Erreur analyse sentiment OpenAI: " + e.getMessage());
            // Fallback
            result = analyzeSentimentBasic(text);
        }

        return result;
    }

    /**
     * Appel à l'API ChatGPT pour l'analyse
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
        requestBody.addProperty("temperature", 0.3);
        requestBody.addProperty("max_tokens", 150);

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "Tu es un expert en analyse de sentiment. Tu réponds UNIQUEMENT avec un objet JSON valide contenant les champs demandés.");
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
     * Version fallback avec analyse basique
     */
    public Map<String, Object> analyzeSentimentBasic(String text) {
        Map<String, Object> result = new HashMap<>();
        text = text.toLowerCase();

        String[] positiveWords = {"super", "excellent", "magnifique", "génial", "beau",
                "j'adore", "aime", "parfait", "incroyable", "merveilleux",
                "satisfait", "content", "qualité", "bravo", "recommande"};

        String[] negativeWords = {"mauvais", "déçu", "déception", "dommage", "pas beau",
                "horrible", "terrible", "cassé", "abîmé", "cher",
                "problème", "insatisfait", "médiocre", "délai"};

        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : positiveWords) {
            if (text.contains(word)) positiveCount++;
        }

        for (String word : negativeWords) {
            if (text.contains(word)) negativeCount++;
        }

        String sentiment;
        double score;

        if (positiveCount > negativeCount) {
            sentiment = "POSITIF";
            score = 0.5 + (0.5 * positiveCount / Math.max(positiveCount + negativeCount, 1));
        } else if (negativeCount > positiveCount) {
            sentiment = "NÉGATIF";
            score = 0.5 - (0.5 * negativeCount / Math.max(positiveCount + negativeCount, 1));
        } else {
            sentiment = "NEUTRE";
            score = 0.5;
        }

        result.put("sentiment", sentiment);
        result.put("score", Math.min(1.0, Math.max(0.0, score)));
        result.put("emoji", getSentimentEmoji(sentiment));
        result.put("color", getSentimentColor(sentiment));
        result.put("explanation", "Analyse basée sur " + positiveCount + " mots positifs, " + negativeCount + " mots négatifs");

        return result;
    }

    private String getSentimentEmoji(String sentiment) {
        switch (sentiment.toUpperCase()) {
            case "POSITIF": return "😊";
            case "NÉGATIF": return "😞";
            case "NEUTRE": return "😐";
            default: return "🤔";
        }
    }

    private String getSentimentColor(String sentiment) {
        switch (sentiment.toUpperCase()) {
            case "POSITIF": return "#10b981";
            case "NÉGATIF": return "#ef4444";
            case "NEUTRE": return "#f59e0b";
            default: return "#6b7280";
        }
    }
}
