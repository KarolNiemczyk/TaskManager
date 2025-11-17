package com.example.task.controller.api;

import com.example.task.model.dto.TaskCreateDto;
import com.example.task.model.dto.TaskDto;
import com.example.task.model.entity.TaskStatus;
import com.example.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Zadania", description = "CRUD dla zadań")
public class TaskApiController {

    private final TaskService taskService;

    @Operation(summary = "Pobierz listę zadań z filtrami i paginacją")
    @GetMapping
    public ResponseEntity<Page<TaskDto>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) LocalDate dueDateBefore,
            @RequestParam(required = false) LocalDate dueDateAfter,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "created_at,desc") String sort) {  // ← ZMIANA: created_at

        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        Sort.Direction direction = sortParams.length > 1
                ? Sort.Direction.fromString(sortParams[1].toUpperCase())
                : Sort.Direction.DESC;

        // DOPUSZCZALNE POLA DO SORTOWANIA
        String[] allowedSortFields = {"id", "title", "status", "due_date", "created_at", "updated_at"};
        boolean isValidSort = java.util.Arrays.stream(allowedSortFields).anyMatch(sortBy::equals);
        if (!isValidSort) {
            sortBy = "created_at"; // fallback
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<TaskDto> tasks = taskService.getAllTasks(status, categoryId, dueDateBefore, dueDateAfter, title, pageable);
        return ResponseEntity.ok(tasks);
    }
    @Operation(summary = "Pobierz zadanie po ID")
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable Long id) {
        TaskDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Utwórz nowe zadanie")
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskCreateDto dto) {
        TaskDto created = taskService.createTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Zaktualizuj zadanie")
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id, @Valid @RequestBody TaskCreateDto dto) {
        TaskDto updated = taskService.updateTask(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Usuń zadanie")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}