package com.example.task.service;

import com.example.task.exception.ResourceNotFoundException;
import com.example.task.mapper.TaskMapper;
import com.example.task.model.dto.TaskCreateDto;
import com.example.task.model.dto.TaskDto;
import com.example.task.model.entity.Category;
import com.example.task.model.entity.Task;
import com.example.task.model.entity.TaskStatus;
import com.example.task.repository.CategoryRepository;
import com.example.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllTasks() {
        Task task = new Task();
        task.setId(1L);
        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskRepository.searchTasks(null,null,null,null,null, Pageable.unpaged())).thenReturn(page);
        when(taskMapper.toDto(task)).thenReturn(new TaskDto());

        Page<TaskDto> result = taskService.getAllTasks(null,null,null,null,null, Pageable.unpaged());
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void testGetTaskByIdFound() {
        Task task = new Task();
        task.setId(1L);
        TaskDto dto = new TaskDto();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(dto);

        TaskDto result = taskService.getTaskById(1L);
        assertThat(result).isNotNull();
    }

    @Test
    void testGetTaskByIdNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(1L));
    }
    @Test
    void testCreateTask() {
        TaskCreateDto createDto = new TaskCreateDto();
        createDto.setTitle("TestTask");
        createDto.setStatus(TaskStatus.TODO);
        createDto.setCategoryId(null); // brak kategorii

        Task task = new Task();
        TaskDto dto = new TaskDto();

        // ponieważ categoryId = null – serwis NIE powinnien wywołać categoryRepository
        when(taskMapper.toEntity(createDto, null)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(dto);

        TaskDto result = taskService.createTask(createDto);

        assertThat(result).isNotNull();
    }

    @Test
    void testUpdateTaskFound() {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("Updated");
        dto.setStatus(TaskStatus.TODO);

        Task task = new Task();
        Category category = new Category();
        TaskDto taskDto = new TaskDto();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(categoryRepository.findById(null)).thenReturn(Optional.of(category));
        doNothing().when(taskMapper).updateEntityFromDto(dto, task, category);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        TaskDto result = taskService.updateTask(1L, dto);
        assertThat(result).isNotNull();
    }

    @Test
    void testUpdateTaskNotFound() {
        TaskCreateDto dto = new TaskCreateDto();
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(1L, dto));
    }

    @Test
    void testDeleteTaskFound() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        taskService.deleteTask(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteTaskNotFound() {
        when(taskRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(1L));
    }
}
