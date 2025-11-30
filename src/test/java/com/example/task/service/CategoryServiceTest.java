package com.example.task.service;

import com.example.task.exception.ResourceNotFoundException;
import com.example.task.model.dto.CategoryDto;
import com.example.task.model.entity.Category;
import com.example.task.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllCategories() {
        Category c1 = new Category();
        c1.setId(1L);
        c1.setName("Cat1");

        when(categoryRepository.findAll()).thenReturn(List.of(c1));

        List<CategoryDto> dtos = categoryService.getAllCategories();
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getName()).isEqualTo("Cat1");
    }

    @Test
    void testGetCategoryByIdFound() {
        Category c = new Category();
        c.setId(1L);
        c.setName("Cat1");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));

        CategoryDto dto = categoryService.getCategoryById(1L);
        assertThat(dto.getName()).isEqualTo("Cat1");
    }

    @Test
    void testGetCategoryByIdNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(1L));
    }

    @Test
    void testCreateCategory() {
        Category c = new Category();
        c.setId(1L);
        c.setName("NewCat");

        when(categoryRepository.save(any(Category.class))).thenReturn(c);

        CategoryDto dto = new CategoryDto();
        dto.setName("NewCat");
        CategoryDto result = categoryService.createCategory(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("NewCat");
    }

    @Test
    void testUpdateCategoryFound() {
        Category c = new Category();
        c.setId(1L);
        c.setName("OldName");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));
        when(categoryRepository.save(any(Category.class))).thenReturn(c);

        CategoryDto dto = new CategoryDto();
        dto.setName("UpdatedName");

        CategoryDto updated = categoryService.updateCategory(1L, dto);
        assertThat(updated.getName()).isEqualTo("UpdatedName");
    }

    @Test
    void testUpdateCategoryNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        CategoryDto dto = new CategoryDto();
        dto.setName("Test");
        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(1L, dto));
    }

    @Test
    void testDeleteCategoryFound() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        categoryService.deleteCategory(1L);
        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteCategoryNotFound() {
        when(categoryRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(1L));
    }
}
