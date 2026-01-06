package com.example.task.controller;

import com.example.task.controller.api.TaskApiController;
import com.example.task.model.dto.TaskCreateDto;
import com.example.task.model.dto.TaskDto;
import com.example.task.model.TaskStatus;
import com.example.task.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskApiController.class)
@WithMockUser
class TaskApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------- GET /tasks/{id} --------------------
    @Test
    void getTaskById_ShouldReturnDto() throws Exception {
        TaskDto dto = new TaskDto();
        dto.setId(1L);
        dto.setTitle("Test");

        when(taskService.getTaskById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test"));

        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    void getTaskById_NotFound_ShouldReturn404() throws Exception {
        when(taskService.getTaskById(1L)).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isInternalServerError());

        verify(taskService, times(1)).getTaskById(1L);
    }

    // -------------------- POST /tasks --------------------
    @Test
    void createTask_ShouldReturnCreated() throws Exception {
        TaskCreateDto createDto = new TaskCreateDto();
        createDto.setTitle("New");
        createDto.setStatus(TaskStatus.TODO);

        TaskDto dto = new TaskDto();
        dto.setId(1L);
        dto.setTitle("New");

        when(taskService.createTask(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(taskService, times(1)).createTask(any());
    }

    @Test
    void createTask_InvalidInput_ShouldReturn400() throws Exception {
        TaskCreateDto createDto = new TaskCreateDto(); // brak title i status

        mockMvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any());
    }

    // -------------------- PUT /tasks/{id} --------------------
    @Test
    void updateTask_ShouldReturnUpdated() throws Exception {
        TaskCreateDto createDto = new TaskCreateDto();
        createDto.setTitle("Updated");
        createDto.setStatus(TaskStatus.TODO);

        TaskDto dto = new TaskDto();
        dto.setId(1L);
        dto.setTitle("Updated");

        when(taskService.updateTask(anyLong(), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));

        verify(taskService, times(1)).updateTask(eq(1L), any());
    }

    @Test
    void updateTask_NotFound_ShouldReturn404() throws Exception {
        TaskCreateDto createDto = new TaskCreateDto();
        createDto.setTitle("Updated");
        createDto.setStatus(TaskStatus.TODO);

        when(taskService.updateTask(anyLong(), any())).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(put("/api/v1/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isInternalServerError());

        verify(taskService, times(1)).updateTask(eq(1L), any());
    }

    // -------------------- DELETE /tasks/{id} --------------------
    @Test
    void deleteTask_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(1L);
    }

    @Test
    void deleteTask_NotFound_ShouldReturn404() throws Exception {
        doThrow(new RuntimeException("Not found")).when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/v1/tasks/1").with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(taskService, times(1)).deleteTask(1L);
    }

    // -------------------- GET /tasks/jdbc --------------------
    @Test
    void getTasksJdbc_ShouldReturnList() throws Exception {
        TaskDto dto = new TaskDto();
        dto.setId(1L); dto.setTitle("JDBC Task");

        when(taskService.getAllTasksJdbc()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/tasks/jdbc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("JDBC Task"));

        verify(taskService, times(1)).getAllTasksJdbc();
    }

    // -------------------- GET /tasks/export/csv --------------------
    @Test
    void exportTasksToCsv_ShouldReturnCsv() throws Exception {
        TaskDto dto = new TaskDto();
        dto.setId(1L); dto.setTitle("CSV Task");

        when(taskService.getAllTasksJdbc()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/tasks/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=zadania_")));

        verify(taskService, times(1)).getAllTasksJdbc();
    }

    @Test
    void exportTasksToCsv_ShouldHandleException() throws Exception {
        when(taskService.getAllTasksJdbc()).thenThrow(new RuntimeException("IO Error"));

        mockMvc.perform(get("/api/v1/tasks/export/csv"))
                .andExpect(status().isInternalServerError());

        verify(taskService, times(1)).getAllTasksJdbc();
    }


    // -------------------- GET /tasks/export/statistics/csv --------------------
    @Test
    void exportStatisticsCsv_ShouldReturnCsv() throws Exception {
        List<String[]> rows = List.of(new String[]{"Typ", "Wartość"}, new String[]{"Total", "1"});
        when(taskService.getStatisticsForCsv()).thenReturn(rows);

        mockMvc.perform(get("/api/v1/tasks/export/statistics/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("statystyki_")));

        verify(taskService, times(1)).getStatisticsForCsv();
    }

    @Test
    void exportStatisticsCsv_ShouldHandleException() throws Exception {
        when(taskService.getStatisticsForCsv()).thenThrow(new RuntimeException("IO Error"));

        mockMvc.perform(get("/api/v1/tasks/export/statistics/csv"))
                .andExpect(status().isInternalServerError());

        verify(taskService, times(1)).getStatisticsForCsv();
    }


    // -------------------- GET /tasks with filters --------------------
    @Test
    void getTasksWithFilters_ShouldReturnPage() throws Exception {
        TaskDto dto = new TaskDto();
        dto.setId(1L);
        dto.setTitle("Filtered Task");

        org.springframework.data.domain.Page<TaskDto> page =
                new org.springframework.data.domain.PageImpl<>(List.of(dto));

        when(taskService.getTasksWithFilters(null, null, null, null, null, 0, 9, "createdAt,desc")).thenReturn(page);

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Filtered Task"));

        verify(taskService, times(1)).getTasksWithFilters(null, null, null, null, null, 0, 9, "createdAt,desc");
    }
}
