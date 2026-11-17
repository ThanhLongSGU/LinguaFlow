package com.linguaflow.linguaflow.repository;

import com.linguaflow.linguaflow.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}