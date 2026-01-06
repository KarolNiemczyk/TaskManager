package com.example.task.controller;

import com.example.task.controller.api.CategoryApiController;
import com.example.task.model.dto.CategoryDto;
import com.example.task.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryApiController.class)
@WithMockUser
class CategoryApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------- GET /categories ----------------
    @Test
    void getAllCategories_ShouldReturnList() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setId(1L); dto.setName("Cat1");

        when(categoryService.getAllCategories()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cat1"));

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void getAllCategories_EmptyList_ShouldReturnEmpty() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(categoryService, times(1)).getAllCategories();
    }

    // ---------------- GET /categories/{id} ----------------
    @Test
    void getCategoryById_ShouldReturnDto() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setId(1L); dto.setName("Cat1");

        when(categoryService.getCategoryById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cat1"));

        verify(categoryService, times(1)).getCategoryById(1L);
    }

    @Test
    void getCategoryById_NotFound_ShouldReturn500() throws Exception {
        when(categoryService.getCategoryById(1L)).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isInternalServerError());

        verify(categoryService, times(1)).getCategoryById(1L);
    }

    // ---------------- POST /categories ----------------
    @Test
    void createCategory_ShouldReturnCreated() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("NewCat");

        CategoryDto returned = new CategoryDto();
        returned.setId(1L);
        returned.setName("NewCat");

        when(categoryService.createCategory(any())).thenReturn(returned);

        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("NewCat"));

        verify(categoryService, times(1)).createCategory(any());
    }

    @Test
    void createCategory_InvalidInput_ShouldReturn400() throws Exception {
        CategoryDto dto = new CategoryDto(); // brak nazwy

        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    void createCategory_ServiceThrows_ShouldReturn500() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("NewCat");

        when(categoryService.createCategory(any())).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());

        verify(categoryService, times(1)).createCategory(any());
    }

    // ---------------- PUT /categories/{id} ----------------
    @Test
    void updateCategory_ShouldReturnUpdated() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("Updated");

        CategoryDto returned = new CategoryDto();
        returned.setId(1L);
        returned.setName("Updated");

        when(categoryService.updateCategory(eq(1L), any())).thenReturn(returned);

        mockMvc.perform(put("/api/v1/categories/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(categoryService, times(1)).updateCategory(eq(1L), any());
    }

    @Test
    void updateCategory_InvalidInput_ShouldReturn400() throws Exception {
        CategoryDto dto = new CategoryDto(); // brak nazwy

        mockMvc.perform(put("/api/v1/categories/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).updateCategory(anyLong(), any());
    }

    @Test
    void updateCategory_ServiceThrows_ShouldReturn500() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("Updated");

        when(categoryService.updateCategory(eq(1L), any())).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(put("/api/v1/categories/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());

        verify(categoryService, times(1)).updateCategory(eq(1L), any());
    }

    // ---------------- DELETE /categories/{id} ----------------
    @Test
    void deleteCategory_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(1L);
    }

    @Test
    void deleteCategory_ServiceThrows_ShouldReturn500() throws Exception {
        doThrow(new RuntimeException("DB error")).when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/v1/categories/1").with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(categoryService, times(1)).deleteCategory(1L);
    }
}
