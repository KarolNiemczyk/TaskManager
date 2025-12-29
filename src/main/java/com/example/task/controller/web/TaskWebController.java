package com.example.task.controller.web;

import com.example.task.model.dto.TaskCreateDto;
import com.example.task.model.dto.TaskDto;
import com.example.task.model.TaskStatus;
import com.example.task.service.CategoryService;
import com.example.task.service.TaskService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.StringJoiner;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class TaskWebController {

    private final TaskService taskService;
    private final CategoryService categoryService;

    @GetMapping
    public String index() {
        log.info("Wyświetlenie strony głównej");
        return "index";
    }

    @GetMapping("/tasks")
    public String listTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) LocalDate dueDateBefore,
            @RequestParam(required = false) LocalDate dueDateAfter,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "createdAt") String sortProperty,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Model model) {

        log.info("Wywołanie listTasks z filtrami: status={}, categoryId={}, dueDateBefore={}, dueDateAfter={}, title={}, page={}, size={}, sortProperty={}, sortDirection={}",
                status, categoryId, dueDateBefore, dueDateAfter, title, page, size, sortProperty, sortDirection);

        String sort = sortProperty + "," + sortDirection;

        Page<TaskDto> tasks = taskService.getTasksWithFilters(
                status, categoryId, dueDateBefore, dueDateAfter, title, page, size, sort
        );

        model.addAttribute("size", size);
        model.addAttribute("title", title);
        model.addAttribute("tasks", tasks);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentParams", buildQueryParams(status, categoryId, dueDateBefore, dueDateAfter, title, size, sortProperty, sortDirection));
        model.addAttribute("sortProperty", sortProperty);
        model.addAttribute("sortDirection", sortDirection);

        log.debug("Zwracana liczba zadań: {}", tasks.getTotalElements());
        return "tasks/list";
    }

    private String buildQueryParams(TaskStatus status, Long categoryId, LocalDate before, LocalDate after,
                                    String title, int size, String sortProperty, String sortDirection) {
        StringJoiner joiner = new StringJoiner("&");
        if (status != null) joiner.add("status=" + status);
        if (categoryId != null) joiner.add("categoryId=" + categoryId);
        if (before != null) joiner.add("dueDateBefore=" + before);
        if (after != null) joiner.add("dueDateAfter=" + after);
        if (title != null && !title.isBlank()) joiner.add("title=" + title.trim());
        joiner.add("size=" + size);
        joiner.add("sortProperty=" + (sortProperty != null ? sortProperty : "createdAt"));
        joiner.add("sortDirection=" + (sortDirection != null ? sortDirection : "desc"));
        String params = joiner.toString();
        return params.isEmpty() ? "" : "&" + params;
    }

    @GetMapping("/tasks/new")
    public String newTask(Model model) {
        log.info("Wyświetlenie formularza tworzenia nowego zadania");
        model.addAttribute("task", new TaskCreateDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Nowe zadanie");
        model.addAttribute("taskId", null);
        return "tasks/form";
    }

    @PostMapping("/tasks")
    public String createTask(@Valid @ModelAttribute("task") TaskCreateDto dto,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            log.warn("Błąd walidacji przy tworzeniu zadania: {}", result.getAllErrors());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("pageTitle", "Nowe zadanie");
            model.addAttribute("taskId", null);
            return "tasks/form";
        }
        log.info("Tworzenie nowego zadania: {}", dto.getTitle());
        taskService.createTask(dto);
        return "redirect:/tasks";
    }

    @GetMapping("/tasks/{id}")
    public String editTask(@PathVariable Long id, Model model) {
        log.info("Wyświetlenie formularza edycji zadania id={}", id);
        TaskDto taskDto = taskService.getTaskById(id);
        TaskCreateDto dto = taskService.toCreateDto(taskDto);
        model.addAttribute("task", dto);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Edytuj zadanie");
        model.addAttribute("taskId", id);
        return "tasks/form";
    }

    @PostMapping("/tasks/{id}")
    public String updateTask(@PathVariable Long id,
                             @Valid @ModelAttribute("task") TaskCreateDto dto,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            log.warn("Błąd walidacji przy aktualizacji zadania id={}: {}", id, result.getAllErrors());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("pageTitle", "Edytuj zadanie");
            model.addAttribute("taskId", id);
            return "tasks/form";
        }
        log.info("Aktualizacja zadania id={}, nowy tytuł={}", id, dto.getTitle());
        taskService.updateTask(id, dto);
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id) {
        log.info("Usuwanie zadania id={}", id);
        taskService.deleteTask(id);
        return "redirect:/tasks";
    }

    @GetMapping("/tasks/download")
    public void downloadCsv(HttpServletResponse response) throws IOException {
        log.info("Rozpoczęcie eksportu zadań do CSV");
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=zadania.csv");

        List<TaskDto> tasks = taskService.getAllTasksJdbc();

        try (PrintWriter writer = response.getWriter()) {
            writer.println("ID;Tytuł;Opis;Status;Kategoria;Termin");
            for (TaskDto t : tasks) {
                writer.printf("%d;%s;%s;%s;%s;%s%n",
                        t.getId(),
                        escapeCsv(t.getTitle()),
                        escapeCsv(t.getDescription()),
                        t.getStatus(),
                        escapeCsv(t.getCategoryName()),
                        t.getDueDate() != null ? t.getDueDate() : ""
                );
            }
        }
        log.info("Eksport CSV zakończony, liczba zadań: {}", tasks.size());
    }

    @GetMapping("/tasks/statistics/download")
    public void downloadStatisticsCsv(HttpServletResponse response) throws IOException {
        log.info("Rozpoczęcie eksportu statystyk zadań do CSV");
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=statystyki.csv");

        List<String[]> rows = taskService.getStatisticsForCsv();

        try (PrintWriter writer = response.getWriter()) {
            for (String[] row : rows) {
                writer.println(String.join(";", row));
            }
        }
        log.info("Eksport statystyk CSV zakończony, liczba wierszy: {}", rows.size());
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
