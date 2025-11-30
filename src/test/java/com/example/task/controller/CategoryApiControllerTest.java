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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryApiController.class)
class CategoryApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllCategories_ShouldReturnList() throws Exception {
        Mockito.when(categoryService.getAllCategories())
                .thenReturn(List.of(new CategoryDto(){{
                    setId(1L); setName("A");
                }}));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("A"));
    }

    @Test
    void getCategoryById_ShouldReturnDto() throws Exception {
        Mockito.when(categoryService.getCategoryById(1L))
                .thenReturn(new CategoryDto(){{
                    setId(1L); setName("A");
                }});

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("A"));
    }

    @Test
    void createCategory_ShouldReturnCreated() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("New");

        Mockito.when(categoryService.createCategory(any())).thenReturn(new CategoryDto(){{
            setId(1L); setName("New");
        }});

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateCategory_ShouldReturnUpdated() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("Updated");

        Mockito.when(categoryService.updateCategory(anyLong(), any())).thenReturn(new CategoryDto(){{
            setId(1L); setName("Updated");
        }});

        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteCategory_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(categoryService).deleteCategory(1L);
    }
}
