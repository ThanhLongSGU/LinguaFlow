package com.linguaflow.linguaflow.service.ai.mock;

import com.linguaflow.linguaflow.service.ai.TranslationProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "linguaflow.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockTranslationProvider implements TranslationProvider {

    @Override
    public String translate(String text, String sourceLanguageCode, String targetLanguageCode) {
        return "[MOCK-" + targetLanguageCode.toUpperCase() + "] " + text;
    }
}