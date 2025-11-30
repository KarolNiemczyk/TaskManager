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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final TaskMapper taskMapper;

    public Page<TaskDto> getTasksWithFilters(
            TaskStatus status,
            Long categoryId,
            LocalDate dueDateBefore,
            LocalDate dueDateAfter,
            String title,
            int page,
            int size,
            String sort) {

        page = Math.max(0, page);
        size = size <= 0 || size > 100 ? 10 : size;

        String[] sortParts = sort.split(",");
        String property = sortParts[0];
        Sort.Direction direction = sortParts.length > 1
                ? Sort.Direction.fromString(sortParts[1].toUpperCase())
                : Sort.Direction.DESC;

        String javaField = switch (property) {
            case "due_date", "dueDate"         -> "dueDate";
            case "created_at", "createdAt"     -> "createdAt";
            case "updated_at", "updatedAt"     -> "updatedAt";
            case "id", "title", "status"       -> property;
            default                            -> "createdAt";
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, javaField));
        return getAllTasks(status, categoryId, dueDateBefore, dueDateAfter, title, pageable);
    }

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
        Category category = resolveCategory(dto.getCategoryId());
        Task task = taskMapper.toEntity(dto, category);
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    @Transactional
    public TaskDto updateTask(Long id, TaskCreateDto dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zadanie o id " + id + " nie istnieje"));

        Category category = resolveCategory(dto.getCategoryId());
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

    public List<TaskDto> getAllTasksForCsv() {
        return getAllTasks(null, null, null, null, null, Pageable.unpaged()).getContent();
    }

    public TaskCreateDto toCreateDto(TaskDto dto) {
        TaskCreateDto createDto = new TaskCreateDto();
        createDto.setTitle(dto.getTitle());
        createDto.setDescription(dto.getDescription());
        createDto.setStatus(dto.getStatus());
        createDto.setDueDate(dto.getDueDate());
        createDto.setCategoryId(dto.getCategoryId());
        return createDto;
    }

    private Category resolveCategory(Long categoryId) {
        return categoryId != null ?
                categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Kategoria o id " + categoryId + " nie istnieje"))
                : null;
    }
}
