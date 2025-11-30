package com.example.task.controller.api;

import com.example.task.model.dto.TaskCreateDto;
import com.example.task.model.dto.TaskDto;
import com.example.task.model.entity.Task;
import com.example.task.model.entity.TaskStatus;
import com.example.task.repository.TaskRepository;
import com.example.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Zadania", description = "CRUD dla zadań")
public class TaskApiController {

    private final TaskService taskService;
    private final TaskRepository taskRepository; // tylko do CSV

    @Operation(summary = "Pobierz listę zadań z filtrami i paginacją")
    @GetMapping
    public ResponseEntity<Page<TaskDto>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) LocalDate dueDateBefore,
            @RequestParam(required = false) LocalDate dueDateAfter,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Page<TaskDto> result = taskService.getTasksWithFilters(status, categoryId, dueDateBefore, dueDateAfter, title, page, size, sort);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Eksport wszystkich zadań do CSV")
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportTasksToCsv() throws IOException {
        List<Task> allTasks = taskRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, "UTF-8"), CSVFormat.DEFAULT
                .withHeader("ID", "Tytuł", "Opis", "Status", "Termin", "Kategoria", "Utworzono", "Zaktualizowano"))) {

            for (Task t : allTasks) {
                printer.printRecord(
                        t.getId(),
                        t.getTitle(),
                        t.getDescription() != null ? t.getDescription() : "",
                        t.getStatus().toString(),
                        t.getDueDate() != null ? t.getDueDate().toString() : "",
                        t.getCategory() != null ? t.getCategory().getName() : "Brak",
                        t.getCreatedAt(),
                        t.getUpdatedAt()
                );
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=zadania_" + LocalDate.now() + ".csv");
        headers.set(HttpHeaders.CACHE_CONTROL, "must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(out.toByteArray());
    }

    @Operation(summary = "Pobierz zadanie po ID")
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @Operation(summary = "Utwórz nowe zadanie")
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(dto));
    }

    @Operation(summary = "Zaktualizuj zadanie")
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id, @Valid @RequestBody TaskCreateDto dto) {
        return ResponseEntity.ok(taskService.updateTask(id, dto));
    }

    @Operation(summary = "Usuń zadanie")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
