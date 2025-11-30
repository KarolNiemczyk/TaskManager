package com.example.task.controller;

import com.example.task.controller.api.TaskApiController;
import com.example.task.model.dto.TaskCreateDto;
import com.example.task.model.dto.TaskDto;
import com.example.task.model.entity.TaskStatus;
import com.example.task.repository.TaskRepository;
import com.example.task.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskApiController.class)
class TaskApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getTaskById_ShouldReturnDto() throws Exception {
        TaskDto dto = new TaskDto();
        dto.setId(1L);
        dto.setTitle("Test");

        Mockito.when(taskService.getTaskById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test"));
    }

    @Test
    void createTask_ShouldReturnCreated() throws Exception {
        TaskCreateDto createDto = new TaskCreateDto();
        createDto.setTitle("New"); createDto.setStatus(TaskStatus.TODO);

        TaskDto dto = new TaskDto();
        dto.setId(1L);
        dto.setTitle("New");

        Mockito.when(taskService.createTask(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateTask_ShouldReturnUpdated() throws Exception {
        TaskCreateDto createDto = new TaskCreateDto();
        createDto.setTitle("Updated"); createDto.setStatus(TaskStatus.TODO);

        TaskDto dto = new TaskDto();
        dto.setId(1L); dto.setTitle("Updated");

        Mockito.when(taskService.updateTask(anyLong(), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void deleteTask_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(taskService).deleteTask(1L);
    }
}
