// src/main/java/com/example/task/service/CategoryService.java
package com.example.task.service;

import com.example.task.exception.ResourceNotFoundException;
import com.example.task.mapper.TaskMapper;
import com.example.task.model.dto.CategoryDto;
import com.example.task.model.entity.Category;
import com.example.task.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TaskMapper taskMapper; // nie używamy, ale zostawiamy

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategoria o id " + id + " nie istnieje"));
        return toDto(category);
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setColor(dto.getColor());
        category = categoryRepository.save(category);
        return toDto(category);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategoria o id " + id + " nie istnieje"));
        category.setName(dto.getName());
        category.setColor(dto.getColor());
        category = categoryRepository.save(category);
        return toDto(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Kategoria o id " + id + " nie istnieje");
        }
        // ON DELETE SET NULL w SQL → zadania zostaną z category_id = NULL
        categoryRepository.deleteById(id);
    }

    private CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setColor(category.getColor());
        return dto;
    }
}