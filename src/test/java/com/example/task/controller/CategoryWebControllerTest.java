package com.example.task.controller;

import com.example.task.controller.web.CategoryWebController;
import com.example.task.model.TaskStatus;
import com.example.task.model.dto.TaskCreateDto;
import com.example.task.model.dto.TaskDto;
import com.example.task.service.CategoryService;
import com.example.task.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryWebController.class)


@WithMockUser
class CategoryWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    // ---------- GET /categories ----------
    @Test
    void listCategories_ShouldReturnList() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/list"))
                .andExpect(model().attributeExists("categories"));
    }

    // ---------- GET /categories/new ----------
    @Test
    void newCategoryForm_ShouldReturnForm() throws Exception {
        mockMvc.perform(get("/categories/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/form"));
    }

    // ---------- GET /categories/{id} ----------
    @Test
    void editCategoryForm_ShouldReturnForm() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(new com.example.task.model.dto.CategoryDto());

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/form"));
    }

    // ---------- POST /categories ----------
    @Test
    void createCategory_Valid_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/categories")
                        .with(csrf())
                        .param("name", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        verify(categoryService).createCategory(any());
    }

    @Test
    void createCategory_Invalid_ShouldReturnForm() throws Exception {
        mockMvc.perform(post("/categories").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/form"));

        verify(categoryService, never()).createCategory(any());
    }

    // ---------- POST /categories/{id} ----------
    @Test
    void updateCategory_Valid_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/categories/1")
                        .with(csrf())
                        .param("name", "Updated"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        verify(categoryService).updateCategory(eq(1L), any());
    }

    // ---------- DELETE /categories/{id} ----------
    @Test
    void deleteCategory_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/categories/1")
                        .with(csrf())
                        .param("_method", "DELETE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        verify(categoryService).deleteCategory(1L);
    }
}
