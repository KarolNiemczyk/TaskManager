package com.example.task.service;

import com.example.task.exception.ResourceNotFoundException;
import com.example.task.model.dto.CategoryDto;
import com.example.task.model.entity.Category;
import com.example.task.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

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

    // ---------------- getAllCategories ----------------
    @Test
    void getAllCategories_ShouldReturnListOfDtos() {
        Category c1 = new Category();
        c1.setId(1L);
        c1.setName("Cat1");
        c1.setColor("#FFF");

        Category c2 = new Category();
        c2.setId(2L);
        c2.setName("Cat2");

        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));

        List<CategoryDto> dtos = categoryService.getAllCategories();

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getName()).isEqualTo("Cat1");
        assertThat(dtos.get(0).getColor()).isEqualTo("#FFF");
        assertThat(dtos.get(1).getName()).isEqualTo("Cat2");

        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void getAllCategories_EmptyList_ShouldReturnEmpty() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryDto> dtos = categoryService.getAllCategories();
        assertThat(dtos).isEmpty();

        verify(categoryRepository, times(1)).findAll();
    }

    // ---------------- getCategoryById ----------------
    @Test
    void getCategoryById_Found_ShouldReturnDto() {
        Category c = new Category();
        c.setId(1L);
        c.setName("Cat1");
        c.setColor("#AAA");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));

        CategoryDto dto = categoryService.getCategoryById(1L);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Cat1");
        assertThat(dto.getColor()).isEqualTo("#AAA");

        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void getCategoryById_NotFound_ShouldThrow() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(1L));

        verify(categoryRepository, times(1)).findById(1L);
    }

    // ---------------- createCategory ----------------
    @Test
    void createCategory_ShouldSaveAndReturnDto() {
        CategoryDto dto = new CategoryDto();
        dto.setName("NewCat");
        dto.setColor("#123");

        Category saved = new Category();
        saved.setId(1L);
        saved.setName("NewCat");
        saved.setColor("#123");

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryDto result = categoryService.createCategory(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("NewCat");
        assertThat(result.getColor()).isEqualTo("#123");

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(captor.capture());

        Category toSave = captor.getValue();
        assertThat(toSave.getName()).isEqualTo("NewCat");
        assertThat(toSave.getColor()).isEqualTo("#123");
    }

    // ---------------- updateCategory ----------------
    @Test
    void updateCategory_Found_ShouldUpdateAndReturnDto() {
        Category existing = new Category();
        existing.setId(1L);
        existing.setName("OldName");
        existing.setColor("#AAA");

        CategoryDto dto = new CategoryDto();
        dto.setName("Updated");
        dto.setColor("#BBB");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryDto updated = categoryService.updateCategory(1L, dto);

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getColor()).isEqualTo("#BBB");

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(existing);
    }

    @Test
    void updateCategory_NotFound_ShouldThrow() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        CategoryDto dto = new CategoryDto();
        dto.setName("Updated");

        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(1L, dto));

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).save(any());
    }

    // ---------------- deleteCategory ----------------
    @Test
    void deleteCategory_Exists_ShouldDelete() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).existsById(1L);
        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCategory_NotFound_ShouldThrow() {
        when(categoryRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(1L));

        verify(categoryRepository, times(1)).existsById(1L);
        verify(categoryRepository, never()).deleteById(anyLong());
    }

}
