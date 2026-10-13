package com.linguaflow.linguaflow.controller;

import com.linguaflow.linguaflow.dto.CategoryDTO;
import com.linguaflow.linguaflow.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> getById(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(categoryService.getById(categoryId));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> create(@RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(categoryDTO));
    }
}