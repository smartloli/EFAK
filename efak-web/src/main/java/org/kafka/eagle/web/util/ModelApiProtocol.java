package org.kafka.eagle.web.util;

/**
 * Maps configured model vendors to the wire protocol they speak.
 *
 * OpenAI-compatible: OpenAI, DeepSeek, Kimi, Qwen, Doubao, GLM, and unknown vendors.
 * Native: Anthropic Messages API, Ollama.
 */
public final class ModelApiProtocol {

    public static final String OPENAI = "openai";
    public static final String ANTHROPIC = "anthropic";
    public static final String OLLAMA = "ollama";

    private ModelApiProtocol() {
    }

    public static String of(String apiType) {
        if (apiType == null || apiType.isBlank()) {
            return OPENAI;
        }
        switch (apiType.trim().toLowerCase()) {
            case "anthropic":
            case "claude":
            case "custom-anthropic":
                return ANTHROPIC;
            case "ollama":
            case "custom-ollama":
                return OLLAMA;
            default:
                return OPENAI;
        }
    }

    public static boolean isCustom(String apiType) {
        if (apiType == null) {
            return false;
        }
        String value = apiType.trim();
        return value.equalsIgnoreCase("custom")
                || value.equalsIgnoreCase("custom-openai")
                || value.equalsIgnoreCase("custom-anthropic")
                || value.equalsIgnoreCase("custom-ollama");
    }

    public static boolean isAnthropic(String apiType) {
        return ANTHROPIC.equals(of(apiType));
    }

    public static boolean isOllama(String apiType) {
        return OLLAMA.equals(of(apiType));
    }

    public static boolean isOpenAICompatible(String apiType) {
        return OPENAI.equals(of(apiType));
    }
}
