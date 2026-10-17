package tourism_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tourism_api.entity.Category;

public interface CategoryRepository
        extends JpaRepository<Category, Integer> {
}
