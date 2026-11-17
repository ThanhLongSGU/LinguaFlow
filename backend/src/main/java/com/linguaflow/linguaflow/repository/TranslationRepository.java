package com.linguaflow.linguaflow.repository;

import com.linguaflow.linguaflow.entity.Language;
import com.linguaflow.linguaflow.entity.Narration;
import com.linguaflow.linguaflow.entity.Translation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TranslationRepository extends JpaRepository<Translation, Integer> {

    Optional<Translation> findByNarrationAndLanguage(Narration narration, Language language);
}