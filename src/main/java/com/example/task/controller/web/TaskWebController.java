package com.example.task.controller.web;

import com.example.task.model.dto.TaskCreateDto;
import com.example.task.model.dto.TaskDto;
import com.example.task.model.entity.TaskStatus;
import com.example.task.service.CategoryService;
import com.example.task.service.TaskService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.StringJoiner;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class TaskWebController {

    private final TaskService taskService;
    private final CategoryService categoryService;

    // Strona główna
    @GetMapping
    public String index() {
        return "index";
    }

    // Lista zadań z filtrami i paginacją
    @GetMapping("/tasks")
    public String listTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) LocalDate dueDateBefore,
            @RequestParam(required = false) LocalDate dueDateAfter,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            Model model) {

        page = Math.max(0, page);
        size = Math.min(100, Math.max(1, size));

        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createdAt";

        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            property = parts[0];
            if (parts.length > 1) {
                direction = "asc".equalsIgnoreCase(parts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
            }
        }

        // Mapowanie nazw z URL na pola encji
        property = switch (property) {
            case "dueDate", "due_date" -> "dueDate";
            case "createdAt", "created_at" -> "createdAt";
            case "updatedAt", "updated_at" -> "updatedAt";
            default -> property;
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        Page<TaskDto> tasks = taskService.getAllTasks(status, categoryId, dueDateBefore, dueDateAfter, title, pageable);

        model.addAttribute("tasks", tasks);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentParams", buildQueryParams(status, categoryId, dueDateBefore, dueDateAfter, title, size, sort));

        return "tasks/list";
    }

    private String buildQueryParams(TaskStatus status, Long categoryId, LocalDate before, LocalDate after,
                                    String title, int size, String sort) {
        StringJoiner joiner = new StringJoiner("&");
        if (status != null) joiner.add("status=" + status);
        if (categoryId != null) joiner.add("categoryId=" + categoryId);
        if (before != null) joiner.add("dueDateBefore=" + before);
        if (after != null) joiner.add("dueDateAfter=" + after);
        if (title != null && !title.isBlank()) joiner.add("title=" + title);
        joiner.add("size=" + size);
        if (sort != null && !sort.isEmpty()) joiner.add("sort=" + sort);
        return joiner.length() > 0 ? joiner.toString() : "";
    }

    // Formularz nowego zadania
    @GetMapping("/tasks/new")
    public String newTask(Model model) {
        model.addAttribute("task", new TaskCreateDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Nowe zadanie");
        return "tasks/form";
    }

    // Tworzenie nowego zadania
    @PostMapping("/tasks")
    public String createTask(@Valid @ModelAttribute("task") TaskCreateDto taskCreateDto,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("pageTitle", "Nowe zadanie");
            return "tasks/form";
        }
        taskService.createTask(taskCreateDto);
        return "redirect:/tasks";
    }

    // Formularz edycji zadania
    @GetMapping("/tasks/{id}")
    public String editTask(@PathVariable Long id, Model model) {
        TaskDto task = taskService.getTaskById(id);
        model.addAttribute("task", task);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Edytuj zadanie");
        return "tasks/form";
    }

    // Aktualizacja zadania
    @PostMapping("/tasks/{id}")
    public String updateTask(@PathVariable Long id,
                             @Valid @ModelAttribute("task") TaskCreateDto taskUpdateDto,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("pageTitle", "Edytuj zadanie");
            return "tasks/form";
        }
        taskService.updateTask(id, taskUpdateDto);
        return "redirect:/tasks";
    }

    // Usuwanie zadania
    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/tasks";
    }

    // Eksport zadań do CSV
    @GetMapping("/tasks/download")
    public void downloadTasksCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=zadania.csv");

        // Pobranie wszystkich zadań bez paginacji
        List<TaskDto> tasks = taskService.getAllTasksForCsv();

        try (PrintWriter writer = new PrintWriter(response.getOutputStream(), true)) {
            writer.println("ID,Tytuł,Opis,Status,Kategoria,Termin");
            for (TaskDto t : tasks) {
                writer.printf("%d,%s,%s,%s,%s,%s%n",
                        t.getId(),
                        t.getTitle(),
                        t.getDescription() != null ? t.getDescription() : "",
                        t.getStatus(),
                        t.getCategoryName() != null ? t.getCategoryName() : "",
                        t.getDueDate() != null ? t.getDueDate() : "");
            }
        }
    }

}
