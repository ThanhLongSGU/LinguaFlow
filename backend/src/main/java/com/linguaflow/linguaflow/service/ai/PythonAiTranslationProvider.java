package com.linguaflow.linguaflow.service.ai;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PythonAiTranslationProvider implements TranslationProvider {

    private static final String AI_SERVICE_URL = "http://localhost:8000";

    private final RestClient.Builder restClientBuilder;

    @Override
    @Retry(name = "pythonAi")
    public String translate(String text, String sourceLanguageCode, String targetLanguageCode) {
        TranslateResponse response = restClientBuilder
                .baseUrl(AI_SERVICE_URL)
                .build()
                .post()
                .uri("/api/translate")
                .body(new TranslateRequest(text, sourceLanguageCode, targetLanguageCode))
                .retrieve()
                .body(TranslateResponse.class);

        if (response == null || response.translatedText() == null) {
            throw new IllegalStateException("Python AI service returned an empty translation");
        }

        return response.translatedText();
    }

    private record TranslateRequest(
            String text,
            String sourceLanguageCode,
            String targetLanguageCode) {
    }

    private record TranslateResponse(String translatedText) {
    }
}