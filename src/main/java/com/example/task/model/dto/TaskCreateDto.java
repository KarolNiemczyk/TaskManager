package com.example.task.model.dto;

import com.example.task.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class TaskCreateDto {

    @NotBlank(message = "Tytuł jest wymagany")
    @Size(max = 100, message = "Tytuł nie może przekraczać 100 znaków")
    private String title;

    @Size(max = 1000, message = "Opis nie może przekraczać 1000 znaków")
    private String description;

    @NotNull(message = "Status jest wymagany")
    private TaskStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    private Long categoryId;
}
