package com.example.task.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryDto {

    private Long id;

    @NotBlank(message = "Nazwa kategorii jest wymagana")
    @Size(max = 50, message = "Nazwa nie może przekraczać 50 znaków")
    private String name;

    private String color;
}