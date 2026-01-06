package com.example.task.repository;

import com.example.task.model.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void save_ShouldPersistCategory() {
        Category category = new Category();
        category.setName("Work");
        category.setColor("#111");

        Category saved = categoryRepository.save(category);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Work");
    }

    @Test
    void findById_ShouldReturnCategory() {
        Category category = new Category();
        category.setName("Home");
        category.setColor("#222");

        Category saved = categoryRepository.save(category);

        Optional<Category> found = categoryRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Home");
    }

    @Test
    void findAll_ShouldReturnMultipleCategories() {
        Category c1 = new Category();
        c1.setName("A");

        Category c2 = new Category();
        c2.setName("B");

        categoryRepository.save(c1);
        categoryRepository.save(c2);

        assertThat(categoryRepository.findAll()).hasSize(2);
    }

    @Test
    void deleteById_ShouldRemoveCategory() {
        Category category = new Category();
        category.setName("Temp");

        Category saved = categoryRepository.save(category);
        categoryRepository.deleteById(saved.getId());

        assertThat(categoryRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void existsById_ShouldReturnTrue() {
        Category category = new Category();
        category.setName("Exists");

        Category saved = categoryRepository.save(category);

        assertThat(categoryRepository.existsById(saved.getId())).isTrue();
    }
}
