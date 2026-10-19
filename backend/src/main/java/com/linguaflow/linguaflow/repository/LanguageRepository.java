package com.linguaflow.linguaflow.repository;

import com.linguaflow.linguaflow.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LanguageRepository extends JpaRepository<Language, Integer> {

    List<Language> findByActiveTrue();
}