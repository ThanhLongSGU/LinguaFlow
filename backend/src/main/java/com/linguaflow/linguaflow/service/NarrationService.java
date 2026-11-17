package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.entity.AiTranslationTask;
import com.linguaflow.linguaflow.entity.Language;
import com.linguaflow.linguaflow.entity.Narration;
import com.linguaflow.linguaflow.entity.Translation;
import com.linguaflow.linguaflow.entity.TouristSite;
import com.linguaflow.linguaflow.repository.AiTranslationTaskRepository;
import com.linguaflow.linguaflow.repository.LanguageRepository;
import com.linguaflow.linguaflow.repository.NarrationRepository;
import com.linguaflow.linguaflow.repository.TouristSiteRepository;
import com.linguaflow.linguaflow.repository.TranslationRepository;
import com.linguaflow.linguaflow.service.ai.TextToSpeechProvider;
import com.linguaflow.linguaflow.service.ai.TranslationProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class NarrationService {

    private static final String SOURCE_LANGUAGE_CODE = "vi";

    private final NarrationRepository narrationRepository;
    private final LanguageRepository languageRepository;
    private final TranslationRepository translationRepository;
    private final AiTranslationTaskRepository aiTranslationTaskRepository;
    private final TranslationProvider translationProvider;
    private final TextToSpeechProvider textToSpeechProvider;
    private final TouristSiteRepository touristSiteRepository;
    private final org.springframework.beans.factory.ObjectProvider<NarrationService> selfProvider;

    public String calculateMd5(String content) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("MD5 algorithm is unavailable", exception);
        }
    }

    public Narration createNarration(String title, String originalContent, Integer siteId) {
        TouristSite touristSite = touristSiteRepository.findById(siteId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Tourist site not found with id: " + siteId));

        Narration narration = Narration.builder()
                .title(title)
                .originalContent(originalContent)
                .contentHash(calculateMd5(originalContent))
                .touristSite(touristSite)
                .build();
        Narration savedNarration = narrationRepository.save(narration);

        List<Language> languages = languageRepository.findByActiveTrue();
        List<AiTranslationTask> tasks = languages.stream()
                .map(language -> AiTranslationTask.builder()
                        .narration(savedNarration)
                        .language(language)
                        .status("PENDING")
                        .retryCount(0)
                        .build())
                .map(aiTranslationTaskRepository::save)
                .toList();

        selfProvider.getObject().processAiTranslationTasks(savedNarration, tasks, languages);
        return savedNarration;
    }

    @Async
    public void processAiTranslationTasks(
            Narration narration,
            List<AiTranslationTask> tasks,
            List<Language> languages) {
        try {
            for (AiTranslationTask task : tasks) {
                task.setStatus("PROCESSING");
                aiTranslationTaskRepository.save(task);
            }

            for (int index = 0; index < tasks.size(); index++) {
                AiTranslationTask task = tasks.get(index);
                Language language = languages.get(index);

                try {
                    String translatedText = translationProvider.translate(
                            narration.getOriginalContent(),
                            SOURCE_LANGUAGE_CODE,
                            language.getCode());
                    String audioUrl = textToSpeechProvider.generateAudio(
                            translatedText,
                            language.getCode());

                    Translation translation = Translation.builder()
                            .narration(narration)
                            .language(language)
                            .content(translatedText)
                            .audioUrl(audioUrl)
                            .build();

                    try {
                        translationRepository.save(translation);
                    } catch (DataIntegrityViolationException duplicateTranslation) {
                        translationRepository.findByNarrationAndLanguage(narration, language);
                    }

                    task.setStatus("COMPLETED");
                    task.setErrorMessage(null);
                } catch (Exception exception) {
                    task.setStatus("FAILED");
                    task.setErrorMessage(exception.getMessage());
                }

                aiTranslationTaskRepository.save(task);
            }
        } catch (Exception exception) {
            for (AiTranslationTask task : tasks) {
                task.setStatus("FAILED");
                task.setErrorMessage(exception.getMessage());
                aiTranslationTaskRepository.save(task);
            }
        }
    }
}