package tourism_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import tourism_api.entity.Category;
import tourism_api.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
