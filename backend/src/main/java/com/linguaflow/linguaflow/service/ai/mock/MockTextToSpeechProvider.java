package com.linguaflow.linguaflow.service.ai.mock;

import com.linguaflow.linguaflow.service.ai.TextToSpeechProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "linguaflow.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockTextToSpeechProvider implements TextToSpeechProvider {

    @Override
    public String generateAudio(String text, String languageCode) {
        return "/static/audio/mock-" + languageCode.toLowerCase() + ".mp3";
    }
}