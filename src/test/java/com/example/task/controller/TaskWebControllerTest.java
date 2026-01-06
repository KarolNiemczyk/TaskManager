package com.example.task.controller;

import com.example.task.controller.web.TaskWebController;
import com.example.task.model.TaskStatus;
import com.example.task.model.dto.CategoryDto;
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

@WebMvcTest(TaskWebController.class)
@WithMockUser
class TaskWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private CategoryService categoryService;

    // ---------- GET / ----------
    @Test
    void index_ShouldReturnIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }


    // ---------- GET /tasks/new ----------
    @Test
    void listTasks_ShouldReturnListView() throws Exception {
        TaskDto dto = new TaskDto();
        dto.setId(1L);
        dto.setTitle("Test");
        dto.setStatus(TaskStatus.TODO);

        Page<TaskDto> page = new PageImpl<>(List.of(dto));

        when(taskService.getTasksWithFilters(any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString()))
                .thenReturn(page);
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"))
                .andExpect(model().attributeExists("tasks", "categories"));
    }
    // ---------- GET /tasks ----------
    @Test
    void listTasks_WithFilters_ShouldPassParams() throws Exception {
        when(taskService.getTasksWithFilters(eq(TaskStatus.TODO), eq(1L), any(), any(), eq("abc"),
                eq(0), eq(9), eq("createdAt,desc")))
                .thenReturn(Page.empty());

        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/tasks")
                        .param("status", "TODO")
                        .param("categoryId", "1")
                        .param("title", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"));
    }
    @Test
    void newTask_ShouldReturnForm() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/tasks/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"))
                .andExpect(model().attributeExists("task", "categories"));
    }

    // ---------- POST /tasks ----------
    @Test
    void createTask_Valid_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/tasks")
                        .with(csrf())
                        .param("title", "New task")
                        .param("status", "TODO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        verify(taskService, times(1)).createTask(any());
    }

    @Test
    void createTask_Invalid_ShouldReturnForm() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(post("/tasks")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"));

        verify(taskService, never()).createTask(any());
    }

    // ---------- GET /tasks/{id} ----------
    @Test
    void editTask_ShouldReturnForm() throws Exception {
        TaskDto dto = new TaskDto();
        dto.setId(1L);
        dto.setTitle("Edit");

        when(taskService.getTaskById(1L)).thenReturn(dto);
        when(taskService.toCreateDto(dto)).thenReturn(new TaskCreateDto());
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"))
                .andExpect(model().attributeExists("task", "categories"));
    }

    @Test
    void editTask_NotFound_ShouldFail() throws Exception {
        when(taskService.getTaskById(1L)).thenThrow(new RuntimeException());

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isInternalServerError());
    }

    // ---------- POST /tasks/{id} ----------
    @Test
    void updateTask_Valid_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/tasks/1")
                        .with(csrf())
                        .param("title", "Updated")
                        .param("status", "TODO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        verify(taskService).updateTask(eq(1L), any());
    }

    @Test
    void updateTask_Invalid_ShouldReturnForm() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(post("/tasks/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"));

        verify(taskService, never()).updateTask(anyLong(), any());
    }

    // ---------- POST /tasks/{id}/delete ----------
    @Test
    void deleteTask_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/tasks/1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        verify(taskService).deleteTask(1L);
    }

    // ---------- GET /tasks/download ----------
    @Test
    void downloadCsv_ShouldReturnCsv() throws Exception {
        when(taskService.getAllTasksJdbc()).thenReturn(List.of());

        mockMvc.perform(get("/tasks/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=zadania.csv"));
    }

    // ---------- GET /tasks/statistics/download ----------
    @Test
    void downloadStatisticsCsv_ShouldReturnCsv() throws Exception {
        when(taskService.getStatisticsForCsv()).thenReturn(List.<String[]>of(new String[]{"A", "1"}));

        mockMvc.perform(get("/tasks/statistics/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=statystyki.csv"));
    }
    // ---------- ADDITIONAL TESTS ----------
    @Test
    void listTasks_Empty_ShouldReturnListView() throws Exception {
        when(taskService.getTasksWithFilters(any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString()))
                .thenReturn(Page.empty());
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"))
                .andExpect(model().attribute("tasks", Page.empty()))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void createTask_WithDescription_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/tasks")
                        .with(csrf())
                        .param("title", "Task with description")
                        .param("description", "Some description")
                        .param("status", "TODO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        verify(taskService).createTask(any());
    }

    @Test
    void createTask_MissingTitle_ShouldReturnForm() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(post("/tasks")
                        .with(csrf())
                        .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"));

        verify(taskService, never()).createTask(any());
    }

    @Test
    void updateTask_NonExistingId_ShouldFail() throws Exception {
        doThrow(new RuntimeException("Not found")).when(taskService).updateTask(eq(99L), any());

        mockMvc.perform(post("/tasks/99")
                        .with(csrf())
                        .param("title", "Update non-existing")
                        .param("status", "TODO"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteTask_NonExistingId_ShouldFail() throws Exception {
        doThrow(new RuntimeException("Not found")).when(taskService).deleteTask(99L);

        mockMvc.perform(post("/tasks/99/delete").with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void downloadCsv_WithTasks_ShouldContainData() throws Exception {
        TaskDto dto = new TaskDto();
        dto.setId(1L);
        dto.setTitle("CSV Task");
        dto.setDescription("Desc");
        dto.setStatus(TaskStatus.TODO);
        dto.setCategoryName("Cat");
        dto.setDueDate(LocalDate.of(2026, 1, 6));

        when(taskService.getAllTasksJdbc()).thenReturn(List.of(dto));

        mockMvc.perform(get("/tasks/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=zadania.csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("CSV Task")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("TODO")));
    }

    @Test
    void downloadCsv_EmptyList_ShouldContainHeaderOnly() throws Exception {
        when(taskService.getAllTasksJdbc()).thenReturn(List.of());

        mockMvc.perform(get("/tasks/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=zadania.csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ID;Tytuł;Opis;Status;Kategoria;Termin")));
    }

    @Test
    void downloadCsv_WithSpecialCharacters_ShouldEscapeProperly() throws Exception {
        TaskDto dto = new TaskDto();
        dto.setId(2L);
        dto.setTitle("Title;With;Semicolons");
        dto.setDescription("Desc\nWithNewline");
        dto.setStatus(TaskStatus.TODO);

        when(taskService.getAllTasksJdbc()).thenReturn(List.of(dto));

        mockMvc.perform(get("/tasks/download"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"Title;With;Semicolons\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"Desc\nWithNewline\"")));
    }

    @Test
    void editTask_WithCategory_ShouldPopulateCategoryName() throws Exception {
        TaskDto dto = new TaskDto();
        dto.setId(1L);
        dto.setTitle("Task with category");
        dto.setCategoryName("Cat1");

        when(taskService.getTaskById(1L)).thenReturn(dto);
        when(taskService.toCreateDto(dto)).thenReturn(new TaskCreateDto());
        when(categoryService.getAllCategories()).thenReturn(List.<CategoryDto>of());

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"))
                .andExpect(model().attributeExists("task", "categories"));
    }

}
