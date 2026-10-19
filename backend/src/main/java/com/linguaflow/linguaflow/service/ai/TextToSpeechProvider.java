package com.linguaflow.linguaflow.service.ai;

public interface TextToSpeechProvider {

    String generateAudio(String text, String languageCode);
}