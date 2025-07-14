package org.quarkos;

public enum Model {
    GEMINI_2_5_FLASH_PREVIEW_05_20("Gemini 2.5 Flash Preview 05-20", "gemini-2.5-flash-preview-05-20", "pretty much the same as gemini-2.5-flash", true),
    GEMINI_2_5_FLASH("Gemini 2.5 Flash", "gemini-2.5-flash", "gemini-2.5-flash", true),
    //GEMINI_2_5_PRO("Gemini 2.5 Pro", "gemini-2.5-pro", "Probably too slow and most definitely too expensive; Might add usage for premium API users later", true),
    GEMINI_2_5_FLASH_LITE_PREVIEW_06_17("Gemini 2.5 Flash Lite Preview 06-17", "gemini-2.5-flash-lite-preview-06-17", "Perfect for quiz questions, fast and good", false),
    GEMINI_2_0_FLASH("Gemini 2.0 Flash", "gemini-2.0-flash", "Pretty good, pretty much the same as gemini-2.5-flash-lite-preview-06-17, however the answers are sometimes not elaborated enough", true),
    GEMINI_2_0_FLASH_LITE("Gemini 2.0 Flash Lite", "gemini-2.0-flash-lite", "It's alright, but not as good as gemini-2.5-flash-lite-preview-06-17", true);
    //GEMINI_2_5_FLASH_PREVIEW_TTS("Gemini 2.5 Flash Preview TTS", "gemini-2.5-flash-preview-tts", "Text-to-Speech model for Gemini 2.5 Flash Preview", false);

    private final String displayName;
    private final String modelName;
    private final String descriptor;
    private final Boolean isThinkingEnabled;

    Model(String displayName, String modelName, String descriptor, Boolean isThinkingEnabled) {
        this.displayName = displayName;
        this.modelName = modelName;
        this.descriptor = descriptor;
        this.isThinkingEnabled = isThinkingEnabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getModelName() {
        return modelName;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public Boolean isThinkingEnabled() {
        return isThinkingEnabled;
    }

    @Override
    public String toString() {
        return displayName;
    }
}