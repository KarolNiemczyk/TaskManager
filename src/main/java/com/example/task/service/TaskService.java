package com.example.task.service;

import com.example.task.exception.ResourceNotFoundException;
import com.example.task.mapper.TaskMapper;
import com.example.task.model.dto.TaskCreateDto;
import com.example.task.model.dto.TaskDto;
import com.example.task.model.dto.StatisticsDto;
import com.example.task.model.entity.Category;
import com.example.task.model.entity.Task;
import com.example.task.model.entity.TaskStatus;
import com.example.task.repository.CategoryRepository;
import com.example.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.example.task.repository.jdbc.TaskJdbcDao;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final TaskMapper taskMapper;
    private final TaskJdbcDao taskJdbcDao;

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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    public TaskCreateDto toCreateDto(TaskDto dto) {
        TaskCreateDto createDto = new TaskCreateDto();
        createDto.setTitle(dto.getTitle());
        createDto.setDescription(dto.getDescription());
        createDto.setStatus(dto.getStatus());
        createDto.setDueDate(dto.getDueDate());
        createDto.setCategoryId(dto.getCategoryId());
        return createDto;
    }
    @Transactional(readOnly = true)
    public StatisticsDto getTaskStatistics() {
        List<Task> tasks = taskRepository.findAll();

        long total = tasks.size();

        Map<String, Long> byStatus = tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getStatus().name(),
                        Collectors.counting()
                ));

        Map<String, Long> byCategory = tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory().getName() : "Uncategorized",
                        Collectors.counting()
                ));

        return new StatisticsDto(total, byStatus, byCategory);
    }
    @Transactional(readOnly = true)
    public List<TaskDto> getAllTasksJdbc() {
        return taskJdbcDao.findAllAsDtos();
    }

    @Transactional(readOnly = true)
    public List<String[]> getStatisticsForCsv() {
        StatisticsDto stats = getTaskStatistics();

        Map<String, Long> byStatus = stats.getTasksByStatus();
        Map<String, Long> byCategory = stats.getTasksByCategory();
        long total = stats.getTotalTasks();   // <-- tutaj poprawnie

// Lista do CSV
        List<String[]> rows = new java.util.ArrayList<>();
        rows.add(new String[]{"Typ", "Wartość"});
        rows.add(new String[]{"Liczba wszystkich zadań", String.valueOf(total)});

// Statusy
        rows.add(new String[]{"Zadania TODO", String.valueOf(byStatus.getOrDefault("TODO", 0L))});
        rows.add(new String[]{"W trakcie", String.valueOf(byStatus.getOrDefault("IN_PROGRESS", 0L))});
        rows.add(new String[]{"Zrobione", String.valueOf(byStatus.getOrDefault("DONE", 0L))});

// Kategorie — dynamiczne
        for (Map.Entry<String, Long> entry : byCategory.entrySet()) {
            rows.add(new String[]{"Kategoria: " + entry.getKey(), String.valueOf(entry.getValue())});
        }

        return rows;
    }

    private Category resolveCategory(Long categoryId) {
        return categoryId != null ?
                categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Kategoria o id " + categoryId + " nie istnieje"))
                : null;
    }

}