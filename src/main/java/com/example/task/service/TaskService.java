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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final TaskMapper taskMapper;

    public Page<TaskDto> getAllTasks(
            TaskStatus status,
            Long categoryId,
            LocalDate dueDateBefore,
            LocalDate dueDateAfter,
            String title,
            Pageable pageable) {

        return taskRepository.searchTasks(status, categoryId, dueDateBefore, dueDateAfter, title, pageable)
                .map(taskMapper::toDto);
    }

    public TaskDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zadanie o id " + id + " nie istnieje"));
        return taskMapper.toDto(task);
    }

    @Transactional
    public TaskDto createTask(TaskCreateDto dto) {
        Category category = dto.getCategoryId() != null ?
                categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Kategoria o id " + dto.getCategoryId() + " nie istnieje"))
                : null;

        Task task = taskMapper.toEntity(dto, category);
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    @Transactional
    public TaskDto updateTask(Long id, TaskCreateDto dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zadanie o id " + id + " nie istnieje"));

        Category category = dto.getCategoryId() != null ?
                categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Kategoria o id " + dto.getCategoryId() + " nie istnieje"))
                : null;

        taskMapper.updateEntityFromDto(dto, task, category);
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Zadanie o id " + id + " nie istnieje");
        }
        taskRepository.deleteById(id);
    }

    // Statystyki
    public long countByStatus(TaskStatus status) {
        return status == null ? taskRepository.count() : taskRepository.findByStatus(status, Pageable.unpaged()).getContent().size();
    }
}