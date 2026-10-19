package com.linguaflow.linguaflow.service.ai;

public interface TranslationProvider {

    String translate(String text, String sourceLanguageCode, String targetLanguageCode);
}