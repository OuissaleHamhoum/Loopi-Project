package edu.Loopi.config;

public class ApiConfig {

    // ==================== OPENAI CONFIGURATION ====================
    public static final String OPENAI_API_KEY = "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";

    // Modèles OpenAI
    public static final String GPT4_VISION_MODEL = "gpt-4-vision-preview";
    public static final String GPT35_TURBO_MODEL = "gpt-3.5-turbo";
    public static final String GPT4_MODEL = "gpt-4";

    // ✅ AJOUTEZ CETTE LIGNE POUR LA COMPATIBILITÉ
    public static final String GPT_MODEL = GPT35_TURBO_MODEL; // Par défaut, utilise GPT-3.5

    // URLs
    public static final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";

    // ==================== PARAMÈTRES ====================
    public static final boolean USE_REAL_API = false;
    public static final int API_TIMEOUT = 30000;
    public static final int MAX_TOKENS_DESCRIPTION = 500;
    public static final int MAX_TOKENS_CHAT = 200;
    public static final int MAX_TOKENS_SENTIMENT = 150;

    // Températures
    public static final double TEMPERATURE_DESCRIPTION = 0.8;
    public static final double TEMPERATURE_CHAT = 0.7;
    public static final double TEMPERATURE_SENTIMENT = 0.3;
}