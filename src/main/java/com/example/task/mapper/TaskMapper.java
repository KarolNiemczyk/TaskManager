package com.example.task.mapper;

import com.example.task.model.dto.TaskCreateDto;
import com.example.task.model.dto.TaskDto;
import com.example.task.model.entity.Task;
import com.example.task.model.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskDto toDto(Task task) {
        if (task == null) return null;

        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        Category category = task.getCategory();
        if (category != null) {
            dto.setCategoryId(category.getId());
            dto.setCategoryName(category.getName());
        }

        return dto;
    }

    public Task toEntity(TaskCreateDto dto, Category category) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setDueDate(dto.getDueDate());
        task.setCategory(category);
        return task;
    }

    public void updateEntityFromDto(TaskCreateDto dto, Task task, Category category) {
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setDueDate(dto.getDueDate());
        task.setCategory(category);
    }
}