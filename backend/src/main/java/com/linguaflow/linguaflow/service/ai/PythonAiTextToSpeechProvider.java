package com.linguaflow.linguaflow.service.ai;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PythonAiTextToSpeechProvider implements TextToSpeechProvider {

    private static final String AI_SERVICE_URL = "http://localhost:8000";

    private final RestClient.Builder restClientBuilder;

    @Override
    @Retry(name = "pythonAi")
    public String generateAudio(String text, String languageCode) {
        TtsResponse response = restClientBuilder
                .baseUrl(AI_SERVICE_URL)
                .build()
                .post()
                .uri("/api/tts")
                .body(new TtsRequest(text, languageCode))
                .retrieve()
                .body(TtsResponse.class);

        if (response == null || response.audioPath() == null) {
            throw new IllegalStateException("Python AI service returned an empty audio path");
        }

        return response.audioPath();
    }

    private record TtsRequest(String text, String languageCode) {
    }

    private record TtsResponse(String audioPath) {
    }
}