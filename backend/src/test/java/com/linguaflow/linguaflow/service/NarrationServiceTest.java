package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.entity.AiTranslationTask;
import com.linguaflow.linguaflow.entity.Language;
import com.linguaflow.linguaflow.entity.Narration;
import com.linguaflow.linguaflow.entity.TouristSite;
import com.linguaflow.linguaflow.entity.Translation;
import com.linguaflow.linguaflow.repository.AiTranslationTaskRepository;
import com.linguaflow.linguaflow.repository.LanguageRepository;
import com.linguaflow.linguaflow.repository.NarrationRepository;
import com.linguaflow.linguaflow.repository.TouristSiteRepository;
import com.linguaflow.linguaflow.repository.TranslationRepository;
import com.linguaflow.linguaflow.service.ai.TextToSpeechProvider;
import com.linguaflow.linguaflow.service.ai.TranslationProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NarrationServiceTest {

    @Mock
    private NarrationRepository narrationRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private TranslationRepository translationRepository;

    @Mock
    private AiTranslationTaskRepository aiTranslationTaskRepository;

    @Mock
    private TranslationProvider translationProvider;

    @Mock
    private TextToSpeechProvider textToSpeechProvider;

    @Mock
    private TouristSiteRepository touristSiteRepository;

    @Mock
    private ObjectProvider<NarrationService> selfProvider;

    @InjectMocks
    private NarrationService narrationService;

    @Test
    void testCreateNarration_ShouldReturnNarration_AndSavePendingTasks() {
        var site = TouristSite.builder()
                .siteId(7)
                .build();
        var language = Language.builder()
                .languageId(1)
                .code("en")
                .active(true)
                .build();
        var savedNarration = Narration.builder()
                .narrationId(10)
                .touristSite(site)
                .title("Welcome")
                .originalContent("Xin chao")
                .build();

        when(touristSiteRepository.findById(7)).thenReturn(Optional.of(site));
        when(narrationRepository.save(any(Narration.class))).thenReturn(savedNarration);
        when(languageRepository.findByActiveTrue()).thenReturn(List.of(language));
        when(aiTranslationTaskRepository.save(any(AiTranslationTask.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(selfProvider.getObject()).thenReturn(new AsyncNarrationService());

        var result = narrationService.createNarration("Welcome", "Xin chao", 7);

        assertSameNarration(result, savedNarration);

        verify(aiTranslationTaskRepository).save(argThat(task -> "PENDING".equals(task.getStatus())
                && task.getRetryCount() == 0
                && task.getNarration() == savedNarration));
    }

    @Test
    void testProcessAiTranslationTasks_ShouldUpdateToCompleted() throws NoSuchMethodException {
        var language = Language.builder()
                .languageId(1)
                .code("en")
                .name("English")
                .active(true)
                .build();
        var narration = Narration.builder()
                .narrationId(10)
                .originalContent("Hello")
                .build();
        var task = AiTranslationTask.builder()
                .taskId(100L)
                .narration(narration)
                .language(language)
                .status("PENDING")
                .retryCount(0)
                .build();

        when(translationProvider.translate("Hello", "vi", "en"))
                .thenReturn("[MOCK] Hello");
        when(textToSpeechProvider.generateAudio("[MOCK] Hello", "en"))
                .thenReturn("/static/audio/mock.mp3");

        narrationService.processAiTranslationTasks(
                narration,
                List.of(task),
                List.of(language));

        verify(translationProvider).translate("Hello", "vi", "en");
        verify(textToSpeechProvider).generateAudio("[MOCK] Hello", "en");
        verify(translationRepository).save(any(Translation.class));
        verify(aiTranslationTaskRepository, atLeastOnce())
                .save(task);
        assertEquals("COMPLETED", task.getStatus());
        assertNull(task.getErrorMessage());
        assertTrue(NarrationService.class
                .getMethod("processAiTranslationTasks", Narration.class, List.class, List.class)
                .isAnnotationPresent(Async.class));
    }

    private void assertSameNarration(Narration actual, Narration expected) {
        assertNotNull(actual);
        assertEquals(expected.getNarrationId(), actual.getNarrationId());
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getOriginalContent(), actual.getOriginalContent());
    }

    private static final class AsyncNarrationService extends NarrationService {

        private AsyncNarrationService() {
            super(null, null, null, null, null, null, null, null);
        }

        @Override
        public void processAiTranslationTasks(
                Narration narration,
                List<AiTranslationTask> tasks,
                List<Language> languages) {
        }
    }
}
